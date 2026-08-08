package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaFormat
import com.example.data.model.PlatformType
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuality
import com.example.data.service.DownloadState
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSegmentedControl
import com.example.ui.components.PlatformBadge
import com.example.ui.components.PlatformIcon
import com.example.ui.theme.FacebookColor
import com.example.ui.theme.InstagramColor
import com.example.ui.theme.PinterestColor
import com.example.ui.theme.TikTokColor
import com.example.ui.theme.TwitterColor
import com.example.ui.theme.YouTubeColor
import com.example.ui.theme.iOSBlueGradientEnd
import com.example.ui.theme.iOSBlueGradientStart
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDownloads: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val urlInput by viewModel.urlInput.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val parsedMetadata by viewModel.parsedMetadata.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedQualityIndex by remember { mutableStateOf(0) } // 0: Auto, 1: 1080p, 2: 720p, 3: 480p
    var selectedFormatIndex by remember { mutableStateOf(0) }  // 0: MP4, 1: MP3

    val detectedPlatform = remember(urlInput) {
        if (urlInput.isNotBlank()) PlatformType.detectFromUrl(urlInput) else null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Top Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SaveFlow",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = StringResources.getString("app_subtitle", language),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Row {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = { viewModel.openSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Hero Greeting Card (Download Box)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF0055C4).copy(alpha = 0.25f),
                borderColor = Color(0xFF007AFF).copy(alpha = 0.8f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = StringResources.getString("hero_title", language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = StringResources.getString("hero_subtitle", language),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Box
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        placeholder = {
                            Text(
                                text = StringResources.getString("input_hint", language),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearInput() }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.pasteFromClipboard(context) }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = Color(0xFF007AFF)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF007AFF),
                            unfocusedBorderColor = Color(0xFF007AFF).copy(alpha = 0.4f),
                            focusedContainerColor = Color(0xFF007AFF).copy(alpha = 0.15f),
                            unfocusedContainerColor = Color(0xFF007AFF).copy(alpha = 0.08f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Detected Platform Indicator Banner
                    if (detectedPlatform != null && detectedPlatform != PlatformType.OTHER) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓ " + StringResources.getString("platform_detected", language) + ":",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF34C759)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PlatformBadge(platformStr = detectedPlatform.displayName)
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = StringResources.getString(errorMessage!!, language),
                            color = Color(0xFFFF453A),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Download Action Button
                    GlassButton(
                        text = if (isAnalyzing) StringResources.getString("analyzing_link", language)
                        else StringResources.getString("download_button", language),
                        icon = if (!isAnalyzing) Icons.Default.Download else null,
                        onClick = { viewModel.analyzeLink() },
                        enabled = !isAnalyzing && urlInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Supported Platforms Section
        item {
            SupportedPlatformsSection(
                language = language,
                onPlatformClick = { platformName ->
                    viewModel.pasteFromClipboard(context)
                }
            )
        }

        // Analyzing Loading Indicator
        if (isAnalyzing) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = StringResources.getString("analyzing_link", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active Download State Overlay
        if (downloadState is DownloadState.Progress || downloadState is DownloadState.Completed) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF0055C4).copy(alpha = 0.3f),
                    borderColor = Color(0xFF007AFF)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val state = downloadState
                        if (state is DownloadState.Progress) {
                            val speedKb = String.format("%.0f KB/s", state.speedKbps)
                            Text(
                                text = StringResources.getString("downloading", language) + " (${state.percent}%)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF007AFF),
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = speedKb, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                TextButton(onClick = { viewModel.cancelDownload() }) {
                                    Text(
                                        text = StringResources.getString("cancel", language),
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF453A)
                                    )
                                }
                            }
                        } else if (state is DownloadState.Completed) {
                            Text(
                                text = StringResources.getString("download_completed", language),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34C759)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            GlassButton(
                                text = StringResources.getString("action_play", language),
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    val completedVideo = VideoEntity(
                                        id = 0,
                                        title = parsedMetadata?.title ?: "Reel Video",
                                        platform = parsedMetadata?.platform?.displayName ?: "Instagram",
                                        originalUrl = parsedMetadata?.originalUrl ?: "",
                                        thumbnailUrl = parsedMetadata?.thumbnailUrl ?: "",
                                        localFilePath = state.localFilePath,
                                        fileSizeBytes = state.fileSizeBytes,
                                        durationText = parsedMetadata?.durationText ?: "0:30",
                                        qualityLabel = "HD"
                                    )
                                    viewModel.playVideo(completedVideo)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Parsed Video Preview & Options Card
        parsedMetadata?.let { metadata ->
            item {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF0055C4).copy(alpha = 0.25f),
                        borderColor = Color(0xFF007AFF).copy(alpha = 0.8f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.DarkGray)
                                        .clickable {
                                            val streamUrl = metadata.availableStreams.firstOrNull()?.downloadUrl ?: metadata.originalUrl
                                            val previewVideo = VideoEntity(
                                                id = 0,
                                                title = metadata.title,
                                                platform = metadata.platform.displayName,
                                                originalUrl = streamUrl,
                                                thumbnailUrl = metadata.thumbnailUrl,
                                                localFilePath = "",
                                                fileSizeBytes = 0L,
                                                durationText = metadata.durationText,
                                                qualityLabel = "Preview"
                                            )
                                            viewModel.playVideo(previewVideo)
                                        }
                                ) {
                                    AsyncImage(
                                        model = metadata.thumbnailUrl,
                                        contentDescription = "Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Reel",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = metadata.durationText,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    PlatformBadge(platformStr = metadata.platform.displayName)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = metadata.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        color = Color.White
                                    )
                                    Text(
                                        text = metadata.authorName,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quality Control
                            Text(
                                text = StringResources.getString("quality_label", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            GlassSegmentedControl(
                                items = listOf(
                                    StringResources.getString("auto_quality", language),
                                    "1080p", "720p", "480p"
                                ),
                                selectedIndex = selectedQualityIndex,
                                onItemSelected = { selectedQualityIndex = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Format Control
                            Text(
                                text = StringResources.getString("format_label", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            GlassSegmentedControl(
                                items = listOf("MP4 (Video)", "MP3 (Audio)"),
                                selectedIndex = selectedFormatIndex,
                                onItemSelected = { selectedFormatIndex = it }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Watch Reel & Download Video Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = StringResources.getString("watch_reel", language),
                                    icon = Icons.Default.PlayArrow,
                                    onClick = {
                                        val streamUrl = metadata.availableStreams.firstOrNull()?.downloadUrl ?: metadata.originalUrl
                                        val previewVideo = VideoEntity(
                                            id = 0,
                                            title = metadata.title,
                                            platform = metadata.platform.displayName,
                                            originalUrl = streamUrl,
                                            thumbnailUrl = metadata.thumbnailUrl,
                                            localFilePath = "",
                                            fileSizeBytes = 0L,
                                            durationText = metadata.durationText,
                                            qualityLabel = "HD"
                                        )
                                        viewModel.playVideo(previewVideo)
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                GlassButton(
                                    text = StringResources.getString("download_video_button", language),
                                    icon = Icons.Default.Download,
                                    onClick = {
                                        val targetStream = metadata.availableStreams.getOrNull(selectedQualityIndex)
                                            ?: metadata.availableStreams.first()
                                        viewModel.startDownloadWithQuality(targetStream)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Downloads Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = StringResources.getString("recent_downloads", language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(onClick = onNavigateToDownloads) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = StringResources.getString("view_all", language),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        if (recentVideos.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = StringResources.getString("no_downloads_yet", language),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = StringResources.getString("no_downloads_sub", language),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(recentVideos.take(5), key = { it.id }) { video ->
                HomeRecentVideoItemCard(
                    video = video,
                    onPlay = { viewModel.playVideo(video) },
                    onShare = { viewModel.shareVideo(context, video) },
                    onDelete = { viewModel.showDeleteConfirmation(video) }
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = StringResources.getString("app_rights", language),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun HomeRecentVideoItemCard(
    video: VideoEntity,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatted = remember(video.downloadTimestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(video.downloadTimestamp))
    }
    val sizeMb = remember(video.fileSizeBytes) {
        String.format("%.1f MB", video.fileSizeBytes / (1024f * 1024f))
    }

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPlay() }
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platformStr = video.platform)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.qualityLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = Color.White
                )
                Text(
                    text = "$sizeMb • $dateFormatted",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Row {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SupportedPlatformsSection(
    language: AppLanguage,
    onPlatformClick: (String) -> Unit
) {
    val platforms = listOf(
        "Instagram" to "Instagram",
        "TikTok" to "TikTok",
        "YouTube" to "YouTube",
        "Facebook" to "Facebook",
        "X" to "X / Twitter",
        "Pinterest" to "Pinterest"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringResources.getString("supported_platforms", language),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = StringResources.getString("supported_platforms_sub", language),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(platforms) { (iconKey, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .clickable { onPlatformClick(label) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformIcon(platformStr = iconKey, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

