package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VioletGlowEnd
import com.example.ui.theme.VioletGlowStart
import com.example.ui.theme.iOSBlue
import com.example.ui.theme.iOSBlueGradientEnd
import com.example.ui.theme.iOSBlueGradientStart

enum class SaveFlowTab(val route: String) {
    HOME("home"),
    DOWNLOADS("downloads"),
    HISTORY("history"),
    SETTINGS("settings")
}

@Composable
fun AmbientGlassBackground(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val bgColor = if (isDarkTheme) Color(0xFF090B10) else Color(0xFFF2F5FB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (isDarkTheme) {
            // Subtle ambient radial glow top-right (Cyan/Blue)
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = 140.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00C6FF).copy(alpha = 0.22f),
                                Color(0xFF0051FF).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Subtle ambient radial glow middle-left (Purple/Violet)
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .offset(x = (-140).dp, y = 280.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFAF52DE).copy(alpha = 0.18f),
                                Color(0xFF8A2BE2).copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Subtle ambient radial glow bottom-right (Pink/Rose)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 120.dp, y = 620.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF2D55).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        } else {
            // Light Theme Ambient Glows
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = 140.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF007AFF).copy(alpha = 0.14f),
                                Color(0xFF00C6FF).copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(360.dp)
                    .offset(x = (-140).dp, y = 260.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE1306C).copy(alpha = 0.12f),
                                Color(0xFFAF52DE).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 120.dp, y = 600.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF34C759).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        content()
    }
}

@Composable
fun SaveFlowGlassBottomNav(
    currentTab: SaveFlowTab,
    onTabSelected: (SaveFlowTab) -> Unit,
    homeTitle: String,
    downloadsTitle: String,
    historyTitle: String,
    settingsTitle: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299 + green * 0.587 + blue * 0.114) < 0.5f }

    val navBgColor = if (isDark) Color(0x2EFFFFFF) else Color(0xF0FFFFFF)
    val navBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.12f),
                Color.Black.copy(alpha = 0.05f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(navBgColor)
                .border(
                    width = 1.dp,
                    brush = navBorderBrush,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabItem(
                    tab = SaveFlowTab.HOME,
                    isSelected = currentTab == SaveFlowTab.HOME,
                    label = homeTitle,
                    activeIcon = Icons.Filled.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    isDark = isDark,
                    onClick = { onTabSelected(SaveFlowTab.HOME) }
                )

                NavTabItem(
                    tab = SaveFlowTab.DOWNLOADS,
                    isSelected = currentTab == SaveFlowTab.DOWNLOADS,
                    label = downloadsTitle,
                    activeIcon = Icons.Filled.Download,
                    inactiveIcon = Icons.Outlined.Download,
                    isDark = isDark,
                    onClick = { onTabSelected(SaveFlowTab.DOWNLOADS) }
                )

                NavTabItem(
                    tab = SaveFlowTab.HISTORY,
                    isSelected = currentTab == SaveFlowTab.HISTORY,
                    label = historyTitle,
                    activeIcon = Icons.Filled.History,
                    inactiveIcon = Icons.Outlined.History,
                    isDark = isDark,
                    onClick = { onTabSelected(SaveFlowTab.HISTORY) }
                )

                NavTabItem(
                    tab = SaveFlowTab.SETTINGS,
                    isSelected = currentTab == SaveFlowTab.SETTINGS,
                    label = settingsTitle,
                    activeIcon = Icons.Filled.Settings,
                    inactiveIcon = Icons.Outlined.Settings,
                    isDark = isDark,
                    onClick = { onTabSelected(SaveFlowTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: SaveFlowTab,
    isSelected: Boolean,
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(stiffness = 350f),
        label = "tabScale"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isDark -> Color.White.copy(alpha = 0.5f)
            else -> Color(0xFF555D65)
        },
        label = "tabIconColor"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(iOSBlueGradientStart, iOSBlueGradientEnd)
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(horizontal = if (isSelected) 14.dp else 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSelected) activeIcon else inactiveIcon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
