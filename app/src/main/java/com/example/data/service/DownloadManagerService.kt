package com.example.data.service

import android.content.Context
import com.example.data.model.PlatformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Progress(
        val percent: Int,
        val speedKbps: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : DownloadState()
    data class Completed(
        val localFilePath: String,
        val fileSizeBytes: Long
    ) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class DownloadManagerService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun downloadVideo(
        videoUrl: String,
        platform: PlatformType,
        title: String
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Progress(0, 0f, 0, 0))

        try {
            val destinationDir = getPlatformDirectory(platform)
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(20)
                .ifEmpty { "video" }
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${platform.folderName}_${dateStr}_$sanitizedTitle.mp4"
            val targetFile = File(destinationDir, fileName)

            val request = Request.Builder()
                .url(videoUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                emit(DownloadState.Error("HTTP_ERROR_${response.code}"))
                return@flow
            }

            val body = response.body!!
            val totalBytes = body.contentLength().let { if (it <= 0) 10_000_000L else it }

            var downloadedBytes = 0L
            val buffer = ByteArray(8192)
            var bytesRead: Int

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            var startTime = System.currentTimeMillis()
            var bytesSinceLastSample = 0L
            var currentSpeedKbps = 0f

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        bytesSinceLastSample += bytesRead

                        val currentTime = System.currentTimeMillis()
                        val timeDiffMs = currentTime - startTime

                        if (timeDiffMs >= 500) {
                            currentSpeedKbps = (bytesSinceLastSample / 1024f) / (timeDiffMs / 1000f)
                            startTime = currentTime
                            bytesSinceLastSample = 0
                        }

                        val progressPercent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                        emit(
                            DownloadState.Progress(
                                percent = progressPercent,
                                speedKbps = currentSpeedKbps,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                        )
                    }
                }
            }

            emit(DownloadState.Completed(targetFile.absolutePath, targetFile.length()))

        } catch (e: Exception) {
            e.printStackTrace()
            emit(DownloadState.Error(e.localizedMessage ?: "DOWNLOAD_FAILED"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getPlatformDirectory(platform: PlatformType): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(baseDir, "ReelsDownloads/${platform.folderName}")
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }
}
