package com.example.data.ai

import com.example.data.local.BrainConfig
import com.example.domain.ShahgeeConfig
import com.example.domain.ShahgeeModelCatalog
import com.example.domain.StudioModelProfile
import com.example.domain.WorkspaceCatalog
import com.example.domain.WorkspaceModule
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

data class AiResponse(
    val title: String,
    val content: String,
    val isSampleMode: Boolean = true,
    val workspaceId: String? = null,
    val modelBadge: String = "FUSION"
)

interface AiRepository {
    val isLiveGeminiEnabled: Boolean

    suspend fun discoverLocalModels(baseUrl: String): Result<List<String>>

    suspend fun askShahgee(
        prompt: String,
        workspaceId: String? = null,
        brainConfig: BrainConfig = BrainConfig(),
        activeDirective: String = brainConfig.defaultDirective,
        selectedModelId: String = ShahgeeModelCatalog.MERGED_ID,
        mergedMemberIds: Set<String> = ShahgeeModelCatalog.defaultMergedMemberIds,
        localServerUrl: String = ""
    ): AiResponse
}

/**
 * Multi-model AI repository supporting:
 * 1) Shahgee Fusion (Merged ensemble across Gemma 3, Dolphin 2.9, Qwen 2.5 & Gemini),
 * 2) Direct Google REST inference for Gemma 3 & Gemini when GEMINI_API_KEY is set,
 * 3) Local/custom Ollama or OpenAI-compatible runtime bridge when configured,
 * 4) Clear local Sample Preview when no live endpoint or key is configured.
 */
