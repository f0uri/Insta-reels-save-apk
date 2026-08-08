package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.data.model.PlatformType
import com.example.ui.theme.FacebookColor
import com.example.ui.theme.GlassDarkBorder
import com.example.ui.theme.GlassDarkBorderHighlight
import com.example.ui.theme.InstagramColor
import com.example.ui.theme.PinterestColor
import com.example.ui.theme.TikTokColor
import com.example.ui.theme.TwitterColor
import com.example.ui.theme.YouTubeColor
import com.example.ui.theme.iOSBlueGradientEnd
import com.example.ui.theme.iOSBlueGradientStart

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.25f,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red * 0.299 + green * 0.587 + blue * 0.114) < 0.5f }

    val effectiveBg = backgroundColor ?: if (isDark) {
        Color.White.copy(alpha = 0.07f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val borderBrush = if (borderColor != null) {
        Brush.linearGradient(listOf(borderColor, borderColor.copy(alpha = 0.4f)))
    } else if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha * 1.5f),
                Color.White.copy(alpha = borderAlpha * 0.4f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.12f),
                Color.Black.copy(alpha = 0.04f)
            )
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = effectiveBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        content()
    }
}

@Composable
fun iOSCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        content = content
    )
}

@Composable
fun GlassButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(iOSBlueGradientStart, iOSBlueGradientEnd)
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "glassButtonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(gradientColors)
                } else {
                    Brush.linearGradient(listOf(Color.Gray.copy(0.4f), Color.DarkGray.copy(0.4f)))
                }
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (enabled) onClick()
                    }
                )
            }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp).size(20.dp)
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CupertinoGradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    GlassButton(
        text = text,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun PlatformIcon(
    platformStr: String,
    modifier: Modifier = Modifier.size(18.dp)
) {
    val platform = try {
        PlatformType.valueOf(platformStr.uppercase().replace(" ", "_"))
    } catch (e: Exception) {
        when {
            platformStr.contains("Instagram", ignoreCase = true) -> PlatformType.INSTAGRAM
            platformStr.contains("TikTok", ignoreCase = true) -> PlatformType.TIKTOK
            platformStr.contains("Facebook", ignoreCase = true) -> PlatformType.FACEBOOK
            platformStr.contains("YouTube", ignoreCase = true) -> PlatformType.YOUTUBE
            platformStr.contains("Twitter", ignoreCase = true) || platformStr.contains("X", ignoreCase = true) -> PlatformType.TWITTER
            platformStr.contains("Pinterest", ignoreCase = true) -> PlatformType.PINTEREST
            else -> PlatformType.OTHER
        }
    }

    val iconUrl = when (platform) {
        PlatformType.INSTAGRAM -> "https://cdn.jsdelivr.net/gh/glincker/thesvg@main/public/icons/instagram/default.svg"
        PlatformType.FACEBOOK -> "https://cdn.jsdelivr.net/npm/@thesvg/icons/icons/facebook.svg"
        PlatformType.YOUTUBE -> "https://logo.svgcdn.com/simple-icons/youtube-dark.svg"
        PlatformType.TWITTER -> "https://cdn.jsdelivr.net/npm/@thesvg/icons/icons/x.svg"
        PlatformType.TIKTOK -> "https://cdn.jsdelivr.net/npm/@thesvg/icons/icons/tiktok.svg"
        PlatformType.PINTEREST -> "https://cdn.jsdelivr.net/npm/@thesvg/icons/icons/pinterest.svg"
        else -> "https://cdn.jsdelivr.net/npm/@thesvg/icons/icons/x.svg"
    }

    val context = LocalContext.current
    val imageRequest = remember(iconUrl, context) {
        ImageRequest.Builder(context)
            .data(iconUrl)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        NativeFallbackBadge(platform = platform)

        AsyncImage(
            model = imageRequest,
            contentDescription = platformStr,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun NativeFallbackBadge(platform: PlatformType) {
    when (platform) {
        PlatformType.INSTAGRAM -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
        PlatformType.TIKTOK -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "♪", color = Color(0xFF00F2FE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        PlatformType.YOUTUBE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFF0000)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▶", color = Color.White, fontSize = 7.sp)
            }
        }
        PlatformType.FACEBOOK -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF1877F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "f", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        PlatformType.TWITTER -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF0F1419)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "𝕏", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        PlatformType.PINTEREST -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFE60023)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "P", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▶", color = Color.White, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun PlatformBadge(platformStr: String, modifier: Modifier = Modifier) {
    val platform = try {
        PlatformType.valueOf(platformStr.uppercase().replace(" ", "_"))
    } catch (e: Exception) {
        when {
            platformStr.contains("Instagram", ignoreCase = true) -> PlatformType.INSTAGRAM
            platformStr.contains("TikTok", ignoreCase = true) -> PlatformType.TIKTOK
            platformStr.contains("Facebook", ignoreCase = true) -> PlatformType.FACEBOOK
            platformStr.contains("YouTube", ignoreCase = true) -> PlatformType.YOUTUBE
            platformStr.contains("Twitter", ignoreCase = true) || platformStr.contains("X", ignoreCase = true) -> PlatformType.TWITTER
            platformStr.contains("Pinterest", ignoreCase = true) -> PlatformType.PINTEREST
            else -> PlatformType.OTHER
        }
    }

    val (bgColor, textColor, label) = when (platform) {
        PlatformType.INSTAGRAM -> Triple(InstagramColor.copy(alpha = 0.22f), InstagramColor, "Instagram")
        PlatformType.TIKTOK -> Triple(TikTokColor.copy(alpha = 0.22f), TikTokColor, "TikTok")
        PlatformType.YOUTUBE -> Triple(YouTubeColor.copy(alpha = 0.22f), YouTubeColor, "YouTube")
        PlatformType.FACEBOOK -> Triple(FacebookColor.copy(alpha = 0.22f), FacebookColor, "Facebook")
        PlatformType.TWITTER -> Triple(TwitterColor.copy(alpha = 0.22f), TwitterColor, "X / Twitter")
        PlatformType.PINTEREST -> Triple(PinterestColor.copy(alpha = 0.22f), PinterestColor, "Pinterest")
        PlatformType.OTHER -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), MaterialTheme.colorScheme.primary, "Video")
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlatformIcon(platformStr = platformStr, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(iOSBlueGradientStart, iOSBlueGradientEnd)
                                )
                            } else {
                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .clickable { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

