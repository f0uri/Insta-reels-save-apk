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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.components.PlatformBadge
import com.example.ui.util.StringResources
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allVideos by viewModel.allVideos.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    val groupedHistory = remember(allVideos) {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val todayList = mutableListOf<VideoEntity>()
        val yesterdayList = mutableListOf<VideoEntity>()
        val thisWeekList = mutableListOf<VideoEntity>()

        allVideos.forEach { video ->
            val diff = now - video.downloadTimestamp
            when {
                diff < oneDay -> todayList.add(video)
                diff < 2 * oneDay -> yesterdayList.add(video)
                else -> thisWeekList.add(video)
            }
        }
        listOf(
            "time_today" to todayList,
            "time_yesterday" to yesterdayList,
            "time_this_week" to thisWeekList
        ).filter { it.second.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                    text = StringResources.getString("history_title", language),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (allVideos.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
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
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            groupedHistory.forEach { (timeKey, videos) ->
                item {
                    Text(
                        text = StringResources.getString(timeKey, language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(videos, key = { it.id }) { video ->
                    HistoryItemCard(
                        video = video,
                        onPlay = { viewModel.playVideo(video) },
                        onShare = { viewModel.shareVideo(context, video) },
                        onDelete = { viewModel.showDeleteConfirmation(video) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun HistoryItemCard(
    video: VideoEntity,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatted = remember(video.downloadTimestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(video.downloadTimestamp))
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
                    .size(60.dp)
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
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
                        tint = Color.White.copy(0.8f),
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
