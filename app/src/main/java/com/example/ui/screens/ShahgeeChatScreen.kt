package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.ShahgeeConfig
import com.example.domain.ShahgeeModelCatalog
import com.example.domain.StudioModelProfile
import com.example.ui.ShahgeeChatMessage
import com.example.ui.components.AiNoticeBanner
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioAmberDim
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDim
import com.example.ui.theme.StudioMint
import com.example.ui.theme.StudioNight
import com.example.ui.theme.StudioNightTop
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShahgeeChatScreen(
    messages: List<ShahgeeChatMessage>,
    isGenerating: Boolean,
    isLiveGeminiEnabled: Boolean,
    hasLocalServer: Boolean,
    selectedModelId: String,
    mergedMemberIds: Set<String>,
    isSpeechAvailable: Boolean,
    preferredLocaleTag: String,
    savedBanner: String?,
    onBack: () -> Unit,
    onSelectModel: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSpeakInputClick: () -> Unit,
    onReadAloud: (String) -> Unit,
    onSaveMessageToMemory: (ShahgeeChatMessage) -> Unit,
    onClearChat: () -> Unit,
    onClearSavedBanner: () -> Unit
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var isVoiceInputMode by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeProfile = ShahgeeModelCatalog.findById(selectedModelId)
    val isAnyLiveEngine = isLiveGeminiEnabled || hasLocalServer

    LaunchedEffect(messages.size, isGenerating) {
        val targetIndex = if (isGenerating) messages.size else (messages.size - 1)
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(savedBanner) {
        if (!savedBanner.isNullOrBlank()) {
            snackbarHostState.showSnackbar(savedBanner)
            onClearSavedBanner()
        }
    }

    val starterPrompts = remember {
        listOf(
            "Summarize my ALiRaZa Studio V200 workspaces",
            "Draft a focused daily engineering plan",
            "Explain Shahgee's safety confirmation gates"
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = StudioNight,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_shahgee_hologram_1795295913720),
                            contentDescription = "Shahgee avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, StudioCyan, CircleShape)
                        )
                        Column {
                            Text(
                                text = ShahgeeConfig.defaultPersona.assistantName.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = buildString {
                                    append(
                                        if (activeProfile.isMergedEnsemble) {
                                            "Fusion (${mergedMemberIds.size} merged)"
                                        } else {
                                            activeProfile.displayName
                                        }
                                    )
                                    append(" • ")
                                    append(if (isAnyLiveEngine) "LIVE" else "Sample")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAnyLiveEngine) StudioMint else StudioAmber
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = StudioCyan
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("chat_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear conversation",
                            tint = StudioTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioNightTop)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(StudioNightTop, StudioNight)))
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_model_switcher"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ShahgeeModelCatalog.all, key = { it.id }) { model ->
                        val isSelected = model.id == selectedModelId
                        val visual = resolveModelVisualIdentity(model.shortBadge, isError = false)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectModel(model.id) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("chat_model_chip_${model.id}"),
                            leadingIcon = {
                                Icon(
                                    imageVector = visual.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) visual.accentColor else StudioTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (model.isMergedEnsemble) "Fusion (All)" else model.shortBadge,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = StudioCard,
                                labelColor = StudioTextSecondary,
                                selectedContainerColor = visual.accentColor.copy(alpha = 0.18f),
                                selectedLabelColor = visual.accentColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = StudioBorder,
                                selectedBorderColor = visual.accentColor.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                if (!isAnyLiveEngine) {
                    AiNoticeBanner(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("shahgee_chat_message_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onReadAloud = { onReadAloud(msg.text) },
                            onSaveToMemory = { onSaveMessageToMemory(msg) }
                        )
                    }

                    if (messages.size <= 1) {
                        item {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "SUGGESTED STARTERS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioAmber
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    starterPrompts.forEachIndexed { idx, prompt ->
                                        AssistChip(
                                            onClick = { onSendMessage(prompt) },
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("chat_starter_$idx"),
                                            label = {
                                                Text(
                                                    text = prompt,
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

                    if (isGenerating) {
                        item(key = "typing_indicator") {
                            ShahgeeTypingIndicator(activeProfile = activeProfile)
                        }
                    }
                }

                Surface(
                    color = StudioNightTop.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, StudioBorder.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InputModeToggleRow(
                                isVoiceMode = isVoiceInputMode,
                                onSelectMode = { isVoiceInputMode = it }
                            )
                            Text(
                                text = if (isVoiceInputMode) {
                                    "MIC • $preferredLocaleTag"
                                } else {
                                    "TEXT MODE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isVoiceInputMode) StudioCyan else StudioTextMuted
                            )
                        }

                        AnimatedContent(
                            targetState = isVoiceInputMode,
                            label = "chat_input_mode_content"
                        ) { voiceMode ->
                            if (voiceMode) {
                                VoiceInputPanel(
                                    isGenerating = isGenerating,
                                    isSpeechAvailable = isSpeechAvailable,
                                    onSpeakClick = onSpeakInputClick,
                                    onSwitchToText = { isVoiceInputMode = false }
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("shahgee_chat_input"),
                                        placeholder = {
                                            Text(
                                                text = "Message Shahgee...",
                                                color = StudioTextMuted,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        maxLines = 4,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = StudioCard,
                                            unfocusedContainerColor = StudioCard,
                                            focusedBorderColor = StudioCyan,
                                            unfocusedBorderColor = StudioBorder,
                                            focusedTextColor = StudioTextPrimary,
                                            unfocusedTextColor = StudioTextPrimary
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (inputText.isNotBlank() && !isGenerating) {
                                                    onSendMessage(inputText)
                                                    inputText = ""
                                                }
                                            }
                                        )
                                    )

                                    FilledIconButton(
                                        onClick = {
                                            if (inputText.isNotBlank()) {
                                                onSendMessage(inputText)
                                                inputText = ""
                                            }
                                        },
                                        enabled = inputText.isNotBlank() && !isGenerating,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .testTag("shahgee_chat_send_button"),
                                        shape = RoundedCornerShape(15.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = StudioCyan,
                                            contentColor = StudioNight
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = stringResource(R.string.action_send)
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
}

@Composable
private fun InputModeToggleRow(
    isVoiceMode: Boolean,
    onSelectMode: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioCard,
        border = BorderStroke(1.dp, StudioBorder.copy(alpha = 0.7f)),
        modifier = Modifier.testTag("chat_input_mode_toggle")
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                onClick = { onSelectMode(false) },
                shape = RoundedCornerShape(11.dp),
                color = if (!isVoiceMode) StudioCyan.copy(alpha = 0.2f) else StudioCard,
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("mode_toggle_text")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Keyboard,
                        contentDescription = "Text input mode",
                        tint = if (!isVoiceMode) StudioCyan else StudioTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Text",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!isVoiceMode) StudioCyan else StudioTextSecondary
                    )
                }
            }

            Surface(
                onClick = { onSelectMode(true) },
                shape = RoundedCornerShape(11.dp),
                color = if (isVoiceMode) StudioCyan.copy(alpha = 0.2f) else StudioCard,
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("mode_toggle_voice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Voice input mode",
                        tint = if (isVoiceMode) StudioCyan else StudioTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isVoiceMode) StudioCyan else StudioTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceInputPanel(
    isGenerating: Boolean,
    isSpeechAvailable: Boolean,
    onSpeakClick: () -> Unit,
    onSwitchToText: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "voice_mic_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_scale"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = StudioCard,
        border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_input_panel")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSpeakClick,
                enabled = !isGenerating,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioCyan,
                    contentColor = StudioNight
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .scale(if (!isGenerating && isSpeechAvailable) pulseScale else 1.0f)
                    .testTag("shahgee_voice_capture_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (!isSpeechAvailable) {
                        "Speech Service Unavailable"
                    } else if (isGenerating) {
                        "Shahgee Responding..."
                    } else {
                        "Tap to Speak to Shahgee"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!isSpeechAvailable) {
                TextButton(
                    onClick = onSwitchToText,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Use Text", color = StudioAmber)
                }
            }
        }
    }
}

private data class ModelVisualIdentity(
    val accentColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector,
    val displayBadge: String
)

private val DolphinBlue = Color(0xFF60A5FA)
private val QwenViolet = Color(0xFFC084FC)

private fun resolveModelVisualIdentity(
    modelBadge: String,
    isError: Boolean
): ModelVisualIdentity {
    if (isError) {
        return ModelVisualIdentity(
            accentColor = StudioCoral,
            secondaryColor = StudioCoral,
            icon = Icons.Rounded.ErrorOutline,
            displayBadge = "ERROR"
        )
    }

    val normalized = modelBadge.uppercase()
    return when {
        normalized.contains("FUSION") -> ModelVisualIdentity(
            accentColor = StudioAmber,
            secondaryColor = StudioCyan,
            icon = Icons.Rounded.AutoAwesome,
            displayBadge = modelBadge
        )
        normalized.contains("GEMMA") -> ModelVisualIdentity(
            accentColor = StudioMint,
            secondaryColor = StudioCyan,
            icon = Icons.Rounded.Bolt,
            displayBadge = "GEMMA 3"
        )
        normalized.contains("DOLPHIN") -> ModelVisualIdentity(
            accentColor = DolphinBlue,
            secondaryColor = StudioCyan,
            icon = Icons.Rounded.Waves,
            displayBadge = "DOLPHIN 2.9"
        )
        normalized.contains("QWEN") -> ModelVisualIdentity(
            accentColor = QwenViolet,
            secondaryColor = StudioCyan,
            icon = Icons.Rounded.Code,
            displayBadge = "QWEN 2.5"
        )
        normalized.contains("GEMINI") -> ModelVisualIdentity(
            accentColor = StudioCyan,
            secondaryColor = StudioMint,
            icon = Icons.Rounded.Psychology,
            displayBadge = "GEMINI"
        )
        else -> ModelVisualIdentity(
            accentColor = StudioCyan,
            secondaryColor = StudioAmber,
            icon = Icons.Rounded.AutoAwesome,
            displayBadge = modelBadge.ifBlank { "SHAHGEE" }
        )
    }
}

@Composable
private fun ShahgeeAvatarIndicator(
    accentColor: Color = StudioCyan,
    secondaryColor: Color = StudioAmber,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .testTag("shahgee_avatar_indicator")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_shahgee_hologram_1795295913720),
            contentDescription = "Shahgee avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    width = 1.75.dp,
                    brush = Brush.linearGradient(listOf(accentColor, secondaryColor)),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isActive) StudioAmber else accentColor)
                .border(1.5.dp, StudioNight, CircleShape)
        )
    }
}

@Composable
private fun UserAvatarIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(36.dp)
            .testTag("user_avatar_indicator"),
        shape = CircleShape,
        color = StudioCyanDim.copy(alpha = 0.85f),
        border = BorderStroke(1.5.dp, StudioCyan.copy(alpha = 0.8f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "User avatar",
                tint = StudioCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShahgeeTypingIndicator(
    activeProfile: StudioModelProfile
) {
    val visual = resolveModelVisualIdentity(activeProfile.shortBadge, isError = false)
    val transition = rememberInfiniteTransition(label = "shahgee_typing")
    val dotOffsets = List(3) { index ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = -5f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 480,
                    delayMillis = index * 140,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_offset_$index"
        )
    }
    val dotAlphas = List(3) { index ->
        transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 480,
                    delayMillis = index * 140,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha_$index"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shahgee_typing_indicator"),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        ShahgeeAvatarIndicator(
            accentColor = visual.accentColor,
            secondaryColor = visual.secondaryColor,
            isActive = true
        )
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 4.dp,
                bottomEnd = 18.dp
            ),
            color = StudioCardElevated,
            border = BorderStroke(1.dp, visual.accentColor.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = activeProfile.shortBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = visual.accentColor
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .offset(y = dotOffsets[index].value.dp)
                                .alpha(dotAlphas[index].value)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == 1) visual.secondaryColor else visual.accentColor
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ShahgeeChatMessage,
    onReadAloud: () -> Unit,
    onSaveToMemory: () -> Unit
) {
    val formattedTime = remember(message.timestampMillis) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestampMillis))
    }
    val modelVisual = remember(message.modelBadge, message.isError) {
        resolveModelVisualIdentity(message.modelBadge, message.isError)
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (message.isUser) 20.dp else 4.dp,
        bottomEnd = if (message.isUser) 4.dp else 20.dp
    )

    val bubbleBrush = if (message.isUser) {
        Brush.linearGradient(
            colors = listOf(
                StudioCyanDim.copy(alpha = 0.78f),
                StudioCardElevated
            )
        )
    } else if (message.isError) {
        Brush.linearGradient(
            colors = listOf(
                StudioCoral.copy(alpha = 0.16f),
                StudioCard
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                modelVisual.accentColor.copy(alpha = 0.12f),
                StudioCardElevated,
                StudioCard
            )
        )
    }

    val borderColor = when {
        message.isUser -> StudioCyan.copy(alpha = 0.6f)
        message.isError -> StudioCoral.copy(alpha = 0.75f)
        else -> modelVisual.accentColor.copy(alpha = 0.48f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            ShahgeeAvatarIndicator(
                accentColor = modelVisual.accentColor,
                secondaryColor = modelVisual.secondaryColor
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 520.dp)
                .testTag(if (message.isUser) "chat_bubble_user_${message.id}" else "chat_bubble_shahgee_${message.id}"),
            shape = bubbleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = if (message.isUser) 4.dp else 2.dp
        ) {
            Box(
                modifier = Modifier
                    .background(bubbleBrush)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!message.isUser) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 2.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            modelVisual.accentColor.copy(alpha = 0.85f),
                                            modelVisual.secondaryColor.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (message.isUser) "YOU" else "SHAHGEE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isUser) StudioCyan else modelVisual.accentColor
                            )
                            if (!message.isUser) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = modelVisual.accentColor.copy(alpha = 0.16f),
                                    border = BorderStroke(
                                        1.dp,
                                        modelVisual.accentColor.copy(alpha = 0.55f)
                                    ),
                                    modifier = Modifier.testTag("chat_model_badge_${message.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = modelVisual.icon,
                                            contentDescription = "Generated by ${modelVisual.displayBadge}",
                                            tint = modelVisual.accentColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = modelVisual.displayBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = modelVisual.accentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (!message.isUser && message.isSample) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StudioAmberDim.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "SAMPLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StudioAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isUser) StudioTextSecondary else StudioTextMuted,
                            modifier = Modifier.testTag("chat_timestamp_${message.id}")
                        )
                    }

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isError) MaterialTheme.colorScheme.error else StudioTextPrimary
                    )

                    if (!message.isUser && !message.isError) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onReadAloud,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("chat_speak_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = modelVisual.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Speak",
                                    color = modelVisual.accentColor,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            TextButton(
                                onClick = onSaveToMemory,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("chat_save_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdd,
                                    contentDescription = "Save to Memory",
                                    tint = StudioAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Save",
                                    color = StudioAmber,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            UserAvatarIndicator()
        }
    }
}
