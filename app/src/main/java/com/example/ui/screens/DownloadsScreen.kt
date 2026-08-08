package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSegmentedControl
import com.example.ui.components.PlatformBadge
import com.example.ui.components.PlatformIcon
import com.example.ui.dialogs.VideoActionBottomSheet
import com.example.ui.util.StringResources
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allVideos by viewModel.allVideos.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    var selectedMediaTab by remember { mutableStateOf(0) } // 0: All, 1: Videos, 2: Audio
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatformFilter by remember { mutableStateOf("All") }
    var activeSheetVideo by remember { mutableStateOf<VideoEntity?>(null) }

    val platformOptions = listOf("All", "Instagram", "TikTok", "Facebook", "YouTube", "Twitter", "Pinterest")

    val filteredVideos = remember(allVideos, searchQuery, selectedPlatformFilter, selectedMediaTab) {
        allVideos.filter { video ->
            val matchesSearch = video.title.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = if (selectedPlatformFilter == "All") true
            else video.platform.equals(selectedPlatformFilter, ignoreCase = true)

            val matchesTab = when (selectedMediaTab) {
                1 -> video.formatLabel.equals("MP4", ignoreCase = true)
                2 -> video.formatLabel.equals("MP3", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesPlatform && matchesTab
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = StringResources.getString("all_downloads", language),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segmented Control Filter [ All ] [ Videos ] [ Audio ]
        GlassSegmentedControl(
            items = listOf(
                StringResources.getString("filter_all", language),
                StringResources.getString("filter_videos", language),
                StringResources.getString("filter_audio", language)
            ),
            selectedIndex = selectedMediaTab,
            onItemSelected = { selectedMediaTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Glass Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = StringResources.getString("search_hint", language),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Platform Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(platformOptions) { platform ->
                FilterChip(
                    selected = selectedPlatformFilter == platform,
                    onClick = { selectedPlatformFilter = platform },
                    label = { Text(text = platform, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = if (platform != "All") {
                        { PlatformIcon(platformStr = platform, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedPlatformFilter == platform,
                        borderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }
        }

        // Downloads List
        if (filteredVideos.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = StringResources.getString("no_downloads_yet", language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = StringResources.getString("no_downloads_sub", language),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredVideos, key = { it.id }) { video ->
                    DownloadGlassCardItem(
                        video = video,
                        onClick = { activeSheetVideo = video },
                        onPlay = { viewModel.playVideo(video) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    activeSheetVideo?.let { video ->
        VideoActionBottomSheet(
            video = video,
            language = language,
            onDismiss = { activeSheetVideo = null },
            onPlay = { viewModel.playVideo(video) },
            onShare = { viewModel.shareVideo(context, video) },
            onDelete = { viewModel.showDeleteConfirmation(video) }
        )
    }
}

@Composable
fun DownloadGlassCardItem(
    video: VideoEntity,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val dateFormatted = remember(video.downloadTimestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(video.downloadTimestamp))
    }
    val sizeMb = remember(video.fileSizeBytes) {
        String.format("%.1f MB", video.fileSizeBytes / (1024f * 1024f))
    }

    GlassCard(
        modifier = Modifier.clickable { onClick() },
        backgroundColor = Color(0xFF0055C4).copy(alpha = 0.18f),
        borderColor = Color(0xFF007AFF).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = Color.White
                )
                Text(
                    text = "$sizeMb • $dateFormatted • Completed ✓",
                    fontSize = 11.sp,
                    color = Color(0xFF34C759)
                )
            }

            IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

