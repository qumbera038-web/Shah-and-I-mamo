package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ai.GeminiAiRepository
import com.example.data.ai.GeminiApiClient
import com.example.data.local.BrainConfigRepository
import com.example.data.local.MemoryRepository
import com.example.data.local.SettingsRepository
import com.example.data.local.StudioDatabase
import com.example.domain.WorkspaceCatalog
import com.example.ui.StudioViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ShahgeeChatScreen
import com.example.ui.screens.WorkspaceDetailScreen
import com.example.ui.theme.ALiRaZaStudioTheme
import com.example.util.HomeScreenPinHelper
import com.example.voice.ShahgeeVoiceController
import java.util.Locale

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_SHAHGEE_CHAT = "shahgee_chat"
private const val ROUTE_WORKSPACE = "workspace/{workspaceId}"

class MainActivity : ComponentActivity() {
    private lateinit var voiceController: ShahgeeVoiceController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        voiceController = ShahgeeVoiceController(this)
        val database = StudioDatabase.getInstance(applicationContext)
        val memoryRepository = MemoryRepository(database.memoryDao())
        val settingsRepository = SettingsRepository(applicationContext)
        val brainConfigRepository = BrainConfigRepository(applicationContext)
        val aiRepository = GeminiAiRepository(GeminiApiClient())

        setContent {
            ALiRaZaStudioTheme {
                val studioViewModel: StudioViewModel = viewModel(
                    factory = StudioViewModel.Factory(
                        aiRepository = aiRepository,
                        memoryRepository = memoryRepository,
                        settingsRepository = settingsRepository,
                        brainConfigRepository = brainConfigRepository
                    )
                )

                val selectedTitle by studioViewModel.selectedTitle.collectAsStateWithLifecycle()
                val statusText by studioViewModel.statusText.collectAsStateWithLifecycle()
                val aiState by studioViewModel.aiState.collectAsStateWithLifecycle()
                val chatMessages by studioViewModel.chatMessages.collectAsStateWithLifecycle()
                val isChatGenerating by studioViewModel.isChatGenerating.collectAsStateWithLifecycle()
                val savedBanner by studioViewModel.savedBanner.collectAsStateWithLifecycle()
                val memories by studioViewModel.memories.collectAsStateWithLifecycle()
                val settings by studioViewModel.settings.collectAsStateWithLifecycle()
                val brainConfig by studioViewModel.brainConfig.collectAsStateWithLifecycle()
                val discoveredLocalModels by studioViewModel.discoveredLocalModels.collectAsStateWithLifecycle()
                val localServerStatus by studioViewModel.localServerStatus.collectAsStateWithLifecycle()
                val isScanningLocalServer by studioViewModel.isScanningLocalServer.collectAsStateWithLifecycle()
                val isSpeaking by voiceController.isSpeaking.collectAsStateWithLifecycle()

                LaunchedEffect(settings.preferredLocaleTag) {
                    voiceController.setLocale(settings.preferredLocaleTag)
                }

                var pendingWorkspaceId by remember { mutableStateOf<String?>(null) }
                var pendingVoiceForChat by remember { mutableStateOf(false) }
                val isSpeechAvailable = remember {
                    SpeechRecognizer.isRecognitionAvailable(this@MainActivity)
                }

                val speechLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val phrase = result.data
                            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            ?.firstOrNull()
                        if (!phrase.isNullOrBlank()) {
                            if (pendingVoiceForChat) {
                                studioViewModel.sendChatMessage(
                                    prompt = phrase,
                                    onAutoSpeak = { voiceController.speak(it) }
                                )
                            } else {
                                studioViewModel.askShahgee(
                                    prompt = phrase,
                                    workspaceId = pendingWorkspaceId,
                                    onAutoSpeak = { voiceController.speak(it) }
                                )
                            }
                        } else {
                            studioViewModel.updateStatus("No speech detected. Try again or type a prompt.")
                        }
                    }
                }

