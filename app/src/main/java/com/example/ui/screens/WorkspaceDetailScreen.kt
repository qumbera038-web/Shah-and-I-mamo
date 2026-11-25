package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.AiResponse
import com.example.data.local.BrainConfig
import com.example.data.local.MemoryEntity
import com.example.data.local.StudioSettings
import com.example.domain.ShahgeeConfig
import com.example.domain.ShahgeeModelCatalog
import com.example.domain.WorkspaceKind
import com.example.domain.WorkspaceModule
import com.example.ui.AiUiState
import com.example.ui.components.AiNoticeBanner
import com.example.ui.components.AiResponseCard
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioMint
import com.example.ui.theme.StudioNight
import com.example.ui.theme.StudioNightTop
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailScreen(
    module: WorkspaceModule,
    aiState: AiUiState,
    savedBanner: String?,
    memories: List<MemoryEntity>,
    settings: StudioSettings,
    brainConfig: BrainConfig,
    discoveredLocalModels: List<String>,
    localServerStatus: String?,
    isScanningLocalServer: Boolean,
    isSpeechAvailable: Boolean,
    isLiveGeminiEnabled: Boolean,
    onBack: () -> Unit,
    onSpeakClick: () -> Unit,
    onSubmitPrompt: (String) -> Unit,
    onReadAloud: (String) -> Unit,
    onSaveResponse: (AiResponse) -> Unit,
    onAddMemory: (String, String, String) -> Unit,
    onTogglePin: (MemoryEntity) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetHologramPulse: (Boolean) -> Unit,
    onSetPreferredLocale: (String) -> Unit,
    onSaveBrainDirective: (String) -> Unit,
    onResetBrainDirective: () -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleMergedMember: (String) -> Unit,
    onSaveLocalServerUrl: (String) -> Unit,
    onScanLocalServer: (String) -> Unit,
    onPinStudioToHome: () -> Unit,
    onPinChatToHome: () -> Unit,
    onClearSavedBanner: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddMemoryDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedBanner) {
        if (!savedBanner.isNullOrBlank()) {
            snackbarHostState.showSnackbar(savedBanner)
            onClearSavedBanner()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioNight,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "${module.shortTag} • ${module.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioAmber
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("workspace_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Studio",
                            tint = StudioCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioNightTop
                )
            )
        },
        floatingActionButton = {
            if (module.kind == WorkspaceKind.MEMORY_VAULT) {
                FloatingActionButton(
                    onClick = { showAddMemoryDialog = true },
                    containerColor = StudioCyan,
                    contentColor = StudioNight,
                    modifier = Modifier.testTag("add_memory_fab")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add memory note"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(StudioNightTop, StudioNight)))
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                when (module.kind) {
                    WorkspaceKind.AI_INTERACTIVE -> {
                        InteractiveWorkspaceContent(
                            module = module,
                            aiState = aiState,
                            brainConfig = brainConfig,
                            settings = settings,
                            discoveredLocalModels = discoveredLocalModels,
                            localServerStatus = localServerStatus,
                            isScanningLocalServer = isScanningLocalServer,
                            isLiveGeminiEnabled = isLiveGeminiEnabled,
                            onSpeakClick = onSpeakClick,
                            onSubmitPrompt = onSubmitPrompt,
                            onReadAloud = onReadAloud,
                            onSaveResponse = onSaveResponse,
                            onSaveBrainDirective = onSaveBrainDirective,
                            onResetBrainDirective = onResetBrainDirective,
                            onSelectModel = onSelectModel,
                            onToggleMergedMember = onToggleMergedMember,
                            onSaveLocalServerUrl = onSaveLocalServerUrl,
                            onScanLocalServer = onScanLocalServer
                        )
                    }
                    WorkspaceKind.MEMORY_VAULT -> {
                        MemoryVaultContent(
                            memories = memories,
                            onTogglePin = onTogglePin,
                            onDeleteMemory = onDeleteMemory
                        )
                    }
                    WorkspaceKind.SETTINGS -> {
                        SettingsWorkspaceContent(
                            settings = settings,
                            isSpeechAvailable = isSpeechAvailable,
                            isLiveGeminiEnabled = isLiveGeminiEnabled,
                            onSetAutoSpeak = onSetAutoSpeak,
                            onSetHologramPulse = onSetHologramPulse,
                            onSetPreferredLocale = onSetPreferredLocale,
                            onPinStudioToHome = onPinStudioToHome,
                            onPinChatToHome = onPinChatToHome
                        )
                    }
                }
            }
        }
    }

    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onConfirm = { title, content, category ->
                onAddMemory(title, content, category)
                showAddMemoryDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveWorkspaceContent(
    module: WorkspaceModule,
    aiState: AiUiState,
    brainConfig: BrainConfig,
    settings: StudioSettings,
    discoveredLocalModels: List<String>,
    localServerStatus: String?,
    isScanningLocalServer: Boolean,
    isLiveGeminiEnabled: Boolean,
    onSpeakClick: () -> Unit,
    onSubmitPrompt: (String) -> Unit,
    onReadAloud: (String) -> Unit,
    onSaveResponse: (AiResponse) -> Unit,
    onSaveBrainDirective: (String) -> Unit,
    onResetBrainDirective: () -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleMergedMember: (String) -> Unit,
    onSaveLocalServerUrl: (String) -> Unit,
    onScanLocalServer: (String) -> Unit
) {
    var prompt by rememberSaveable(module.id) { mutableStateOf("") }
    val isAnyLiveEngine = isLiveGeminiEnabled || settings.localServerUrl.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                border = BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = module.icon,
                            contentDescription = null,
                            tint = StudioCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Workspace Brief",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary
                        )
                    }
                    Text(
                        text = module.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioNight.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, StudioBorder.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = StudioMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = module.safetyNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (module.id == "ai_brain") {
            item {
                ModelMergeStudioCard(
                    settings = settings,
                    discoveredLocalModels = discoveredLocalModels,
                    localServerStatus = localServerStatus,
                    isScanningLocalServer = isScanningLocalServer,
                    onSelectModel = onSelectModel,
                    onToggleMergedMember = onToggleMergedMember,
                    onSaveLocalServerUrl = onSaveLocalServerUrl,
                    onScanLocalServer = onScanLocalServer
                )
            }
            item {
                BrainConfigManifestCard(
                    brainConfig = brainConfig,
                    customDirective = settings.customBrainDirective,
                    onSaveDirective = onSaveBrainDirective,
                    onResetDirective = onResetBrainDirective
                )
            }
        }

        if (!isAnyLiveEngine) {
            item {
                AiNoticeBanner()
            }
        }

        if (module.quickPrompts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK PROMPTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioAmber
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        module.quickPrompts.forEachIndexed { index, suggestion ->
                            AssistChip(
                                onClick = {
                                    prompt = suggestion
                                    onSubmitPrompt(suggestion)
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("quick_prompt_$index"),
                                label = {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioTextPrimary
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = StudioCardElevated
                                ),
                                border = BorderStroke(1.dp, StudioBorder)
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_prompt_input"),
                    label = { Text("Ask Shahgee inside ${module.shortTag}") },
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioCard,
                        unfocusedContainerColor = StudioCard,
                        focusedBorderColor = StudioCyan,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSpeakClick,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("workspace_voice_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_speak))
                    }

                    Button(
                        onClick = {
                            if (prompt.isNotBlank()) {
                                onSubmitPrompt(prompt)
                            }
                        },
                        enabled = prompt.isNotBlank() && aiState !is AiUiState.Loading,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("workspace_run_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (aiState is AiUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = StudioNight
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Thinking...")
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Run in Workspace")
                        }
                    }
                }
            }
        }

        item {
            when (aiState) {
                is AiUiState.Success -> {
                    AiResponseCard(
                        response = aiState.response,
                        onReadAloud = { onReadAloud(aiState.response.content) },
                        onSaveToMemory = { onSaveResponse(aiState.response) }
                    )
                }
                is AiUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = aiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelMergeStudioCard(
    settings: StudioSettings,
    discoveredLocalModels: List<String>,
    localServerStatus: String?,
    isScanningLocalServer: Boolean,
    onSelectModel: (String) -> Unit,
    onToggleMergedMember: (String) -> Unit,
    onSaveLocalServerUrl: (String) -> Unit,
    onScanLocalServer: (String) -> Unit
) {
    var serverUrlDraft by remember(settings.localServerUrl) {
        mutableStateOf(settings.localServerUrl)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_merge_studio_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
        border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = StudioAmber
                    )
                    Text(
                        text = "INSTALLED & MERGED MODELS",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioAmber
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioMint.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${ShahgeeModelCatalog.all.size} PROFILES",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioMint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Gemma 3, Dolphin 2.9, Qwen 2.5 & Gemini are configured. Tap any model to run solo, or select Shahgee Fusion to merge the best strengths of all active models.",
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShahgeeModelCatalog.all.forEach { profile ->
                    val isSelected = profile.id == settings.selectedModelId
                    Surface(
                        onClick = { onSelectModel(profile.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) StudioCyan.copy(alpha = 0.14f) else StudioCard,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) StudioCyan else StudioBorder.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("model_profile_card_${profile.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = profile.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) StudioCyan else StudioTextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (profile.isMergedEnsemble) {
                                            StudioAmber.copy(alpha = 0.2f)
                                        } else {
                                            StudioNight.copy(alpha = 0.7f)
                                        }
                                    ) {
                                        Text(
                                            text = profile.shortBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (profile.isMergedEnsemble) StudioAmber else StudioTextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = profile.roleSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTextSecondary
                                )
                                Text(
                                    text = "Tag: ${profile.googleApiModelId ?: profile.localModelTag} • ${profile.strengths}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioTextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected model",
                                    tint = StudioCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Hub,
                        contentDescription = null,
                        tint = StudioCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SHAHGEE FUSION MIX (MODELS TO MERGE)",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioCyan
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShahgeeModelCatalog.mergeableMembers.forEach { member ->
                        val included = member.id in settings.mergedMemberIds
                        FilterChip(
                            selected = included,
                            onClick = { onToggleMergedMember(member.id) },
                            label = { Text(member.displayName) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("merge_member_chip_${member.id}"),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioCard,
                                labelColor = StudioTextSecondary,
                                selectedContainerColor = StudioAmber.copy(alpha = 0.18f),
                                selectedLabelColor = StudioAmber
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LOCAL OLLAMA / OPENAI BRIDGE (OPTIONAL)",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioAmber
                )
                OutlinedTextField(
                    value = serverUrlDraft,
                    onValueChange = { serverUrlDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_server_url_input"),
                    label = { Text("Local/LAN Endpoint URL") },
                    placeholder = { Text("http://127.0.0.1:11434", color = StudioTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioCard,
                        unfocusedContainerColor = StudioCard,
                        focusedBorderColor = StudioCyan,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSaveLocalServerUrl(serverUrlDraft) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_local_server_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save URL")
                    }
                    Button(
                        onClick = { onScanLocalServer(serverUrlDraft) },
                        enabled = !isScanningLocalServer,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("scan_local_models_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isScanningLocalServer) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = StudioNight
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Scanning...")
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Scan Local Models")
                        }
                    }
                }
                if (!localServerStatus.isNullOrBlank()) {
                    Text(
                        text = localServerStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (discoveredLocalModels.isNotEmpty()) StudioMint else StudioTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BrainConfigManifestCard(
    brainConfig: BrainConfig,
    customDirective: String,
    onSaveDirective: (String) -> Unit,
    onResetDirective: () -> Unit
) {
    val activeValue = customDirective.ifBlank { brainConfig.defaultDirective }
    var draftDirective by remember(activeValue) { mutableStateOf(activeValue) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("brain_manifest_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
        border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = StudioCyan
                    )
                    Text(
                        text = "BRAIN.MD CONFIGURATION",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioCyan
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioMint.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "AUTO-LOADED",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioMint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "${brainConfig.brainName} • ${brainConfig.brainModels}",
                style = MaterialTheme.typography.titleSmall,
                color = StudioTextPrimary
            )
            Text(
                text = "Gemini Model: ${ShahgeeConfig.defaultPersona.modelId} (temp ${ShahgeeConfig.defaultPersona.temperature})",
                style = MaterialTheme.typography.bodySmall,
                color = StudioAmber
            )
            Text(
                text = "Persona Traits: ${ShahgeeConfig.defaultPersona.behavioralTraits.joinToString(" • ")}",
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary
            )

            OutlinedTextField(
                value = draftDirective,
                onValueChange = { draftDirective = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("brain_directive_input"),
                label = { Text("Active Brain Directive") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StudioCard,
                    unfocusedContainerColor = StudioCard,
                    focusedBorderColor = StudioCyan,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        draftDirective = brainConfig.defaultDirective
                        onResetDirective()
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("reset_brain_directive_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Default")
                }
                Button(
                    onClick = { onSaveDirective(draftDirective) },
                    enabled = draftDirective.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("save_brain_directive_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Brain Directive")
                }
            }

            if (brainConfig.dependencies.isNotEmpty()) {
                Text(
                    text = "CONFIGURED DEPENDENCIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioAmber
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    brainConfig.dependencies.forEach { dep ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StudioCard,
                            border = BorderStroke(1.dp, StudioBorder.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${dep.layer} • ${dep.library}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioTextPrimary
                                    )
                                    Text(
                                        text = dep.purpose,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioTextSecondary
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = StudioMint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = dep.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StudioMint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryVaultContent(
    memories: List<MemoryEntity>,
    onTogglePin: (MemoryEntity) -> Unit,
    onDeleteMemory: (Long) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filtered = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) {
            memories
        } else {
            memories.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_input"),
                placeholder = { Text("Search stored knowledge & briefs...", color = StudioTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = StudioCyan
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StudioCard,
                    unfocusedContainerColor = StudioCard,
                    focusedBorderColor = StudioCyan,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                )
            )
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                    border = BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No matching memory entries",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "Tap + to add a local knowledge note or save Shahgee outputs from any workspace.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isPinned) StudioCardElevated else StudioCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (item.isPinned) StudioAmber.copy(alpha = 0.55f) else StudioBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_item_${item.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioCyan
                                )
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StudioTextPrimary
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onTogglePin(item) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = if (item.isPinned) "Unpin note" else "Pin note",
                                        tint = if (item.isPinned) StudioAmber else StudioTextMuted
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteMemory(item.id) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete note",
                                        tint = StudioTextMuted
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsWorkspaceContent(
    settings: StudioSettings,
    isSpeechAvailable: Boolean,
    isLiveGeminiEnabled: Boolean,
    onSetAutoSpeak: (Boolean) -> Unit,
    onSetHologramPulse: (Boolean) -> Unit,
    onSetPreferredLocale: (String) -> Unit,
    onPinStudioToHome: () -> Unit,
    onPinChatToHome: () -> Unit
) {
    val locales = listOf(
        "en-US" to "English (US)",
        "en-GB" to "English (UK)",
        "ur-PK" to "Urdu (Pakistan)"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.45f)),
                modifier = Modifier.testTag("home_screen_install_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddToHomeScreen,
                            contentDescription = null,
                            tint = StudioCyan
                        )
                        Text(
                            text = "MOBILE HOME SCREEN INSTALL",
                            style = MaterialTheme.typography.labelMedium,
                            color = StudioCyan
                        )
                    }
                    Text(
                        text = "Pin ALiRaZa Studio or a direct Shahgee Chat icon permanently onto your mobile home screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPinStudioToHome,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("settings_pin_studio_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pin Studio Icon")
                        }
                        OutlinedButton(
                            onClick = onPinChatToHome,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("settings_pin_chat_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pin Shahgee Chat")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                border = BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeModelProfile = ShahgeeModelCatalog.findById(settings.selectedModelId)
                    val hasLocal = settings.localServerUrl.isNotBlank()
                    Text(
                        text = "AI ENGINE STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioAmber
                    )
                    Text(
                        text = "Active Profile: ${activeModelProfile.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioCyan
                    )
                    Text(
                        text = when {
                            isLiveGeminiEnabled && hasLocal ->
                                "Cloud (Gemini + Gemma 3) & Local Server (${settings.localServerUrl}) Connected"
                            isLiveGeminiEnabled ->
                                "Google AI REST Client: Connected via Secrets (Gemini + Gemma 3)"
                            hasLocal ->
                                "Local Inference Server: ${settings.localServerUrl}"
                            else ->
                                "Sample Preview Mode: Set GEMINI_API_KEY in Secrets or configure Ollama URL in AI Brain"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLiveGeminiEnabled || hasLocal) StudioMint else StudioTextSecondary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                border = BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "VOICE & HOLOGRAM",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioAmber
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-speak Shahgee replies",
                                style = MaterialTheme.typography.titleSmall,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "Use Android Text-to-Speech after responses arrive",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.autoSpeakReplies,
                            onCheckedChange = onSetAutoSpeak,
                            modifier = Modifier.testTag("switch_auto_speak")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hologram pulse ring",
                                style = MaterialTheme.typography.titleSmall,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "Ambient orbital animation around Shahgee core",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.hologramPulseEnabled,
                            onCheckedChange = onSetHologramPulse,
                            modifier = Modifier.testTag("switch_hologram_pulse")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Voice & TTS Locale",
                            style = MaterialTheme.typography.titleSmall,
                            color = StudioTextPrimary
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            locales.forEach { (tag, label) ->
                                FilterChip(
                                    selected = settings.preferredLocaleTag == tag,
                                    onClick = { onSetPreferredLocale(tag) },
                                    label = { Text(label) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = StudioCyan
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isSpeechAvailable) {
                            "Device SpeechRecognizer service: Available"
                        } else {
                            "Device SpeechRecognizer service: Not available on this device/emulator (text input remains active)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Knowledge") }
    var content by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioCard,
        title = { Text("Add to Memory Vault", color = StudioTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notes / Insight") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, content, category) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
