package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.components.PlatformBadge
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoActionBottomSheet(
    video: VideoEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141720),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformBadge(platformStr = video.platform)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Items
            ActionRowItem(
                icon = Icons.Default.PlayArrow,
                title = StringResources.getString("action_play", language),
                iconColor = Color(0xFF007AFF),
                onClick = {
                    onDismiss()
                    onPlay()
                }
            )

            ActionRowItem(
                icon = Icons.Default.Share,
                title = StringResources.getString("action_share", language),
                iconColor = Color(0xFF34C759),
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            ActionRowItem(
                icon = Icons.Default.Download,
                title = StringResources.getString("action_save_gallery", language),
                iconColor = Color(0xFFAF52DE),
                onClick = {
                    onDismiss()
                    Toast.makeText(
                        context,
                        StringResources.getString("action_save_gallery", language),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            ActionRowItem(
                icon = Icons.Default.ContentCopy,
                title = StringResources.getString("action_copy_link", language),
                iconColor = Color(0xFFFFCC00),
                onClick = {
                    onDismiss()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", video.originalUrl))
                    Toast.makeText(
                        context,
                        StringResources.getString("link_copied_toast", language),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionRowItem(
                icon = Icons.Default.Delete,
                title = StringResources.getString("action_delete", language),
                iconColor = Color(0xFFFF453A),
                isDestructive = true,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.8.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) Color(0xFFFF453A) else Color.White
            )
        }
    }
}
