package com.example.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BrainDependencyStatus(
    val layer: String,
    val library: String,
    val purpose: String,
    val status: String
)

data class BrainInstalledModel(
    val id: String,
    val name: String,
    val tags: String,
    val role: String,
    val status: String
)

data class BrainConfig(
    val brainName: String = "Shahgee Intelligence (V200)",
    val brainModels: String = "Shahgee Fusion (Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini)",
    val persona: String = "Your AI companion • Voice interface",
    val engineMode: String = "Hybrid V200 Fusion (Cloud Gemini/Gemma + Local Ollama/OpenAI Bridge)",
    val defaultDirective: String = "Act as Shahgee, the central AI companion in ALiRaZa AI Studio V200, merging Gemma 3 clarity, Dolphin 2.9 creative fluency, Qwen 2.5 structured engineering logic, and Gemini grounding across all 14 workspaces.",
    val dependencies: List<BrainDependencyStatus> = emptyList(),
    val installedModels: List<BrainInstalledModel> = emptyList(),
    val safetyGates: List<String> = emptyList(),
    val routingMap: Map<String, String> = emptyMap(),
    val rawMarkdown: String = ""
)

class BrainConfigRepository(private val context: Context) {

    suspend fun loadBrainConfig(): BrainConfig = withContext(Dispatchers.IO) {
        val raw = runCatching {
            context.assets.open("brain.md").bufferedReader().use { it.readText() }
        }.getOrDefault("")

        if (raw.isBlank()) {
            return@withContext BrainConfig()
        }

        var brainName = "Shahgee Intelligence (V200)"
        var brainModels = "Shahgee Fusion (Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini)"
        var persona = "Your AI companion • Voice interface"
        var engineMode = "Hybrid V200 Fusion (Cloud Gemini/Gemma + Local Ollama/OpenAI Bridge)"
        var defaultDirective =
            "Act as Shahgee, the central AI companion in ALiRaZa AI Studio V200, merging Gemma 3 clarity, Dolphin 2.9 creative fluency, Qwen 2.5 structured engineering logic, and Gemini grounding across all 14 workspaces."
        val dependencies = mutableListOf<BrainDependencyStatus>()
        val installedModels = mutableListOf<BrainInstalledModel>()
        val safetyGates = mutableListOf<String>()
        val routingMap = mutableMapOf<String, String>()

        var section = ""
        raw.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## 1.") -> section = "identity"
                trimmed.startsWith("## 2.") -> section = "dependencies"
                trimmed.startsWith("## 3.") -> section = "routing"
                trimmed.startsWith("## 4.") -> section = "safety"
                trimmed.startsWith("## 5.") -> section = "models"
                trimmed.startsWith("## ") -> section = ""
            }

            when (section) {
                "identity" -> {
                    when {
                        trimmed.startsWith("- **Brain Name**:") ->
                            brainName = trimmed.substringAfter(":").trim()
                        trimmed.startsWith("- **Brain Models**:") ->
                            brainModels = trimmed.substringAfter(":").trim()
                        trimmed.startsWith("- **Persona**:") ->
                            persona = trimmed.substringAfter(":").trim()
                        trimmed.startsWith("- **Engine Mode**:") ->
                            engineMode = trimmed.substringAfter(":").trim()
                        trimmed.startsWith("- **Active Persona Directive**:") ->
                            defaultDirective = trimmed.substringAfter(":").trim()
                    }
                }
                "dependencies" -> {
                    if (trimmed.startsWith("|") && !trimmed.startsWith("| Layer") && !trimmed.startsWith("| :---")) {
                        val cols = trimmed.trim('|').split('|').map { it.trim() }
                        if (cols.size >= 4) {
                            dependencies.add(
                                BrainDependencyStatus(
                                    layer = cols[0],
                                    library = cols[1].replace("`", ""),
                                    purpose = cols[2],
                                    status = cols[3]
                                )
                            )
                        }
                    }
                }
                "models" -> {
                    if (trimmed.startsWith("|") && !trimmed.startsWith("| Model ID") && !trimmed.startsWith("| :---")) {
                        val cols = trimmed.trim('|').split('|').map { it.trim() }
                        if (cols.size >= 5) {
                            installedModels.add(
                                BrainInstalledModel(
                                    id = cols[0].replace("`", ""),
                                    name = cols[1].replace("**", ""),
                                    tags = cols[2].replace("`", ""),
                                    role = cols[3],
                                    status = cols[4]
                                )
                            )
                        }
                    }
                }
                "routing" -> {
                    if (trimmed.startsWith("- `")) {
                        val key = trimmed.substringAfter("- `").substringBefore("`")
                        val desc = trimmed.substringAfter("`:").trim()
                        if (key.isNotBlank() && desc.isNotBlank()) {
                            routingMap[key] = desc
                        }
                    }
                }
                "safety" -> {
                    if (trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(". ")) {
                        safetyGates.add(trimmed.substringAfter(". ").trim())
                    }
                }
            }
        }

        BrainConfig(
            brainName = brainName,
            brainModels = brainModels,
            persona = persona,
            engineMode = engineMode,
            defaultDirective = defaultDirective,
            dependencies = dependencies,
            installedModels = installedModels,
            safetyGates = safetyGates,
            routingMap = routingMap,
            rawMarkdown = raw
        )
    }
}
