package com.example.data.service

import com.example.data.model.MediaFormat
import com.example.data.model.PlatformType
import com.example.data.model.VideoMetadata
import com.example.data.model.VideoQuality
import com.example.data.model.VideoQualityStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client-side fallback that asks listnr for a direct Instagram CDN mp4.
 * listnr is a third-party service and can change or disappear; Railway stays primary.
 */
class ListnrService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    fun resolve(postUrl: String): Result<VideoMetadata> {
        return try {
            val payload = JSONObject().apply {
                put("url", postUrl)
                put("platform", "instagram")
                put("type", "video")
            }
            val body = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(LISTNR_ENDPOINT)
                .post(body)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("origin", "https://listnr.ai")
                .header("referer", "https://listnr.ai/")
                .header("user-agent", LISTNR_UA)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("listnr_http_${response.code}"))
                }
                val text = response.body?.string().orEmpty()
                if (text.isBlank()) {
                    return Result.failure(Exception("listnr_empty"))
                }
                val json = runCatching { JSONObject(text) }.getOrElse {
                    return Result.failure(Exception("listnr_malformed"))
                }
                val directUrl = json.optString("url").trim()
                if (directUrl.isBlank() || !directUrl.startsWith("http")) {
                    return Result.failure(Exception("listnr_missing_url"))
                }
                val thumb = json.optJSONObject("data")?.optString("thumb").orEmpty()
                val platform = PlatformType.detectFromUrl(postUrl)
                val metadata = VideoMetadata(
                    originalUrl = postUrl,
                    platform = platform,
                    title = "",
                    authorName = "",
                    thumbnailUrl = thumb,
                    durationText = "",
                    availableStreams = listOf(
                        VideoQualityStream(
                            quality = VideoQuality.AUTO,
                            downloadUrl = directUrl,
                            estimatedSizeMb = 0.0,
                            format = MediaFormat.MP4,
                            isDirect = true,
                        )
                    )
                )
                Result.success(metadata)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val LISTNR_ENDPOINT = "https://bff.listnr.tech/backend/user/getInfoYT"
        private const val LISTNR_UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"
    }
}
