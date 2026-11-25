package com.example.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.CandlestickChart
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class WorkspaceKind {
    AI_INTERACTIVE,
    MEMORY_VAULT,
    SETTINGS
}

data class WorkspaceModule(
    val id: String,
    val title: String,
    val shortTag: String,
    val category: String,
    val cardLabel: String,
    val summary: String,
    val safetyNote: String,
    val icon: ImageVector,
    val kind: WorkspaceKind = WorkspaceKind.AI_INTERACTIVE,
    val quickPrompts: List<String>
)

object WorkspaceCatalog {
    val all: List<WorkspaceModule> = listOf(
        WorkspaceModule(
            id = "ai_brain",
            title = "AI Brain · Gemma / Dolphin / Qwen 2.5",
            shortTag = "CORE",
            category = "Model Merge",
            cardLabel = "Gemma 3, Dolphin, Qwen 2.5 & Fusion",
            summary = "Install, bridge, and merge Gemma 3, Dolphin 2.9, Qwen 2.5, and Gemini into Shahgee Fusion.",
            safetyNote = "Review model routing, local server URLs, and system directives before deploying changes.",
            icon = Icons.Rounded.Psychology,
            quickPrompts = listOf(
                "Compare Gemma 3, Dolphin 2.9, and Qwen 2.5 strengths",
                "Design a merged multi-model routing strategy",
                "Audit Shahgee's active brain.md configuration"
            )
        ),
        WorkspaceModule(
            id = "agent_hub",
            title = "Agent & Sub-agent Hub",
            shortTag = "AGENTS",
            category = "Orchestration",
            cardLabel = "Multi-agent goals & handoffs",
            summary = "Decompose goals into specialized sub-agents with clear handoffs and verification gates.",
            safetyNote = "Sensitive tool executions require explicit human approval and audit logs.",
            icon = Icons.Rounded.Hub,
            quickPrompts = listOf(
                "Plan a 3-agent research and verification pipeline",
                "Define sub-agent handoff guardrails",
                "Draft an autonomous task retry policy"
            )
        ),
        WorkspaceModule(
            id = "neural_lab",
            title = "Neural Network Lab",
            shortTag = "NEURAL",
            category = "Architecture",
            cardLabel = "Layers, quantization & benchmarks",
            summary = "Inspect layer topologies, training schedules, quantization notes, and evaluation metrics.",
            safetyNote = "Validate quantized weights on held-out benchmarks before deployment.",
            icon = Icons.Rounded.AccountTree,
            quickPrompts = listOf(
                "Outline 8-bit vs 4-bit quantization trade-offs",
                "Design an evaluation suite for latency and recall",
                "Checklist for preventing overfitting on small datasets"
            )
        ),
        WorkspaceModule(
            id = "code_studio",
            title = "Code Studio",
            shortTag = "DEV",
            category = "Engineering",
            cardLabel = "Kotlin, Python & test blueprints",
            summary = "Draft Kotlin, Python, and Compose architectures with clean refactor and test plans.",
            safetyNote = "Never execute untrusted code automatically; always inspect diffs before merging.",
            icon = Icons.Rounded.Code,
            quickPrompts = listOf(
                "Generate a Kotlin Flow retry helper pattern",
                "Review Jetpack Compose state hoisting best practices",
                "Outline unit tests for a repository layer"
            )
        ),
        WorkspaceModule(
            id = "automation_studio",
            title = "Automation Studio",
            shortTag = "FLOW",
            category = "Workflows",
            cardLabel = "Trigger-action flows & dry runs",
            summary = "Compose trigger-condition-action automations with dry-run previews and rollback steps.",
            safetyNote = "Destructive or external actions must stay gated behind confirmation prompts.",
            icon = Icons.Rounded.AutoFixHigh,
            quickPrompts = listOf(
                "Design a daily local knowledge backup workflow",
                "Create a safe notification triage automation",
                "Outline idempotency rules for scheduled jobs"
            )
        ),
        WorkspaceModule(
            id = "research_web",
            title = "Research & Web",
            shortTag = "BRIEF",
            category = "Synthesis",
            cardLabel = "Source checks & executive briefs",
            summary = "Structure research questions, source verification matrices, and executive briefs.",
            safetyNote = "Cross-check primary sources and timestamps before acting on synthesized summaries.",
            icon = Icons.Rounded.Public,
            quickPrompts = listOf(
                "Create a source credibility verification rubric",
                "Outline a comparative technology landscape brief",
                "Structure a 5-minute executive summary template"
            )
        ),
        WorkspaceModule(
            id = "trading_analysis",
            title = "Trading Analysis",
            shortTag = "RISK",
            category = "Markets",
            cardLabel = "Position sizing & risk checklists",
            summary = "Risk-first market structure notes, position sizing formulas, and discipline journals.",
            safetyNote = "Informational analysis only — never place automated broker orders without explicit manual verification.",
            icon = Icons.Rounded.CandlestickChart,
            quickPrompts = listOf(
                "Calculate a 1% account risk position-sizing checklist",
                "Draft a pre-session market structure review",
                "List emotional bias checks after a losing trade"
            )
        ),
        WorkspaceModule(
            id = "media_studio",
            title = "Audio / Video Studio",
            shortTag = "MEDIA",
            category = "Production",
            cardLabel = "Voice scripts & storyboards",
            summary = "Craft Shahgee voiceover scripts, storyboard beats, pacing cues, and audio mastering checklists.",
            safetyNote = "Verify rights and licensing for all third-party voice, music, and visual assets.",
            icon = Icons.Rounded.VideoLibrary,
            quickPrompts = listOf(
                "Write a 30-second Shahgee intro voiceover script",
                "Build a 6-scene product demo storyboard",
                "Create a podcast audio cleanup checklist"
            )
        ),
        WorkspaceModule(
            id = "biotech_sim",
            title = "Bio-Tech Simulation",
            shortTag = "BIO",
            category = "Science",
            cardLabel = "Assay controls & protocol notes",
            summary = "Hypothesis structuring, literature protocol notes, and assay control checklists.",
            safetyNote = "Conceptual educational planning only — follow certified biosafety protocols for lab work.",
            icon = Icons.Rounded.Biotech,
            quickPrompts = listOf(
                "Outline positive and negative controls for an assay",
                "Structure a literature hypothesis comparison table",
                "List reproducibility metadata fields for experiments"
            )
        ),
        WorkspaceModule(
            id = "human_resources",
            title = "Human Resources",
            shortTag = "PEOPLE",
            category = "Operations",
            cardLabel = "Role rubrics & onboarding plans",
            summary = "Role scorecards, structured interview rubrics, and 30-60-90 day onboarding plans.",
            safetyNote = "Ensure hiring rubrics remain objective, fair, and compliant with privacy standards.",
            icon = Icons.Rounded.Groups,
            quickPrompts = listOf(
                "Draft a senior Android engineer interview rubric",
                "Build a 30-60-90 day onboarding roadmap",
                "Write a clear project ownership charter"
            )
        ),
        WorkspaceModule(
            id = "security_guardian",
            title = "Security Guardian",
            shortTag = "SEC",
            category = "Defense",
            cardLabel = "Permission & secret audits",
            summary = "Least-privilege permission audits, secret hygiene checks, and backup recovery drills.",
            safetyNote = "Never hardcode API keys or bypass user consent for sensitive device permissions.",
            icon = Icons.Rounded.Security,
            quickPrompts = listOf(
                "Audit Android runtime permissions for least privilege",
                "Create a zero-trust API secret storage checklist",
                "Draft an incident response and backup recovery drill"
            )
        ),
        WorkspaceModule(
            id = "files_system",
            title = "Files & System Tools",
            shortTag = "SYS",
            category = "Local Ops",
            cardLabel = "Local directories & manifests",
            summary = "Organize local-first directories, export manifests, and device storage diagnostics.",
            safetyNote = "Create recoverable backups before bulk file reorganization or cleanup.",
            icon = Icons.Rounded.FolderOpen,
            quickPrompts = listOf(
                "Design a clean local-first project folder structure",
                "Create a pre-release APK verification checklist",
                "Outline an exportable JSON backup manifest"
            )
        ),
        WorkspaceModule(
            id = "memory_knowledge",
            title = "Memory & Knowledge",
            shortTag = "VAULT",
            category = "Persistent",
            cardLabel = "Saved notes & pinned briefs",
            summary = "Device-local Room database vault for pinned briefs, Shahgee replies, and study notes.",
            safetyNote = "Stored locally on this device in SQLite via Room. Export important notes regularly.",
            icon = Icons.Rounded.Memory,
            kind = WorkspaceKind.MEMORY_VAULT,
            quickPrompts = emptyList()
        ),
        WorkspaceModule(
            id = "settings",
            title = "Settings",
            shortTag = "CONFIG",
            category = "Studio",
            cardLabel = "Voice, locale & Gemini status",
            summary = "Configure Shahgee TTS voice, speech locale, hologram pulse, and AI engine connection status.",
            safetyNote = "All preferences persist locally via Jetpack DataStore.",
            icon = Icons.Rounded.Settings,
            kind = WorkspaceKind.SETTINGS,
            quickPrompts = emptyList()
        )
    )

    fun findById(id: String): WorkspaceModule? = all.firstOrNull { it.id == id }
}
