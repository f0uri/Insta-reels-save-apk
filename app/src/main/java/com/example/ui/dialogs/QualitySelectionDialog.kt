package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.VideoMetadata
import com.example.data.model.VideoQuality
import com.example.data.model.VideoQualityStream
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources

@Composable
fun QualitySelectionDialog(
    metadata: VideoMetadata,
    language: AppLanguage,
    onSelectQuality: (VideoQualityStream) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141720),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = StringResources.getString("quality_dialog_title", language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = metadata.title,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                metadata.availableStreams.forEach { stream ->
                    val qualityKey = when (stream.quality) {
                        VideoQuality.AUTO -> "auto_quality"
                        VideoQuality.HIGH -> "quality_high"
                        VideoQuality.MEDIUM -> "quality_medium"
                        VideoQuality.LOW -> "quality_low"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .clickable { onSelectQuality(stream) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hd,
                            contentDescription = null,
                            tint = Color(0xFF007AFF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stream.quality.resolutionLabel,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = StringResources.getString(qualityKey, language),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = "~${String.format("%.1f", stream.estimatedSizeMb)} MB",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF34C759)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = StringResources.getString("cancel", language),
                        color = Color(0xFFFF453A)
                    )
                }
            }
        }
    }
}