class GeminiAiRepository(
    private val geminiApiClient: GeminiApiClient,
    private val localOpenAiClient: LocalOpenAiClient = LocalOpenAiClient(),
    private val sampleFallback: SampleAiRepository = SampleAiRepository()
) : AiRepository {

    override val isLiveGeminiEnabled: Boolean
        get() = geminiApiClient.isConfigured

    override suspend fun discoverLocalModels(baseUrl: String): Result<List<String>> =
        localOpenAiClient.discoverModels(baseUrl)

    override suspend fun askShahgee(
        prompt: String,
        workspaceId: String?,
        brainConfig: BrainConfig,
        activeDirective: String,
        selectedModelId: String,
        mergedMemberIds: Set<String>,
        localServerUrl: String
    ): AiResponse {
        val cleanPrompt = prompt.trim()
        val workspace = workspaceId?.let { WorkspaceCatalog.findById(it) }
        val profile = ShahgeeModelCatalog.findById(selectedModelId)
        val hasLocalServer = localServerUrl.isNotBlank()

        if (!geminiApiClient.isConfigured && !hasLocalServer) {
            return sampleFallback.askShahgee(
                prompt = cleanPrompt,
                workspaceId = workspaceId,
                brainConfig = brainConfig,
                activeDirective = activeDirective,
                selectedModelId = selectedModelId,
                mergedMemberIds = mergedMemberIds,
                localServerUrl = localServerUrl
            )
        }

        if (profile.isMergedEnsemble) {
            return runMergedEnsemble(
                prompt = cleanPrompt,
                workspace = workspace,
                brainConfig = brainConfig,
                activeDirective = activeDirective,
                mergedMemberIds = mergedMemberIds,
                localServerUrl = localServerUrl
            )
        }

        val systemInstruction = ShahgeeConfig.buildSystemInstruction(
            workspace = workspace,
            customDirective = activeDirective,
            brainConfig = brainConfig,
            modelProfile = profile,
            mergedMemberIds = mergedMemberIds
        )

        val text = dispatchSingleModel(
            profile = profile,
            prompt = cleanPrompt,
            systemInstruction = systemInstruction,
            localServerUrl = localServerUrl
        ).getOrThrow()

        return AiResponse(
            title = if (workspace != null) "${workspace.shortTag} • ${profile.shortBadge}" else profile.displayName,
            content = text,
            isSampleMode = false,
            workspaceId = workspaceId,
            modelBadge = profile.shortBadge
        )
    }

    private suspend fun runMergedEnsemble(
        prompt: String,
        workspace: WorkspaceModule?,
        brainConfig: BrainConfig,
        activeDirective: String,
        mergedMemberIds: Set<String>,
        localServerUrl: String
    ): AiResponse = coroutineScope {
        val members = ShahgeeModelCatalog.resolveMergeMembers(mergedMemberIds)
        val mergeBadge = ShahgeeModelCatalog.formatMergeLabel(mergedMemberIds)
        val titlePrefix = workspace?.shortTag ?: "Shahgee"

        // Query distinct live backends in parallel (local models + direct Google models)
        val drafts: List<Pair<String, String>> = members.map { member ->
            async {
                val memberInstruction = ShahgeeConfig.buildSystemInstruction(
                    workspace = workspace,
                    customDirective = activeDirective,
                    brainConfig = brainConfig,
                    modelProfile = member,
                    mergedMemberIds = mergedMemberIds
                )
                val result = dispatchForEnsembleMember(
                    member = member,
                    prompt = prompt,
                    systemInstruction = memberInstruction,
                    localServerUrl = localServerUrl
                )
                result.getOrNull()?.let { member.displayName to it }
            }
        }.awaitAll().filterNotNull()

        if (drafts.isEmpty()) {
            // Throw the primary error from a direct model attempt so UI shows actionable error
            val fallbackInstruction = ShahgeeConfig.buildSystemInstruction(
                workspace = workspace,
                customDirective = activeDirective,
                brainConfig = brainConfig,
                modelProfile = ShahgeeModelCatalog.findById(ShahgeeModelCatalog.MERGED_ID),
                mergedMemberIds = mergedMemberIds
            )
            val primaryAttempt = dispatchSingleModel(
                profile = ShahgeeModelCatalog.findById(ShahgeeModelCatalog.GEMINI_FLASH_ID),
                prompt = prompt,
                systemInstruction = fallbackInstruction,
                localServerUrl = localServerUrl
            ).getOrThrow()

            return@coroutineScope AiResponse(
                title = "$titlePrefix • Fusion",
                content = primaryAttempt,
                isSampleMode = false,
                workspaceId = workspace?.id,
                modelBadge = mergeBadge
            )
        }

        if (drafts.size == 1) {
            return@coroutineScope AiResponse(
                title = "$titlePrefix • Fusion",
                content = drafts.first().second,
                isSampleMode = false,
                workspaceId = workspace?.id,
                modelBadge = mergeBadge
            )
        }

        val fusionInstruction = ShahgeeConfig.buildSystemInstruction(
            workspace = workspace,
            customDirective = activeDirective,
            brainConfig = brainConfig,
            modelProfile = ShahgeeModelCatalog.findById(ShahgeeModelCatalog.MERGED_ID),
            mergedMemberIds = mergedMemberIds
        )
        val synthesisPrompt = ShahgeeConfig.buildMergeSynthesisPrompt(
            originalPrompt = prompt,
            specialistDrafts = drafts
        )

        val mergedText = synthesizeMergedDrafts(
            synthesisPrompt = synthesisPrompt,
            fusionInstruction = fusionInstruction,
            localServerUrl = localServerUrl
        ).getOrElse {
            drafts.joinToString("\n\n") { (label, body) -> "[$label]\n$body" }
        }

        AiResponse(
            title = "$titlePrefix • Shahgee Fusion",
            content = mergedText,
            isSampleMode = false,
            workspaceId = workspace?.id,
            modelBadge = mergeBadge
        )
    }

    private suspend fun dispatchForEnsembleMember(
        member: StudioModelProfile,
        prompt: String,
        systemInstruction: String,
        localServerUrl: String
    ): Result<String> {
        if (localServerUrl.isNotBlank() && member.id != ShahgeeModelCatalog.GEMINI_FLASH_ID) {
            val localAttempt = localOpenAiClient.chatCompletion(
                baseUrl = localServerUrl,
                modelTag = member.localModelTag,
                systemInstruction = systemInstruction,
                prompt = prompt,
                temperature = member.temperature,
                topP = member.topP
            )
            if (localAttempt.isSuccess) return localAttempt
        }

        if (geminiApiClient.isConfigured) {
            val targetGoogleModel = member.googleApiModelId ?: ShahgeeConfig.defaultPersona.modelId
            return geminiApiClient.generateContent(
                prompt = prompt,
                systemInstruction = systemInstruction,
                persona = ShahgeeConfig.defaultPersona,
                modelIdOverride = targetGoogleModel,
                temperatureOverride = member.temperature,
                topPOverride = member.topP
            )
        }

        return Result.failure(IllegalStateException("No live backend available for ${member.displayName}."))
    }

    private suspend fun dispatchSingleModel(
        profile: StudioModelProfile,
        prompt: String,
        systemInstruction: String,
        localServerUrl: String
    ): Result<String> {
        if (localServerUrl.isNotBlank() && profile.id != ShahgeeModelCatalog.GEMINI_FLASH_ID) {
            val localAttempt = localOpenAiClient.chatCompletion(
                baseUrl = localServerUrl,
                modelTag = profile.localModelTag,
                systemInstruction = systemInstruction,
                prompt = prompt,
                temperature = profile.temperature,
                topP = profile.topP
            )
            if (localAttempt.isSuccess || !geminiApiClient.isConfigured) {
                return localAttempt
            }
        }

        val targetGoogleModel = profile.googleApiModelId ?: ShahgeeConfig.defaultPersona.modelId
        return geminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = systemInstruction,
            persona = ShahgeeConfig.defaultPersona,
            modelIdOverride = targetGoogleModel,
            temperatureOverride = profile.temperature,
            topPOverride = profile.topP
        )
    }

    private suspend fun synthesizeMergedDrafts(
        synthesisPrompt: String,
        fusionInstruction: String,
        localServerUrl: String
    ): Result<String> {
        if (geminiApiClient.isConfigured) {
            val cloudMerge = geminiApiClient.generateContent(
                prompt = synthesisPrompt,
                systemInstruction = fusionInstruction,
                persona = ShahgeeConfig.defaultPersona,
                modelIdOverride = ShahgeeConfig.defaultPersona.modelId,
                temperatureOverride = 0.45,
                topPOverride = 0.9
            )
            if (cloudMerge.isSuccess) return cloudMerge
        }

        if (localServerUrl.isNotBlank()) {
            return localOpenAiClient.chatCompletion(
                baseUrl = localServerUrl,
                modelTag = ShahgeeModelCatalog.findById(ShahgeeModelCatalog.GEMMA_3_ID).localModelTag,
                systemInstruction = fusionInstruction,
                prompt = synthesisPrompt,
                temperature = 0.45,
                topP = 0.9
            )
        }

        return Result.failure(IllegalStateException("Synthesis backend unavailable."))
    }
}

