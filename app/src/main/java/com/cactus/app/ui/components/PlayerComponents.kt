package com.cactus.app.ui.components
import androidx.compose.animation.core.animateDpAsState; import androidx.compose.animation.core.tween
import androidx.compose.foundation.background; import androidx.compose.foundation.clickable; import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.KeyboardArrowDown; import androidx.compose.material.icons.filled.Pause; import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip; import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import com.cactus.app.ui.theme.CactusColors
@Composable
fun PlayerDock(progressFraction: Float, currentTimeLabel: String, totalTimeLabel: String, centerButtonLabel: String, isPlaying: Boolean, showPlayPauseIcon: Boolean = true, onCenterClick: () -> Unit, onSeekBack: () -> Unit = {}, onSeekForward: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().background(CactusColors.White).padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(currentTimeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, modifier = Modifier.width(32.dp))
            Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50)).background(CactusColors.Neutral300)) { Box(modifier = Modifier.fillMaxWidth(progressFraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(CactusColors.Black)) }
            Text(totalTimeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text("-15s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSeekBack).padding(horizontal = 12.dp, vertical = 8.dp))
            Spacer(Modifier.width(24.dp))
            Button(onClick = onCenterClick, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = CactusColors.Black, contentColor = CactusColors.White), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)) { if (showPlayPauseIcon) { Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(8.dp)) }; Text(centerButtonLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(24.dp))
            Text("+15s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSeekForward).padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}
@Composable
fun PaginationDots(activeIndex: Int, total: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(CactusColors.White).padding(top = 8.dp, bottom = 12.dp)) {
        repeat(total) { i -> val isActive = i == activeIndex; val width by animateDpAsState(if (isActive) 16.dp else 6.dp, tween(300), label = "dot-$i"); Box(modifier = Modifier.width(width).height(6.dp).clip(RoundedCornerShape(50)).background(if (isActive) CactusColors.Black else CactusColors.Neutral300)) }
    }
}
@Composable
fun PlayerHeader(title: String, subtitle: String, onDismiss: () -> Unit, rightAction: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        IconButton(onClick = onDismiss) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Dismiss", tint = CactusColors.Neutral500, modifier = Modifier.size(28.dp)) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA3A3A3), letterSpacing = 1.2.sp); Text(subtitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CactusColors.Black, modifier = Modifier.padding(top = 2.dp)) }
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { rightAction() }
    }
}
