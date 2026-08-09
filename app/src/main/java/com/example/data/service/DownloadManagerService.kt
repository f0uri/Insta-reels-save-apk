package com.example.data.service

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import java.io.OutputStream
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

/**
 * Downloads the real video bytes and saves them to the device's PUBLIC
 * gallery (Movies/ReelsDownloads/<platform>) so the file shows up in the
 * phone's Gallery app - not the app-private storage, which is invisible
 * outside the app and gets wiped on uninstall.
 *
 * Uses MediaStore on Android 10+ (scoped storage, no extra permission
 * needed) and falls back to direct file writes on older Android versions.
 */
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
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(20)
                .ifEmpty { "video" }
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${platform.folderName}_${dateStr}_$sanitizedTitle.mp4"

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

            var startTime = System.currentTimeMillis()
            var bytesSinceLastSample = 0L
            var currentSpeedKbps = 0f

            fun onChunk(read: Int) {
                downloadedBytes += read
                bytesSinceLastSample += read
                val currentTime = System.currentTimeMillis()
                val timeDiffMs = currentTime - startTime
                if (timeDiffMs >= 500) {
                    currentSpeedKbps = (bytesSinceLastSample / 1024f) / (timeDiffMs / 1000f)
                    startTime = currentTime
                    bytesSinceLastSample = 0
                }
            }

            val savedPath: String
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savedPath = saveViaMediaStore(inputStream, fileName, platform) { read ->
                    onChunk(read)
                    val progressPercent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                    emit(
                        DownloadState.Progress(
                            percent = progressPercent,
                            speedKbps = currentSpeedKbps,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes
                        )
                    )
                } ?: run {
                    emit(DownloadState.Error("SAVE_TO_GALLERY_FAILED"))
                    return@flow
                }
            } else {
                savedPath = saveViaLegacyFile(inputStream, fileName, platform) { read ->
                    onChunk(read)
                    val progressPercent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                    emit(
                        DownloadState.Progress(
                            percent = progressPercent,
                            speedKbps = currentSpeedKbps,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes
                        )
                    )
                } ?: run {
                    emit(DownloadState.Error("SAVE_TO_GALLERY_FAILED"))
                    return@flow
                }
            }

            emit(DownloadState.Completed(savedPath, downloadedBytes))

        } catch (e: Exception) {
            e.printStackTrace()
            emit(DownloadState.Error(e.localizedMessage ?: "DOWNLOAD_FAILED"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun saveViaMediaStore(
        input: InputStream,
        fileName: String,
        platform: PlatformType,
        onBytes: suspend (Int) -> Unit
    ): String? {
        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val relativePath = "${Environment.DIRECTORY_MOVIES}/ReelsDownloads/${platform.folderName}"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val itemUri = resolver.insert(collection, values) ?: return null

        resolver.openOutputStream(itemUri)?.use { out ->
            copyWithProgress(input, out, onBytes)
        } ?: run {
            resolver.delete(itemUri, null, null)
            return null
        }

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(itemUri, values, null, null)

        return itemUri.toString()
    }

    private suspend fun saveViaLegacyFile(
        input: InputStream,
        fileName: String,
        platform: PlatformType,
        onBytes: suspend (Int) -> Unit
    ): String? {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val albumDir = File(publicDir, "ReelsDownloads/${platform.folderName}")
        if (!albumDir.exists() && !albumDir.mkdirs()) return null

        val outFile = File(albumDir, fileName)
        FileOutputStream(outFile).use { out ->
            copyWithProgress(input, out, onBytes)
        }
        return outFile.absolutePath
    }

    private suspend fun copyWithProgress(
        input: InputStream,
        output: OutputStream,
        onBytes: suspend (Int) -> Unit
    ) {
        val buffer = ByteArray(8192)
        input.use { stream ->
            output.use { out ->
                var read = stream.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    onBytes(read)
                    read = stream.read(buffer)
                }
            }
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            if (path.startsWith("content://")) {
                context.contentResolver.delete(android.net.Uri.parse(path), null, null) > 0
            } else {
                val file = File(path)
                if (file.exists()) file.delete() else false
            }
        } catch (e: Exception) {
            false
        }
    }
}
