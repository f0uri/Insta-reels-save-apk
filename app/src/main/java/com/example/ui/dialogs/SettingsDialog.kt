package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringResources

@Composable
fun SettingsDialog(
    language: AppLanguage,
    isDarkTheme: Boolean,
    autoClearClipboard: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleAutoClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var showLanguagePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        BoxContainer {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringResources.getString("settings", language),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 1: Appearance & Preferences
                GlassGroup {
                    // Language
                    SettingRow(
                        icon = Icons.Default.Language,
                        title = StringResources.getString("language", language),
                        subtitle = language.displayName,
                        onClick = { onToggleLanguage() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dark Mode
                    SettingSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = StringResources.getString("dark_mode", language),
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Copy Paste
                    SettingSwitchRow(
                        icon = Icons.Default.ContentPaste,
                        title = StringResources.getString("auto_clear_clipboard", language),
                        checked = autoClearClipboard,
                        onCheckedChange = { onToggleAutoClear() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Group 2: App Info & Credits
                GlassGroup {
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = StringResources.getString("about_title", language),
                        subtitle = StringResources.getString("app_subtitle", language) + " v2.0",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingRow(
                        icon = Icons.Default.HighQuality,
                        title = StringResources.getString("developer_label", language),
                        subtitle = StringResources.getString("developer_name", language),
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rights Footer
                Text(
                    text = StringResources.getString("app_rights", language),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun BoxContainer(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF141720),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
    ) {
        content()
    }
}

@Composable
private fun GlassGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF007AFF).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF007AFF),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFAF52DE).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFAF52DE),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

