package com.example.domain

import com.example.data.local.BrainConfig

data class ShahgeePersonaSettings(
    val assistantName: String = "Shahgee",
    val studioVersion: String = "ALiRaZa AI Studio V200",
    val modelId: String = "gemini-3.8-flash",
    val temperature: Double = 0.6,
    val topP: Double = 0.9,
    val maxOutputTokens: Int = 1024,
    val defaultLocaleTag: String = "en-US",
    val voiceStyle: String = "Calm, articulate, concise holographic companion",
    val behavioralTraits: List<String> = listOf(
        "Precision-first: deliver structured, actionable steps without filler.",
        "Safety-gated: require explicit user confirmation before code execution, trades, or destructive changes.",
        "Workspace-aware: tailor depth and terminology to the active ALiRaZa Studio workspace.",
        "Voice-ready: keep primary takeaways clear enough for spoken Text-to-Speech readback."
    )
)

object ShahgeeConfig {
    val defaultPersona = ShahgeePersonaSettings()

    val coreSystemPrompt: String = buildString {
        appendLine("You are ${defaultPersona.assistantName}, the central AI companion of ${defaultPersona.studioVersion}.")
        appendLine("Studio Tribute: Built in memory of Ali Raza • V200 foundation.")
        appendLine("Voice & Tone: ${defaultPersona.voiceStyle}.")
        appendLine()
        appendLine("Core Behavioral Traits:")
        defaultPersona.behavioralTraits.forEachIndexed { index, trait ->
            appendLine("${index + 1}. $trait")
        }
        appendLine()
        appendLine("Operational Guardrails:")
        appendLine("- Never invent live telemetry, account balances, or executed side effects.")
        appendLine("- Flag assumptions clearly and recommend verification steps for high-impact decisions.")
    }.trim()

    fun buildSystemInstruction(
        workspace: WorkspaceModule?,
        customDirective: String,
        brainConfig: BrainConfig,
        modelProfile: StudioModelProfile = ShahgeeModelCatalog.findById(ShahgeeModelCatalog.MERGED_ID),
        mergedMemberIds: Set<String> = ShahgeeModelCatalog.defaultMergedMemberIds
    ): String = buildString {
        appendLine(coreSystemPrompt)
        appendLine()
        appendLine("Active Brain Profile: ${brainConfig.brainName} (${brainConfig.brainModels})")
        appendLine("Selected Model Profile: ${modelProfile.displayName} [${modelProfile.family}]")
        appendLine("Model Specialty: ${modelProfile.roleSummary}")
        if (modelProfile.isMergedEnsemble) {
            val activeMembers = ShahgeeModelCatalog.resolveMergeMembers(mergedMemberIds)
            appendLine("Merged Ensemble Members: ${activeMembers.joinToString(", ") { it.displayName }}")
            appendLine("Fusion Rule: Synthesize Qwen 2.5 engineering rigor, Dolphin conversational fluency, Gemma 3 concise clarity, and Gemini grounding into one clear response.")
        }
        val directive = customDirective.ifBlank { brainConfig.defaultDirective }
        if (directive.isNotBlank()) {
            appendLine("Active Directive: $directive")
        }
        if (workspace != null) {
            appendLine()
            appendLine("Current Workspace: ${workspace.title} [${workspace.shortTag}]")
            appendLine("Workspace Focus: ${workspace.summary}")
            appendLine("Workspace Safety Note: ${workspace.safetyNote}")
        }
        if (brainConfig.safetyGates.isNotEmpty()) {
            appendLine()
            appendLine("Brain Governance Gates:")
            brainConfig.safetyGates.forEach { gate ->
                appendLine("- $gate")
            }
        }
    }.trim()

    fun buildMergeSynthesisPrompt(
        originalPrompt: String,
        specialistDrafts: List<Pair<String, String>>
    ): String = buildString {
        appendLine("User Request:")
        appendLine(originalPrompt)
        appendLine()
        appendLine("Merge the best verified reasoning from these installed model drafts into ONE clean, accurate, non-repetitive Shahgee response:")
        specialistDrafts.forEach { (modelLabel, draft) ->
            appendLine()
            appendLine("=== $modelLabel Draft ===")
            appendLine(draft.trim())
        }
        appendLine()
        appendLine("Deliver the final merged answer directly for the user without meta-commentary about drafts unless helpful.")
    }.trim()
}