/**
 * Local sample implementation used when neither GEMINI_API_KEY nor a local server URL is configured.
 */
class SampleAiRepository : AiRepository {
    override val isLiveGeminiEnabled: Boolean = false

    override suspend fun discoverLocalModels(baseUrl: String): Result<List<String>> =
        Result.failure(IllegalStateException("Local model discovery requires LocalOpenAiClient."))

    override suspend fun askShahgee(
        prompt: String,
        workspaceId: String?,
        brainConfig: BrainConfig,
        activeDirective: String,
        selectedModelId: String,
        mergedMemberIds: Set<String>,
        localServerUrl: String
    ): AiResponse {
        delay(380)
        val cleanPrompt = prompt.trim()
        val workspace = workspaceId?.let { WorkspaceCatalog.findById(it) }
        val profile = ShahgeeModelCatalog.findById(selectedModelId)
        val badge = if (profile.isMergedEnsemble) {
            ShahgeeModelCatalog.formatMergeLabel(mergedMemberIds)
        } else {
            profile.shortBadge
        }
        val contextLabel = workspace?.title ?: brainConfig.brainName
        val routeHint = workspaceId?.let { brainConfig.routingMap[it] }

        val body = buildString {
            appendLine("[Sample Preview — Set GEMINI_API_KEY or Local Server URL for live inference]")
            appendLine("Active Model: ${profile.displayName} [$badge]")
            if (workspace != null) appendLine("Workspace: $contextLabel")
            if (!routeHint.isNullOrBlank()) appendLine("Routing Focus: $routeHint")
            appendLine("Request: \"$cleanPrompt\"")
            appendLine()
            when {
                profile.isMergedEnsemble -> {
                    val activeNames = ShahgeeModelCatalog.resolveMergeMembers(mergedMemberIds)
                        .joinToString(" + ") { it.displayName }
                    appendLine("Shahgee Fusion Ensemble ($activeNames):")
                    appendLine("• Qwen 2.5 Layer: Structures actionable engineering steps & edge-case checks.")
                    appendLine("• Dolphin 2.9 Layer: Refines natural conversational tone & creative ideation.")
                    appendLine("• Gemma 3 Layer: Distills concise takeaways & multilingual readability.")
                    appendLine("• Synthesis Gate: Directive applied — \"$activeDirective\"")
                }
                profile.id == ShahgeeModelCatalog.GEMMA_3_ID -> {
                    appendLine("Gemma 3 Concise Brief:")
                    appendLine("• Clear distilled breakdown for \"$cleanPrompt\".")
                    appendLine("• Supports direct Google AI endpoint (gemma-3-27b-it) or local Ollama (gemma3).")
                }
                profile.id == ShahgeeModelCatalog.DOLPHIN_29_ID -> {
                    appendLine("Dolphin 2.9 Companion Draft:")
                    appendLine("• Creative, direct exploration of \"$cleanPrompt\" tailored for voice readback.")
                    appendLine("• Connect a local Ollama/OpenAI URL (dolphin-llama3) or GEMINI_API_KEY to run live.")
                }
                profile.id == ShahgeeModelCatalog.QWEN_25_ID -> {
                    appendLine("Qwen 2.5 Precision Plan:")
                    appendLine("1) Decompose \"$cleanPrompt\" into deterministic inputs, constraints & verification checks.")
                    appendLine("2) Connect local qwen2.5 via Ollama/LM Studio or GEMINI_API_KEY for live code/logic synthesis.")
                }
                else -> {
                    appendLine("Gemini 3.8 Flash Sample Brief:")
                    appendLine("• Objective: $cleanPrompt")
                    appendLine("• Add GEMINI_API_KEY in AI Studio Secrets for live cloud synthesis.")
                }
            }
        }

        return AiResponse(
            title = if (workspace != null) "${workspace.shortTag} • ${profile.shortBadge} Sample" else "${profile.displayName} Sample",
            content = body.trim(),
            isSampleMode = true,
            workspaceId = workspaceId,
            modelBadge = badge
        )
    }
}
