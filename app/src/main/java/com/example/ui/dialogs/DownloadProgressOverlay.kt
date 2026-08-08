package com.example.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.service.DownloadState
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources

@Composable
fun DownloadProgressOverlay(
    downloadState: DownloadState,
    language: AppLanguage,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (downloadState is DownloadState.Completed || downloadState is DownloadState.Error) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141720),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (downloadState) {
                    is DownloadState.Progress -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFF007AFF),
                            modifier = Modifier.height(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = StringResources.getString("downloading", language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { downloadState.percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF007AFF),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${downloadState.percent}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007AFF)
                            )
                            Text(
                                text = "${String.format("%.1f", downloadState.speedKbps)} KB/s",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        val downloadedMb = downloadState.downloadedBytes / (1024f * 1024f)
                        val totalMb = downloadState.totalBytes / (1024f * 1024f)
                        Text(
                            text = "${String.format("%.1f", downloadedMb)} MB / ${String.format("%.1f", totalMb)} MB",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = StringResources.getString("cancel", language),
                                color = Color(0xFFFF453A)
                            )
                        }
                    }

                    is DownloadState.Completed -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.height(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = StringResources.getString("download_completed", language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "OK", color = Color.White)
                        }
                    }

                    is DownloadState.Error -> {
                        Text(
                            text = StringResources.getString("unknown_error", language),
                            color = Color(0xFFFF453A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "OK", color = Color.White)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

