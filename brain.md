# Shahgee Intelligence V200 — Brain Architecture & Configuration (`brain.md`)

> Core AI model architecture, Shahgee configuration parameters, merged multi-model ensemble (**Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini**), and integration dependencies for **ALiRaZa AI Studio V200**.  
> Built in memory of Ali Raza • V200 foundation.

## 1. Brain Identity
- **Brain Name**: Shahgee Intelligence (V200)
- **Brain Models**: Shahgee Fusion (Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini)
- **Persona**: Your AI companion • Voice interface
- **Primary Voice**: Android Speech Input (`RecognizerIntent`) + `TextToSpeech` (`"shahgee"`)
- **Engine Mode**: Hybrid V200 Fusion (Cloud Gemini/Gemma + Local Ollama/OpenAI Bridge)
- **Active Persona Directive**: Act as Shahgee, the central AI companion in ALiRaZa AI Studio V200, merging Gemma 3 clarity, Dolphin 2.9 creative fluency, Qwen 2.5 structured engineering logic, and Gemini grounding across all 14 workspaces.

## 2. Automatic Dependency Configuration
Integration dependencies configured in `app/build.gradle.kts` and `gradle/libs.versions.toml`:

| Layer | Library | Purpose | Status |
| :--- | :--- | :--- | :--- |
| UI & Theme | `androidx.compose:compose-bom` + `material3` | Edge-to-edge dark cyan/amber studio UI | Ready |
| Navigation | `androidx.navigation:navigation-compose` | Type-safe routing across 14 workspace modules | Ready |
| Lifecycle & MVVM | `androidx.lifecycle:lifecycle-viewmodel-compose` | Reactive `StateFlow` state management | Ready |
| Networking & Inference | `com.squareup.okhttp3:okhttp` | Gemini / Gemma REST API & Ollama/OpenAI bridge | Ready |
| Memory & Knowledge | `androidx.room:room-runtime` + `room-ktx` + KSP | Local SQLite vault (`aliraza_studio_v200.db`) | Ready |
| Preferences | `androidx.datastore:datastore-preferences` | Persists active model, merge mix, local endpoint & voice | Ready |
| Concurrency | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Parallel ensemble model calls, TTS & IO | Ready |
| Voice Interface | Android `SpeechRecognizer` + `TextToSpeech` | On-device voice capture & spoken responses | Ready |

## 3. Workspace Routing Rules
- `ai_brain`: **AI Brain · Gemma / Dolphin / Qwen 2.5** — Model stack installer, Shahgee Fusion merge mixer, local Ollama/OpenAI bridge, and `brain.md` manifest.
- `agent_hub`: **Agent & Sub-agent Hub** — Multi-agent orchestration, task decomposition, and handoff gates.
- `neural_lab`: **Neural Network Lab** — Layer topology, quantization trade-offs (8-bit/4-bit), and latency diagnostics.
- `code_studio`: **Code Studio** — Qwen 2.5 & Gemini assisted Kotlin, Python, and Compose architecture drafts with test plans.
- `automation_studio`: **Automation Studio** — Trigger-action automation pipelines with dry-run confirmation gates.
- `research_web`: **Research & Web** — Research briefs, verification matrices, and source synthesis.
- `trading_analysis`: **Trading Analysis** — Risk-first position sizing and market review checklists (no automated orders).
- `media_studio`: **Audio / Video Studio** — Dolphin-assisted voiceover scripts, storyboard beats, and audio production checklists.
- `biotech_sim`: **Bio-Tech Simulation** — Assay control notes, hypotheses, and reproducibility metadata.
- `human_resources`: **Human Resources** — Role scorecards, interview rubrics, and onboarding roadmaps.
- `security_guardian`: **Security Guardian** — Least-privilege permission audits, secret hygiene, and backup drills.
- `files_system`: **Files & System Tools** — Local-first directory layouts, manifests, and APK checklists.
- `memory_knowledge`: **Memory & Knowledge** — Persistent local Room knowledge vault (`memories` table).
- `settings`: **Settings** — Auto-speak toggle, TTS locale selector, hologram pulse, and engine status.

