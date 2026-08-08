package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlatformType(val displayName: String, val folderName: String) {
    INSTAGRAM("Instagram", "Instagram"),
    TIKTOK("TikTok", "TikTok"),
    YOUTUBE("YouTube Shorts", "YouTube"),
    FACEBOOK("Facebook", "Facebook"),
    TWITTER("X / Twitter", "Twitter"),
    PINTEREST("Pinterest", "Pinterest"),
    OTHER("Social Media", "Downloads");

    companion object {
        fun detectFromUrl(url: String): PlatformType {
            val lower = url.lowercase().trim()
            return when {
                lower.contains("instagram.com") || lower.contains("instagr.am") -> INSTAGRAM
                lower.contains("tiktok.com") || lower.contains("vm.tiktok.com") -> TIKTOK
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> FACEBOOK
                lower.contains("twitter.com") || lower.contains("x.com") -> TWITTER
                lower.contains("pinterest.com") || lower.contains("pin.it") -> PINTEREST
                else -> OTHER
            }
        }
    }
}

enum class VideoQuality(val label: String, val resolutionLabel: String, val bitRateKbps: Int) {
    AUTO("Auto Quality", "Auto", 3000),
    HIGH("1080p HD", "1080p", 3500),
    MEDIUM("720p SD", "720p", 1800),
    LOW("480p SD", "480p", 800)
}

enum class MediaFormat(val extension: String, val label: String) {
    MP4("mp4", "MP4 Video"),
    MP3("mp3", "MP3 Audio")
}

data class VideoQualityStream(
    val quality: VideoQuality,
    val downloadUrl: String,
    val estimatedSizeMb: Double,
    val format: MediaFormat = MediaFormat.MP4
)

data class VideoMetadata(
    val originalUrl: String,
    val platform: PlatformType,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String,
    val durationText: String,
    val availableStreams: List<VideoQualityStream>
)

@Entity(tableName = "downloaded_videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val platform: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val durationText: String,
    val qualityLabel: String,
    val formatLabel: String = "MP4",
    val downloadTimestamp: Long = System.currentTimeMillis()
)

