package com.example.ui.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ai.AiResponse
import com.example.domain.WorkspaceModule
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioNight
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun ShahgeeHologramCard(
    isSpeaking: Boolean,
    pulseEnabled: Boolean,
    activeModelLabel: String = "Shahgee Fusion (Merged)",
    onSpeakClick: () -> Unit,
    onVoiceTestClick: () -> Unit,
    onStopVoiceClick: () -> Unit,
    onOpenChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shahgee_pulse")
    val haloScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isSpeaking) 1.07f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 650 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val effectiveScale = if (pulseEnabled || isSpeaking) haloScale else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shahgee_hologram_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF142B40)),
        border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(124.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(122.dp)
                        .scale(effectiveScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    StudioCyan.copy(alpha = if (isSpeaking) 0.45f else 0.25f),
                                    Color(0xFF173C55),
                                    StudioNight
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.img_shahgee_hologram_1795295913720),
                    contentDescription = "Shahgee hologram emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(StudioCyan, StudioAmber)),
                            shape = CircleShape
                        )
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.shahgee_name),
                color = StudioTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.shahgee_tagline),
                color = StudioCyan,
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = StudioAmber.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("active_model_pill")
            ) {
                Text(
                    text = activeModelLabel,
                    color = StudioAmber,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("speak_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_speak),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isSpeaking) {
                    OutlinedButton(
                        onClick = onStopVoiceClick,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("stop_voice_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StudioAmber),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioAmber),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_stop_voice))
                    }
                } else {
                    OutlinedButton(
                        onClick = onVoiceTestClick,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("voice_test_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StudioBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioTextPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = StudioAmber,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_voice_test))
                    }
                }

                OutlinedButton(
                    onClick = onOpenChatClick,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("open_shahgee_chat_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioCyan),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Chat")
                }
            }
        }
    }
}

@Composable
fun AiNoticeBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firebase_ai_notice_banner"),
        shape = RoundedCornerShape(14.dp),
        color = StudioAmber.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Sample AI mode notice",
                tint = StudioAmber,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Sample Preview Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = StudioAmber
                )
                Text(
                    text = "Set GEMINI_API_KEY in Secrets (for Gemini & Gemma 3) or add a local Ollama/OpenAI URL in AI Brain (for Gemma, Dolphin & Qwen 2.5).",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )
            }
        }
    }
}

@Composable
fun AiResponseCard(
    response: AiResponse,
    onReadAloud: () -> Unit,
    onSaveToMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (response.isSampleMode) {
            AiNoticeBanner()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_response_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
            border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = response.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioCyan,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = StudioAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (response.isSampleMode) "${response.modelBadge} • SAMPLE" else response.modelBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = response.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onReadAloud,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("read_aloud_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_read_aloud))
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = onSaveToMemory,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("save_memory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkAdd,
                            contentDescription = null,
                            tint = StudioAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_save_memory),
                            color = StudioAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceModuleCard(
    module: WorkspaceModule,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .testTag("workspace_card_${module.id}"),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioCardElevated else Color(0xFF14263A)
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) StudioCyan.copy(alpha = 0.7f) else StudioBorder.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (isSelected) StudioAmber else StudioCyan).copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = "${module.title} icon",
                        tint = if (isSelected) StudioAmber else StudioCyan,
                        modifier = Modifier
                            .padding(7.dp)
                            .size(18.dp)
                    )
                }
                Text(
                    text = module.shortTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioAmber
                )
            }

            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = module.title,
                    color = StudioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = module.cardLabel,
                    color = StudioTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