## 4. Safety & Governance Gates
1. Sensitive actions require explicit permission, user confirmation, and audit logging.
2. Never execute untrusted code or financial trading orders automatically.
3. Keep recoverable local backups before modifying stored data or files.
4. Request `RECORD_AUDIO` permission strictly on-demand when the user taps **Speak**.

## 5. Installed & Merged Model Architecture (`Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini`)
Shahgee operates through a layered **Multi-Model Fusion Pipeline** decoupled behind the `AiRepository` interface:

| Model ID | Model Name | Cloud / Local Tag | Merged Role | Status |
| :--- | :--- | :--- | :--- | :--- |
| `shahgee_merged` | **Shahgee Fusion (Merged)** | `gemma-3-27b-it` + `dolphin` + `qwen2.5` + `gemini-3.8-flash` | Combines top strengths of all enabled models into one verified response | Active Default |
| `gemma_3` | **Gemma 3 (27B)** | `gemma-3-27b-it` / `gemma3` | Concise instruction clarity, multilingual polish & distillation | Installed |
| `dolphin_29` | **Dolphin 2.9** | `dolphin-llama3` / `dolphin-2.9` | Natural conversational fluency, creative ideation & persona tone | Installed |
| `qwen_25` | **Qwen 2.5** | `qwen2.5:14b` / `qwen2.5-coder` | Structured logic, math, engineering precision & Kotlin/Python code | Installed |
| `gemini_flash` | **Gemini 3.8 Flash** | `gemini-3.8-flash` | High-speed cloud reasoning & final ensemble synthesis | Installed |

### Ensemble Merge Pipeline
1. **Parallel Specialist Dispatch**: Active member models (`Qwen 2.5`, `Dolphin 2.9`, `Gemma 3`, `Gemini`) analyze the prompt using either a configured local Ollama/OpenAI-compatible server (`http://127.0.0.1:11434`) or authenticated Google Gemini/Gemma REST endpoints (`GEMINI_API_KEY`).
2. **Fusion Synthesis**: Candidate outputs are merged to preserve Qwen 2.5's exact technical steps, Dolphin's natural clarity, and Gemma 3's concise verification.
3. **Memory & Voice Layer**: Final output can be saved directly to the Room `memories` table (`MemoryEntity`) or spoken via Android `TextToSpeech`.

## 6. Shahgee Configuration Parameters
| Parameter | Key / Location | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `brain_name` | `brain.md` (`BrainConfig`) | `Shahgee Intelligence (V200)` | Display identity for the studio brain |
| `model_profiles` | `brain.md` (`BrainConfig`) | `Shahgee Fusion (Gemma 3 + Dolphin 2.9 + Qwen 2.5 + Gemini)` | Merged multi-model ensemble profile |
| `selected_model_id` | DataStore (`shahgee_v200_settings`) | `shahgee_merged` | Active single model or merged ensemble |
| `merged_member_ids` | DataStore (`shahgee_v200_settings`) | `gemma_3,dolphin_29,qwen_25,gemini_flash` | Models participating in Shahgee Fusion |
| `local_server_url` | DataStore (`shahgee_v200_settings`) | `""` (optional Ollama/OpenAI URL) | Local/LAN inference endpoint for GGUF/Ollama models |
| `auto_speak_replies` | DataStore (`shahgee_v200_settings`) | `true` | Automatically reads Shahgee responses via TTS |
| `hologram_pulse_enabled` | DataStore (`shahgee_v200_settings`) | `true` | Enables ambient orbital pulse on Shahgee emblem |
| `preferred_locale_tag` | DataStore (`shahgee_v200_settings`) | `en-US` | Active TTS & SpeechRecognizer locale (`en-US`, `en-GB`, `ur-PK`) |
| `custom_brain_directive` | DataStore (`shahgee_v200_settings`) | `""` (uses `brain.md` default) | User-customized system persona directive |
| `database_name` | `StudioDatabase` (Room v1) | `aliraza_studio_v200.db` | Local SQLite storage for Memory & Knowledge |