                fun launchSpeechRecognition(workspaceId: String?, forChat: Boolean) {
                    pendingWorkspaceId = workspaceId
                    pendingVoiceForChat = forChat
                    if (!SpeechRecognizer.isRecognitionAvailable(this@MainActivity)) {
                        val msg = getString(R.string.speech_unavailable)
                        studioViewModel.updateStatus(msg)
                        voiceController.speak(msg)
                        return
                    }
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE,
                            Locale.forLanguageTag(settings.preferredLocaleTag).toLanguageTag()
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Shahgee")
                    }
                    runCatching { speechLauncher.launch(intent) }
                        .onFailure {
                            studioViewModel.updateStatus(getString(R.string.speech_unavailable))
                        }
                }

                val micPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        launchSpeechRecognition(pendingWorkspaceId, pendingVoiceForChat)
                    } else {
                        studioViewModel.updateStatus(getString(R.string.mic_permission_denied))
                    }
                }

                fun requestVoiceInput(workspaceId: String?, forChat: Boolean = false) {
                    pendingWorkspaceId = workspaceId
                    pendingVoiceForChat = forChat
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasMicPermission) {
                        launchSpeechRecognition(workspaceId, forChat)
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                fun pinStudioToHome() {
                    val requested = HomeScreenPinHelper.pinStudioToHomeScreen(this@MainActivity)
                    studioViewModel.showBanner(
                        if (requested) {
                            "Pin prompt sent for ALiRaZa Studio home screen icon."
                        } else {
                            "Launcher does not support in-app pinning; add ALiRaZa Studio from your app drawer."
                        }
                    )
                }

                fun pinChatToHome() {
                    val requested = HomeScreenPinHelper.pinShahgeeChatToHomeScreen(this@MainActivity)
                    studioViewModel.showBanner(
                        if (requested) {
                            "Pin prompt sent for Shahgee Chat home screen shortcut."
                        } else {
                            "Launcher does not support in-app pinning; open Shahgee Chat from Dashboard."
                        }
                    )
                }

                val navController = rememberNavController()
                val initialRoute = remember {
                    if (intent?.action == HomeScreenPinHelper.ACTION_OPEN_SHAHGEE_CHAT) {
                        ROUTE_SHAHGEE_CHAT
                    } else {
                        ROUTE_DASHBOARD
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = initialRoute
                ) {
                    composable(ROUTE_DASHBOARD) {
                        DashboardScreen(
                            selectedTitle = selectedTitle,
                            statusText = statusText,
                            savedBanner = savedBanner,
                            settings = settings,
                            isSpeaking = isSpeaking,
                            onSpeakClick = {
                                studioViewModel.updateStatus("Listening request sent to Android speech service.")
                                requestVoiceInput(workspaceId = null, forChat = true)
                            },
                            onVoiceTestClick = {
                                val greeting = getString(R.string.speech_greeting)
                                voiceController.speak(greeting)
                                studioViewModel.updateStatus("Shahgee voice greeting started.")
                            },
                            onStopVoiceClick = {
                                voiceController.stop()
                                studioViewModel.updateStatus("Voice playback stopped.")
                            },
                            onOpenChatClick = {
                                navController.navigate(ROUTE_SHAHGEE_CHAT)
                            },
                            onPinToHomeClick = { pinStudioToHome() },
                            onClearSavedBanner = { studioViewModel.clearSavedBanner() },
                            onOpenWorkspace = { module ->
                                studioViewModel.selectWorkspace(module.title)
                                voiceController.speak("${module.title} selected")
                                navController.navigate("workspace/${module.id}")
                            }
                        )
                    }

                    composable(ROUTE_SHAHGEE_CHAT) {
                        ShahgeeChatScreen(
                            messages = chatMessages,
                            isGenerating = isChatGenerating,
                            isLiveGeminiEnabled = studioViewModel.isLiveGeminiEnabled,
                            hasLocalServer = settings.localServerUrl.isNotBlank(),
                            selectedModelId = settings.selectedModelId,
                            mergedMemberIds = settings.mergedMemberIds,
                            isSpeechAvailable = isSpeechAvailable,
                            preferredLocaleTag = settings.preferredLocaleTag,
                            savedBanner = savedBanner,
                            onBack = { navController.popBackStack() },
                            onSelectModel = { studioViewModel.selectModel(it) },
                            onSendMessage = { prompt ->
                                studioViewModel.sendChatMessage(
                                    prompt = prompt,
                                    onAutoSpeak = { voiceController.speak(it) }
                                )
                            },
                            onSpeakInputClick = {
                                requestVoiceInput(workspaceId = null, forChat = true)
                            },
                            onReadAloud = { text -> voiceController.speak(text) },
                            onSaveMessageToMemory = { studioViewModel.saveChatMessageToMemory(it) },
                            onClearChat = { studioViewModel.clearChatHistory() },
                            onClearSavedBanner = { studioViewModel.clearSavedBanner() }
                        )
                    }

                    composable(
                        route = ROUTE_WORKSPACE,
                        arguments = listOf(
                            navArgument("workspaceId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val workspaceId = backStackEntry.arguments?.getString("workspaceId")
                        val module = workspaceId?.let { WorkspaceCatalog.findById(it) }
                            ?: WorkspaceCatalog.all.first()

                        WorkspaceDetailScreen(
                            module = module,
                            aiState = aiState,
                            savedBanner = savedBanner,
                            memories = memories,
                            settings = settings,
                            brainConfig = brainConfig,
                            discoveredLocalModels = discoveredLocalModels,
                            localServerStatus = localServerStatus,
                            isScanningLocalServer = isScanningLocalServer,
                            isSpeechAvailable = isSpeechAvailable,
                            isLiveGeminiEnabled = studioViewModel.isLiveGeminiEnabled,
                            onBack = { navController.popBackStack() },
                            onSpeakClick = { requestVoiceInput(workspaceId = module.id, forChat = false) },
                            onSubmitPrompt = { prompt ->
                                studioViewModel.askShahgee(
                                    prompt = prompt,
                                    workspaceId = module.id,
                                    onAutoSpeak = { voiceController.speak(it) }
                                )
                            },
                            onReadAloud = { text -> voiceController.speak(text) },
                            onSaveResponse = { studioViewModel.saveResponseToMemory(it) },
                            onAddMemory = { title, content, category ->
                                studioViewModel.addMemory(title, content, category)
                            },
                            onTogglePin = { studioViewModel.toggleMemoryPin(it) },
                            onDeleteMemory = { studioViewModel.deleteMemory(it) },
                            onSetAutoSpeak = { studioViewModel.setAutoSpeak(it) },
                            onSetHologramPulse = { studioViewModel.setHologramPulse(it) },
                            onSetPreferredLocale = { studioViewModel.setPreferredLocale(it) },
                            onSaveBrainDirective = { studioViewModel.saveBrainDirective(it) },
                            onResetBrainDirective = { studioViewModel.resetBrainDirectiveToDefault() },
                            onSelectModel = { studioViewModel.selectModel(it) },
                            onToggleMergedMember = { studioViewModel.toggleMergedMember(it) },
                            onSaveLocalServerUrl = { studioViewModel.saveLocalServerUrl(it) },
                            onScanLocalServer = { studioViewModel.scanLocalServer(it) },
                            onPinStudioToHome = { pinStudioToHome() },
                            onPinChatToHome = { pinChatToHome() },
                            onClearSavedBanner = { studioViewModel.clearSavedBanner() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::voiceController.isInitialized) {
            voiceController.shutdown()
        }
        super.onDestroy()
    }
}
