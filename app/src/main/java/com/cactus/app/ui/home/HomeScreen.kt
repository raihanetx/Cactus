package com.cactus.app.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cactus.app.data.model.Video
import com.cactus.app.data.repository.VideoFilter
import com.cactus.app.ui.theme.CactusColors
import com.cactus.app.util.FileSize

@Composable
fun HomeScreen(
    onVideoClick: (Video) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val anyGranted = grants.values.any { it }
        val activity = context.findActivity()
        val canStillAsk = activity?.let { act ->
            viewModel.requiredPermissions().any { p -> act.shouldShowRequestPermissionRationale(p) }
        } ?: true
        viewModel.onPermissionResult(anyGranted, canStillAsk)
    }

    LaunchedEffect(uiState.permissionState) {
        if (uiState.permissionState == PermissionState.UNKNOWN) {
            permissionLauncher.launch(viewModel.requiredPermissions())
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME &&
                uiState.permissionState == PermissionState.GRANTED &&
                !uiState.isScanning
            ) {
                viewModel.scanMediaStore()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Filter definitions matching the HTML design
    val filters = listOf(
        VideoFilter.NEW to "All",
        VideoFilter.LARGE_TO_SMALL to "Size: High to Low",
        VideoFilter.SMALL_TO_LARGE to "Size: Low to High",
        VideoFilter.WITH_SUBTITLES to "Subtitle: Yes",
        VideoFilter.WITHOUT_SUBTITLES to "Subtitle: No"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CactusColors.White)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered wrapper matching max-w-[440px] style
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
        ) {
            // ── 1. Header Section ──
            HeaderSection(onSettingsClick = onSettingsClick)

            Spacer(Modifier.height(16.dp))

            // ── 2. Search Bar ──
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                showFilters = showFilters,
                onToggleFilters = { showFilters = !showFilters }
            )

            Spacer(Modifier.height(16.dp))

            // ── 3. Collapsible Filter Drawer ──
            if (showFilters) {
                FilterDrawer(
                    filters = filters,
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.setSelectedFilter(it) }
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── 4. Content Area ──
            when {
                uiState.permissionState == PermissionState.UNKNOWN -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = CactusColors.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                uiState.permissionState == PermissionState.PERMANENTLY_DENIED -> {
                    PermissionDeniedView(
                        onOpenSettings = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                }

                uiState.permissionState == PermissionState.DENIED -> {
                    PermissionDeniedView(
                        message = "Media access needed",
                        subMessage = "Grant access to browse your video library",
                        onAction = { permissionLauncher.launch(viewModel.requiredPermissions()) },
                        actionLabel = "Grant access"
                    )
                }

                uiState.permissionState == PermissionState.GRANTED && uiState.isScanning -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = CactusColors.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Scanning...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CactusColors.Neutral700
                            )
                        }
                    }
                }

                uiState.permissionState == PermissionState.GRANTED && videos.isEmpty() -> {
                    EmptyStateView(onRescan = {
                        viewModel.scanMediaStore()
                        Toast.makeText(context, "Rescanning...", Toast.LENGTH_SHORT).show()
                    })
                }

                else -> {
                    // Video list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(videos, key = { it.id }) { video ->
                            VideoCard(
                                video = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Header Section
// ─────────────────────────────────────────────
@Composable
private fun HeaderSection(onSettingsClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                "Word Forward",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CactusColors.Black,
                letterSpacing = (-0.5).sp,
                lineHeight = 24.sp
            )
            Text(
                "Learn naturally by listening",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = CactusColors.Neutral500,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Notification button with dot badge
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Notification action */ }
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = CactusColors.Neutral600,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CactusColors.Black, CircleShape)
                        .border(2.dp, CactusColors.White, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                )
            }

            // Settings button
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSettingsClick
                    )
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = CactusColors.Neutral600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
    ) {
        // Search icon
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = CactusColors.Neutral400,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
        )

        // Text field
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = CactusColors.Neutral900
            ),
            cursorBrush = SolidColor(CactusColors.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 36.dp)
                .background(
                    CactusColors.Neutral50,
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, CactusColors.Neutral200, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        "Search dialogues...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CactusColors.Neutral400
                    )
                }
                inner()
            }
        )

        // Clear button or filter toggle
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Clear",
                tint = CactusColors.Neutral500,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onQueryChange("") }
                    )
            )
        } else {
            Icon(
                Icons.Filled.Tune,
                contentDescription = "Toggle filters",
                tint = if (showFilters) CactusColors.Black else CactusColors.Neutral500,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleFilters
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────
// Filter Drawer (Horizontal scroll)
// ─────────────────────────────────────────────
@Composable
private fun FilterDrawer(
    filters: List<Pair<VideoFilter, String>>,
    selectedFilter: VideoFilter,
    onFilterSelected: (VideoFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (filter, label) ->
            val isActive = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) CactusColors.Black else CactusColors.White
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) CactusColors.Black else CactusColors.Neutral200,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isActive) CactusColors.White else CactusColors.Black
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Video Card (matches HTML edu-card design)
// ─────────────────────────────────────────────
@Composable
private fun VideoCard(
    video: Video,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "cardScale"
    )

    val subtitleLabel = when {
        video.subtitleStatus == com.cactus.app.data.model.SubtitleStatus.READY -> "Subtitle: Yes"
        video.subtitleStatus == com.cactus.app.data.model.SubtitleStatus.FAILED -> "Subtitle: Failed"
        else -> "Subtitle: No"
    }
    val subtitleColor = when {
        video.subtitleStatus == com.cactus.app.data.model.SubtitleStatus.READY -> CactusColors.Neutral500
        video.subtitleStatus == com.cactus.app.data.model.SubtitleStatus.FAILED -> CactusColors.Red500
        else -> CactusColors.Neutral500
    }

    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isHovered) CactusColors.Black else CactusColors.Neutral200,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Music note icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CactusColors.Neutral100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = CactusColors.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Title and metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    video.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CactusColors.Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        FileSize.format(video.sizeBytes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CactusColors.Neutral500
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(CactusColors.Neutral300, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        subtitleLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = subtitleColor
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Duration and caret
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                val sec = (video.durationMs / 1000).toInt()
                Text(
                    "%02d:%02d".format(sec / 60, sec % 60),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CactusColors.Neutral500
                )
                Spacer(Modifier.height(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CactusColors.Neutral300,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Permission Denied View
// ─────────────────────────────────────────────
@Composable
private fun PermissionDeniedView(
    message: String = "Manual permission needed",
    subMessage: String = "Open app settings → Permissions → enable Video and audio",
    onAction: () -> Unit = {},
    actionLabel: String = "Open settings",
    onOpenSettings: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(CactusColors.Neutral100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicVideo,
                contentDescription = null,
                tint = if (onOpenSettings != null) CactusColors.Red500 else CactusColors.Neutral400,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CactusColors.Neutral800
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subMessage,
            fontSize = 12.sp,
            color = CactusColors.Neutral500,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onOpenSettings?.invoke() ?: onAction() },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = CactusColors.Black,
                contentColor = CactusColors.White
            )
        ) {
            Icon(
                if (onOpenSettings != null) Icons.Filled.Settings else Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (onOpenSettings != null) actionLabel else actionLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Empty State View
// ─────────────────────────────────────────────
@Composable
private fun EmptyStateView(onRescan: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(CactusColors.Neutral100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicVideo,
                contentDescription = null,
                tint = CactusColors.Neutral400,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No videos found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CactusColors.Neutral800
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRescan,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = CactusColors.Black,
                contentColor = CactusColors.White
            )
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Rescan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Helper: find Activity from Context
// ─────────────────────────────────────────────
private fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
