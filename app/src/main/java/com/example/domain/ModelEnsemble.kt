package com.example.domain

data class StudioModelProfile(
    val id: String,
    val displayName: String,
    val shortBadge: String,
    val family: String,
    val googleApiModelId: String?,
    val localModelTag: String,
    val roleSummary: String,
    val strengths: String,
    val temperature: Double,
    val topP: Double,
    val isMergedEnsemble: Boolean = false
)

object ShahgeeModelCatalog {
    const val MERGED_ID = "shahgee_merged"
    const val GEMMA_3_ID = "gemma_3"
    const val DOLPHIN_29_ID = "dolphin_29"
    const val QWEN_25_ID = "qwen_25"
    const val GEMINI_FLASH_ID = "gemini_flash"

    val defaultMergedMemberIds: Set<String> = setOf(
        GEMMA_3_ID,
        DOLPHIN_29_ID,
        QWEN_25_ID,
        GEMINI_FLASH_ID
    )

    val all: List<StudioModelProfile> = listOf(
        StudioModelProfile(
            id = MERGED_ID,
            displayName = "Shahgee Fusion (Merged)",
            shortBadge = "FUSION",
            family = "Merged Ensemble",
            googleApiModelId = "gemini-3.8-flash",
            localModelTag = "qwen2.5 + dolphin + gemma3",
            roleSummary = "Merges Qwen 2.5 logic, Dolphin fluency, Gemma 3 clarity & Gemini grounding.",
            strengths = "Best overall accuracy, code & natural voice",
            temperature = 0.55,
            topP = 0.9,
            isMergedEnsemble = true
        ),
        StudioModelProfile(
            id = GEMMA_3_ID,
            displayName = "Gemma 3 (27B)",
            shortBadge = "GEMMA 3",
            family = "Google Gemma",
            googleApiModelId = "gemma-3-27b-it",
            localModelTag = "gemma3",
            roleSummary = "Crisp instruction following, concise synthesis & multilingual clarity.",
            strengths = "Fast distillation & clean explanations",
            temperature = 0.5,
            topP = 0.9
        ),
        StudioModelProfile(
            id = DOLPHIN_29_ID,
            displayName = "Dolphin 2.9",
            shortBadge = "DOLPHIN",
            family = "Dolphin / Llama-3",
            googleApiModelId = null,
            localModelTag = "dolphin-llama3",
            roleSummary = "Fluid conversational companion, brainstorming & script ideation.",
            strengths = "Creative tone & natural dialogue",
            temperature = 0.72,
            topP = 0.92
        ),
        StudioModelProfile(
            id = QWEN_25_ID,
            displayName = "Qwen 2.5",
            shortBadge = "QWEN 2.5",
            family = "Alibaba Qwen 2.5",
            googleApiModelId = null,
            localModelTag = "qwen2.5",
            roleSummary = "Deep structured reasoning, math, Kotlin/Python engineering & planning.",
            strengths = "Code, math & step-by-step plans",
            temperature = 0.35,
            topP = 0.85
        ),
        StudioModelProfile(
            id = GEMINI_FLASH_ID,
            displayName = "Gemini 3.8 Flash",
            shortBadge = "GEMINI",
            family = "Google Gemini",
            googleApiModelId = "gemini-3.8-flash",
            localModelTag = "gemini-3.8-flash",
            roleSummary = "Flagship high-speed cloud synthesis and ensemble arbiter.",
            strengths = "Fast cloud grounding & synthesis",
            temperature = 0.6,
            topP = 0.9
        )
    )

    val mergeableMembers: List<StudioModelProfile> = all.filter { !it.isMergedEnsemble }

    fun findById(id: String): StudioModelProfile =
        all.firstOrNull { it.id == id } ?: all.first()

    fun resolveMergeMembers(selectedMemberIds: Set<String>): List<StudioModelProfile> {
        val resolved = mergeableMembers.filter { it.id in selectedMemberIds }
        return resolved.ifEmpty { mergeableMembers }
    }

    fun formatMergeLabel(selectedMemberIds: Set<String>): String {
        val names = resolveMergeMembers(selectedMemberIds).joinToString(" + ") { it.shortBadge }
        return "FUSION ($names)"
    }
}
