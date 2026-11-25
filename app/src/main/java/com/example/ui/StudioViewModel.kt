package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.AiResponse
import com.example.data.local.BrainConfig
import com.example.data.local.BrainConfigRepository
import com.example.data.local.MemoryEntity
import com.example.data.local.MemoryRepository
import com.example.data.local.SettingsRepository
import com.example.data.local.StudioSettings
import com.example.domain.ShahgeeModelCatalog
import com.example.domain.WorkspaceCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val response: AiResponse) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class ShahgeeChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val isSample: Boolean = false,
    val isError: Boolean = false,
    val modelBadge: String = "FUSION",
    val timestampMillis: Long = System.currentTimeMillis()
)

class StudioViewModel(
    private val aiRepository: AiRepository,
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository,
    private val brainConfigRepository: BrainConfigRepository
) : ViewModel() {

    private val _selectedTitle = MutableStateFlow("Dashboard")
    val selectedTitle: StateFlow<String> = _selectedTitle.asStateFlow()

    private val _statusText = MutableStateFlow("Shahgee Fusion ready (Gemma 3 + Dolphin + Qwen 2.5).")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _savedBanner = MutableStateFlow<String?>(null)
    val savedBanner: StateFlow<String?> = _savedBanner.asStateFlow()

    private val _brainConfig = MutableStateFlow(BrainConfig())
    val brainConfig: StateFlow<BrainConfig> = _brainConfig.asStateFlow()

    private val _discoveredLocalModels = MutableStateFlow<List<String>>(emptyList())
    val discoveredLocalModels: StateFlow<List<String>> = _discoveredLocalModels.asStateFlow()

    private val _localServerStatus = MutableStateFlow<String?>(null)
    val localServerStatus: StateFlow<String?> = _localServerStatus.asStateFlow()

    private val _isScanningLocalServer = MutableStateFlow(false)
    val isScanningLocalServer: StateFlow<Boolean> = _isScanningLocalServer.asStateFlow()

    private var nextMessageId = 1L

    private fun initialGreetingMessage(): ShahgeeChatMessage = ShahgeeChatMessage(
        id = nextMessageId++,
        isUser = false,
        text = "Hello! I am Shahgee Fusion — combining Gemma 3, Dolphin 2.9, Qwen 2.5, and Gemini. Switch models anytime above or tap Speak to talk.",
        isSample = false,
        modelBadge = "FUSION"
    )

    private val _chatMessages = MutableStateFlow(listOf(initialGreetingMessage()))
    val chatMessages: StateFlow<List<ShahgeeChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>> = memoryRepository.memories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val settings: StateFlow<StudioSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StudioSettings()
        )

    val isLiveGeminiEnabled: Boolean
        get() = aiRepository.isLiveGeminiEnabled

    init {
        viewModelScope.launch {
            memoryRepository.ensureStarterNotes()
            val loadedBrain = brainConfigRepository.loadBrainConfig()
            _brainConfig.value = loadedBrain
        }
    }

    fun selectWorkspace(title: String) {
        _selectedTitle.value = title
        _statusText.value = "$title workspace selected."
    }

    fun updateStatus(message: String) {
        _statusText.value = message
    }

    fun clearSavedBanner() {
        _savedBanner.value = null
    }

    fun showBanner(message: String) {
        _savedBanner.value = message
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedModelId(modelId)
            val profile = ShahgeeModelCatalog.findById(modelId)
            _statusText.value = "Active model switched to ${profile.displayName}."
            _savedBanner.value = "Active Brain Model: ${profile.displayName}"
        }
    }

    fun toggleMergedMember(modelId: String) {
        viewModelScope.launch {
            val current = settingsRepository.settings.first().mergedMemberIds.toMutableSet()
            if (modelId in current) {
                if (current.size <= 2) {
                    _savedBanner.value = "Keep at least 2 models selected for Shahgee Fusion merge."
                    return@launch
                }
                current.remove(modelId)
            } else {
                current.add(modelId)
            }
            settingsRepository.setMergedMemberIds(current)
            _savedBanner.value = "Updated Fusion Mix (${current.size} models)."
        }
    }

    fun saveLocalServerUrl(url: String) {
        viewModelScope.launch {
            val cleaned = url.trim().trimEnd('/')
            settingsRepository.setLocalServerUrl(cleaned)
            if (cleaned.isBlank()) {
                _discoveredLocalModels.value = emptyList()
                _localServerStatus.value = "Local server URL cleared."
                _savedBanner.value = "Local server disconnected."
            } else {
                _localServerStatus.value = "Saved endpoint: $cleaned"
                _savedBanner.value = "Saved local model server URL."
            }
        }
    }

    fun scanLocalServer(url: String) {
        val cleaned = url.trim().trimEnd('/')
        if (cleaned.isBlank()) {
            _localServerStatus.value = "Enter an Ollama/OpenAI URL (e.g., http://127.0.0.1:11434)."
            return
        }
        viewModelScope.launch {
            _isScanningLocalServer.value = true
            _localServerStatus.value = "Scanning $cleaned..."
            settingsRepository.setLocalServerUrl(cleaned)
            aiRepository.discoverLocalModels(cleaned)
                .onSuccess { models ->
                    _discoveredLocalModels.value = models
                    _localServerStatus.value = if (models.isEmpty()) {
                        "Connected to $cleaned, no pulled models found yet."
                    } else {
                        "Connected • Found ${models.size} model(s): ${models.joinToString(", ")}"
                    }
                }
                .onFailure { error ->
                    _discoveredLocalModels.value = emptyList()
                    _localServerStatus.value = "Scan failed: ${error.localizedMessage ?: "unreachable"}"
                }
            _isScanningLocalServer.value = false
        }
    }

    fun askShahgee(
        prompt: String,
        workspaceId: String? = null,
        onAutoSpeak: (String) -> Unit = {}
    ) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val currentBrain = _brainConfig.value
            val currentSettings = settingsRepository.settings.first()
            val activeDirective = currentSettings.customBrainDirective.ifBlank {
                currentBrain.defaultDirective
            }
            val workspaceTitle = workspaceId?.let { WorkspaceCatalog.findById(it)?.title }
                ?: currentBrain.brainName
            val activeProfile = ShahgeeModelCatalog.findById(currentSettings.selectedModelId)

            _statusText.value = "Running ${activeProfile.shortBadge} in $workspaceTitle..."
            _aiState.value = AiUiState.Loading
            try {
                val response = aiRepository.askShahgee(
                    prompt = trimmed,
                    workspaceId = workspaceId,
                    brainConfig = currentBrain,
                    activeDirective = activeDirective,
                    selectedModelId = currentSettings.selectedModelId,
                    mergedMemberIds = currentSettings.mergedMemberIds,
                    localServerUrl = currentSettings.localServerUrl
                )
                _aiState.value = AiUiState.Success(response)
                _statusText.value = if (response.isSampleMode) {
                    "Sample response ready (${response.modelBadge}) in $workspaceTitle."
                } else {
                    "${response.modelBadge} response ready in $workspaceTitle."
                }
                if (currentSettings.autoSpeakReplies) {
                    onAutoSpeak(response.content)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(e.localizedMessage ?: "Unable to process request.")
                _statusText.value = "Request failed."
            }
        }
    }

    fun sendChatMessage(
        prompt: String,
        onAutoSpeak: (String) -> Unit = {}
    ) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isChatGenerating.value) return

        val userMsg = ShahgeeChatMessage(
            id = nextMessageId++,
            isUser = true,
            text = trimmed
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatGenerating.value = true

        viewModelScope.launch {
            val currentBrain = _brainConfig.value
            val currentSettings = settingsRepository.settings.first()
            val activeDirective = currentSettings.customBrainDirective.ifBlank {
                currentBrain.defaultDirective
            }
            try {
                val response = aiRepository.askShahgee(
                    prompt = trimmed,
                    workspaceId = null,
                    brainConfig = currentBrain,
                    activeDirective = activeDirective,
                    selectedModelId = currentSettings.selectedModelId,
                    mergedMemberIds = currentSettings.mergedMemberIds,
                    localServerUrl = currentSettings.localServerUrl
                )
                val replyMsg = ShahgeeChatMessage(
                    id = nextMessageId++,
                    isUser = false,
                    text = response.content,
                    isSample = response.isSampleMode,
                    modelBadge = response.modelBadge
                )
                _chatMessages.value = _chatMessages.value + replyMsg
                if (currentSettings.autoSpeakReplies) {
                    onAutoSpeak(response.content)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ShahgeeChatMessage(
                    id = nextMessageId++,
                    isUser = false,
                    text = e.localizedMessage ?: "Unable to reach AI backend.",
                    isError = true,
                    modelBadge = "ERROR"
                )
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    fun saveChatMessageToMemory(message: ShahgeeChatMessage) {
        if (message.text.isBlank()) return
        viewModelScope.launch {
            memoryRepository.addMemory(
                title = "Shahgee (${message.modelBadge}) Note",
                content = message.text,
                workspaceId = "ai_brain",
                category = "Shahgee Chat"
            )
            _savedBanner.value = "Saved Shahgee message to Memory Vault."
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(initialGreetingMessage())
    }

    fun saveResponseToMemory(response: AiResponse) {
        viewModelScope.launch {
            val workspace = response.workspaceId?.let { WorkspaceCatalog.findById(it) }
            memoryRepository.addMemory(
                title = response.title,
                content = response.content,
                workspaceId = response.workspaceId ?: "ai_brain",
                category = workspace?.category ?: "Shahgee"
            )
            _savedBanner.value = "Saved \"${response.title}\" to Memory & Knowledge Vault."
        }
    }

    fun addMemory(title: String, content: String, category: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            memoryRepository.addMemory(
                title = title,
                content = content,
                workspaceId = "memory_knowledge",
                category = category.ifBlank { "Note" }
            )
            _savedBanner.value = "Added entry to Memory Vault."
        }
    }

    fun toggleMemoryPin(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryRepository.togglePinned(memory)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }

    fun setAutoSpeak(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSpeak(enabled) }
    }

    fun setHologramPulse(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHologramPulse(enabled) }
    }

    fun setPreferredLocale(localeTag: String) {
        viewModelScope.launch { settingsRepository.setPreferredLocale(localeTag) }
    }

    fun saveBrainDirective(directive: String) {
        viewModelScope.launch {
            settingsRepository.setCustomBrainDirective(directive)
            _savedBanner.value = "Brain directive updated."
        }
    }

    fun resetBrainDirectiveToDefault() {
        viewModelScope.launch {
            settingsRepository.setCustomBrainDirective("")
            _savedBanner.value = "Brain directive reset to brain.md default."
        }
    }

    class Factory(
        private val aiRepository: AiRepository,
        private val memoryRepository: MemoryRepository,
        private val settingsRepository: SettingsRepository,
        private val brainConfigRepository: BrainConfigRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudioViewModel(
                aiRepository = aiRepository,
                memoryRepository = memoryRepository,
                settingsRepository = settingsRepository,
                brainConfigRepository = brainConfigRepository
            ) as T
        }
    }
}
