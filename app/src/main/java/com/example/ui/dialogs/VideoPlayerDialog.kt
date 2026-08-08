package com.example.ui.dialogs

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoEntity
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.iOSBlueGradientEnd
import com.example.ui.theme.iOSBlueGradientStart
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerDialog(
    video: VideoEntity,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeModeIndex by remember { mutableStateOf(0) } // 0: FIT, 1: FILL, 2: ZOOM

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    )

    val resizeLabels = listOf("Fit", "Fill", "Zoom")

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val file = File(video.localFilePath)
            val uri = if (file.exists()) Uri.fromFile(file) else Uri.parse(video.originalUrl)
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Position ticker coroutine
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
            delay(300)
        }
    }

    // Auto-hide controls after delay
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) {
                // ExoPlayer Surface
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = resizeModes[resizeModeIndex]
                        }
                    },
                    update = { view ->
                        view.resizeMode = resizeModes[resizeModeIndex]
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Buffering Spinner
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }

                // Controls Overlay
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        // Top Bar Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlatformBadge(platformStr = video.platform)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Resize Mode",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = onShare) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        // Center Playback Controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            // Rewind 10s
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White
                                )
                            }

                            // Big Play/Pause Button
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Forward 10s
                            IconButton(
                                onClick = {
                                    val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                    exoPlayer.seekTo(newPos)
                                    currentPosition = newPos
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White
                                )
                            }
                        }

                        // Bottom Controls: Time slider, Speed, Mute
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                        ) {
                            // Timeline Progress Slider
                            val sliderMax = if (totalDuration > 0) totalDuration.toFloat() else 1f
                            val sliderPos = currentPosition.toFloat().coerceIn(0f, sliderMax)

                            Slider(
                                value = sliderPos,
                                onValueChange = { value ->
                                    currentPosition = value.toLong()
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo(currentPosition)
                                },
                                valueRange = 0f..sliderMax,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${formatTime(currentPosition)} / ${formatTime(totalDuration)}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Speed Toggle Chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable {
                                                playbackSpeed = when (playbackSpeed) {
                                                    1.0f -> 1.25f
                                                    1.25f -> 1.5f
                                                    1.5f -> 2.0f
                                                    2.0f -> 0.5f
                                                    else -> 1.0f
                                                }
                                                exoPlayer.setPlaybackSpeed(playbackSpeed)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${playbackSpeed}x",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Mute / Unmute
                                    IconButton(
                                        onClick = {
                                            isMuted = !isMuted
                                            exoPlayer.volume = if (isMuted) 0f else 1f
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                            contentDescription = "Mute Toggle",
                                            tint = Color.White
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

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
