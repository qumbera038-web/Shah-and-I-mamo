package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.StudioSettings
import com.example.domain.ShahgeeModelCatalog
import com.example.domain.WorkspaceCatalog
import com.example.domain.WorkspaceModule
import com.example.ui.components.ShahgeeHologramCard
import com.example.ui.components.WorkspaceModuleCard
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioNight
import com.example.ui.theme.StudioNightTop

@Composable
fun DashboardScreen(
    selectedTitle: String,
    statusText: String,
    savedBanner: String?,
    settings: StudioSettings,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onVoiceTestClick: () -> Unit,
    onStopVoiceClick: () -> Unit,
    onOpenChatClick: () -> Unit,
    onPinToHomeClick: () -> Unit,
    onClearSavedBanner: () -> Unit,
    onOpenWorkspace: (WorkspaceModule) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(StudioNightTop, StudioNight)))
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val columnCount = when {
                    maxWidth >= 680.dp -> 4
                    maxWidth >= 480.dp -> 3
                    else -> 2
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.studio_title),
                                color = StudioCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = stringResource(R.string.studio_subtitle),
                                color = StudioAmber,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.status_badge_online),
                                color = StudioCyan,
                                fontSize = 10.sp,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.testTag("online_ui_badge")
                            )
                            IconButton(
                                onClick = onPinToHomeClick,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("pin_home_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddToHomeScreen,
                                    contentDescription = "Pin ALiRaZa Studio to mobile home screen",
                                    tint = StudioAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val activeModelProfile = ShahgeeModelCatalog.findById(settings.selectedModelId)
                    val activeModelLabel = if (activeModelProfile.isMergedEnsemble) {
                        ShahgeeModelCatalog.formatMergeLabel(settings.mergedMemberIds)
                    } else {
                        "${activeModelProfile.displayName} • ACTIVE"
                    }

                    ShahgeeHologramCard(
                        isSpeaking = isSpeaking,
                        pulseEnabled = settings.hologramPulseEnabled,
                        activeModelLabel = activeModelLabel,
                        onSpeakClick = onSpeakClick,
                        onVoiceTestClick = onVoiceTestClick,
                        onStopVoiceClick = onStopVoiceClick,
                        onOpenChatClick = onOpenChatClick
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = selectedTitle.uppercase(),
                        color = StudioAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.testTag("selected_workspace_title")
                    )
                    Text(
                        text = statusText,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("dashboard_status_text")
                    )

                    Spacer(Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("workspace_grid")
                    ) {
                        items(WorkspaceCatalog.all, key = { it.id }) { module ->
                            WorkspaceModuleCard(
                                module = module,
                                isSelected = module.title == selectedTitle,
                                onClick = { onOpenWorkspace(module) }
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.tribute_footer),
                        color = Color.Gray,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
