package com.cactus.app.ui.player.slides

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cactus.app.data.model.SubtitleCue
import com.cactus.app.data.model.Video
import com.cactus.app.ui.components.PaginationDots
import com.cactus.app.ui.components.PlayerDock
import com.cactus.app.ui.components.PlayerHeader
import com.cactus.app.ui.player.LocalPlayerHolder
import com.cactus.app.ui.theme.CactusColors
import com.cactus.app.ui.theme.CactusColors.Blue300
import com.cactus.app.ui.theme.CactusColors.Blue600
import com.cactus.app.util.TimeUtils

@Composable
fun DialogueSlide(video: Video?, onDismiss: () -> Unit, viewModel: DialogueViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val holder = LocalPlayerHolder.current
    val playerState by holder.state.collectAsStateWithLifecycle()

    LaunchedEffect(video?.id) { video?.id?.let { viewModel.setActiveVideo(it) } }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val englishCues by viewModel.englishCues.collectAsStateWithLifecycle()
    val banglaCues by viewModel.banglaCues.collectAsStateWithLifecycle()
    val progress by viewModel.pipelineProgress.collectAsStateWithLifecycle()
    val selectedIndices = remember { mutableStateMapOf<Int, Boolean>() }

    val isTranslated = banglaCues.isNotEmpty()
    val isTranslating = uiState.isTranslating
    val centerButtonLabel = when {
        isTranslating -> "${progress?.pct ?: 0}%"
        isTranslated -> if (playerState.isPlaying) "Pause" else "Play"
        else -> "Translate"
    }

    val banglaByIndex = remember(banglaCues) { banglaCues.associateBy { it.cueIndex } }
    val currentCueIndex = remember(playerState.currentPositionMs, englishCues) {
        if (englishCues.isEmpty()) -1
        else englishCues.indexOfFirst { playerState.currentPositionMs >= it.startMs && playerState.currentPositionMs < it.endMs }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(currentCueIndex) { if (currentCueIndex >= 0 && isTranslated) listState.animateScrollToItem(currentCueIndex) }
    val progressFraction = if (playerState.durationMs > 0) (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxSize().background(CactusColors.White)) {
        PlayerHeader(title = "Dialogue", subtitle = "Bilingual Subtitles", onDismiss = onDismiss, rightAction = {
            Icon(Icons.Filled.Language, contentDescription = null, tint = CactusColors.Neutral500, modifier = Modifier.size(20.dp))
        })

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                uiState.errorMessage != null -> {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Translation failed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CactusColors.Red500)
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.errorMessage!!, fontSize = 12.sp, color = CactusColors.Neutral500, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("error", uiState.errorMessage))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = CactusColors.Neutral100, contentColor = CactusColors.Neutral700)) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Copy", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = { video?.let { viewModel.startTranslation(it) } }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = CactusColors.Black, contentColor = CactusColors.White)) {
                                Text("Retry", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                !isTranslated && !isTranslating -> {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(64.dp).clip(RoundedCornerShape(50)).background(CactusColors.Neutral100), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = CactusColors.Neutral400, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("No translation available", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral800)
                        Spacer(Modifier.height(6.dp))
                        Text("Tap Translate to generate bilingual subtitles", fontSize = 12.sp, color = CactusColors.Neutral500, textAlign = TextAlign.Center, modifier = Modifier.width(220.dp))
                    }
                }
                isTranslating -> {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = CactusColors.Black, strokeWidth = 2.dp, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("${progress?.pct ?: 0}%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CactusColors.Black)
                        Spacer(Modifier.height(8.dp))
                        Text(progress?.message ?: "Working...", fontSize = 12.sp, color = CactusColors.Neutral500, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(englishCues.size) { idx ->
                            val enCue = englishCues[idx]
                            val bnCue = banglaByIndex[enCue.cueIndex]
                            val isSelected = selectedIndices[enCue.cueIndex] == true || idx == currentCueIndex
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { selectedIndices[enCue.cueIndex] = !(selectedIndices[enCue.cueIndex] == true) }.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                val mainColor = if (isSelected) CactusColors.Neutral900 else CactusColors.Neutral400
                                val pronColor = if (isSelected) Blue600 else Blue300
                                Text("[${TimeUtils.formatDuration(enCue.startMs)} - ${TimeUtils.formatDuration(enCue.endMs)}] ${enCue.text}${bnCue?.text?.let { " $it" } ?: ""}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = mainColor)
                                if (bnCue?.pronunciation != null) {
                                    Text(bnCue.pronunciation, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = pronColor, fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        PlayerDock(progressFraction = progressFraction, currentTimeLabel = TimeUtils.formatDuration(playerState.currentPositionMs), totalTimeLabel = TimeUtils.formatDuration(playerState.durationMs), centerButtonLabel = centerButtonLabel, isPlaying = playerState.isPlaying, showPlayPauseIcon = isTranslated, onCenterClick = {
            if (!isTranslating && video != null) {
                if (isTranslated) holder.togglePlayPause() else viewModel.startTranslation(video)
            }
        }, onSeekBack = { holder.seekRelative(-15_000L) }, onSeekForward = { holder.seekRelative(15_000L) })
        PaginationDots(activeIndex = 1)
    }
}
