package com.example.data.service

import com.example.data.model.MediaFormat
import com.example.data.model.PlatformType
import com.example.data.model.VideoMetadata
import com.example.data.model.VideoQuality
import com.example.data.model.VideoQualityStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Resolves a social media link into real, direct download streams by calling
 * our own backend (backend/server.js), which runs yt-dlp server-side.
 *
 * There is NO fake/fallback data here on purpose: if extraction fails for any
 * reason (private post, deleted, unsupported link, backend unreachable), we
 * return a real error so the user knows the download did not work - instead
 * of silently handing them an unrelated sample video.
 */
class LinkParserService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    suspend fun parseLink(rawUrl: String): Result<VideoMetadata> = withContext(Dispatchers.IO) {
        val cleanUrl = rawUrl.trim()
        if (cleanUrl.isBlank() || (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://"))) {
            return@withContext Result.failure(IllegalArgumentException("الرجاء إدخال رابط صالح"))
        }

        if (BACKEND_BASE_URL.contains("your-app.up.railway.app")) {
            return@withContext Result.failure(
                IllegalStateException("لم يتم إعداد الخادم بعد. يرجى نشر backend/ وتحديث BACKEND_BASE_URL في Constants.kt")
            )
        }

        val platform = PlatformType.detectFromUrl(cleanUrl)

        try {
            val payload = JSONObject().apply { put("url", cleanUrl) }
            val body = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("$BACKEND_BASE_URL/api/resolve")
                .post(body)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string()
                    ?: return@withContext Result.failure(Exception("لا يوجد رد من الخادم"))

                val json = JSONObject(responseText)
                val success = json.optBoolean("success", false)

                if (!response.isSuccessful || !success) {
                    val serverError = json.optString("error", "تعذر استخراج الفيديو من هذا الرابط")
                    return@withContext Result.failure(Exception(serverError))
                }

                val streamsJson: JSONArray = json.optJSONArray("streams") ?: JSONArray()
                if (streamsJson.length() == 0) {
                    return@withContext Result.failure(Exception("لم يتم العثور على وسائط قابلة للتحميل"))
                }

                val streams = mutableListOf<VideoQualityStream>()
                for (i in 0 until streamsJson.length()) {
                    val s = streamsJson.getJSONObject(i)
                    val downloadUrl = s.optString("downloadUrl")
                    if (downloadUrl.isBlank()) continue
                    val qualityLabel = s.optString("quality", "auto")
                    val sizeMb = if (s.isNull("estimatedSizeMb")) 0.0 else s.optDouble("estimatedSizeMb", 0.0)

                    streams.add(
                        VideoQualityStream(
                            quality = mapQuality(qualityLabel),
                            downloadUrl = downloadUrl,
                            estimatedSizeMb = sizeMb,
                            format = MediaFormat.MP4
                        )
                    )
                }

                if (streams.isEmpty()) {
                    return@withContext Result.failure(Exception("لم يتم العثور على وسائط قابلة للتحميل"))
                }

                val metadata = VideoMetadata(
                    originalUrl = cleanUrl,
                    platform = platform,
                    title = json.optString("title", "${platform.displayName} Video"),
                    authorName = json.optString("author", "@${platform.name.lowercase()}_creator"),
                    thumbnailUrl = json.optString("thumbnail", ""),
                    durationText = json.optString("duration", ""),
                    availableStreams = streams
                )

                Result.success(metadata)
            }
        } catch (e: Exception) {
            Result.failure(Exception(mapError(e)))
        }
    }

    private fun mapQuality(label: String): VideoQuality {
        val digits = label.filter { it.isDigit() }.toIntOrNull() ?: 0
        return when {
            digits >= 1080 -> VideoQuality.HIGH
            digits >= 720 -> VideoQuality.HIGH
            digits >= 480 -> VideoQuality.MEDIUM
            digits in 1..479 -> VideoQuality.LOW
            else -> VideoQuality.AUTO
        }
    }

    private fun mapError(e: Exception): String {
        return when {
            e.message?.contains("Unable to resolve host") == true -> "لا يمكن الوصول للخادم، تأكد من رابط الخادم والاتصال بالإنترنت"
            e.message?.contains("timeout", ignoreCase = true) == true -> "انتهت مهلة الاتصال، حاول مجدداً"
            e.message?.contains("Failed to connect") == true -> "تعذر الاتصال بالخادم"
            else -> e.message ?: "فشل في تحليل الرابط"
        }
    }
}
