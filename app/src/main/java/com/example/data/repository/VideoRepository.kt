package com.example.data.repository

import com.example.data.local.VideoDao
import com.example.data.model.PlatformType
import com.example.data.model.VideoEntity
import com.example.data.model.VideoMetadata
import com.example.data.service.DownloadManagerService
import com.example.data.service.DownloadState
import com.example.data.service.LinkParserService
import kotlinx.coroutines.flow.Flow

class VideoRepository(
    private val videoDao: VideoDao,
    private val linkParserService: LinkParserService,
    private val downloadManagerService: DownloadManagerService
) {
    val allVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()
    val recentVideos: Flow<List<VideoEntity>> = videoDao.getRecentVideos()

    suspend fun parseLink(url: String): Result<VideoMetadata> {
        return linkParserService.parseLink(url)
    }

    fun downloadVideo(
        videoUrl: String,
        platform: PlatformType,
        title: String
    ): Flow<DownloadState> {
        return downloadManagerService.downloadVideo(videoUrl, platform, title)
    }

    suspend fun saveVideoRecord(
        title: String,
        platform: String,
        originalUrl: String,
        thumbnailUrl: String,
        localFilePath: String,
        fileSizeBytes: Long,
        durationText: String,
        qualityLabel: String
    ): Long {
        val entity = VideoEntity(
            title = title,
            platform = platform,
            originalUrl = originalUrl,
            thumbnailUrl = thumbnailUrl,
            localFilePath = localFilePath,
            fileSizeBytes = fileSizeBytes,
            durationText = durationText,
            qualityLabel = qualityLabel
        )
        return videoDao.insertVideo(entity)
    }

    suspend fun deleteVideo(video: VideoEntity) {
        downloadManagerService.deleteFile(video.localFilePath)
        videoDao.deleteVideoById(video.id)
    }

    fun getVideosByPlatform(platform: String): Flow<List<VideoEntity>> {
        return videoDao.getVideosByPlatform(platform)
    }
}
