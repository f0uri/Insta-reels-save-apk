package com.example.data.service

/**
 * Base URL of your deployed backend (backend/server.js), which runs real
 * yt-dlp extraction. See backend/README.md for deployment instructions.
 *
 * IMPORTANT: You must deploy the backend and replace this placeholder with
 * your real backend URL (no trailing slash), otherwise link resolution will
 * fail with a connection error.
 */
const val BACKEND_BASE_URL = "https://insta-reels-save-apk-production.up.railway.app"

/**
 * Builds a URL that streams/downloads through OUR backend (which runs
 * yt-dlp) instead of hitting the platform's CDN link directly. Direct CDN
 * links from Instagram/TikTok/etc are often short-lived or signed and
 * reject requests from a different client (causing 403 errors) - our
 * backend re-resolves and streams the real bytes, which works reliably.
 *
 * Used both for actual downloads AND for in-app preview playback, so both
 * share the same reliable path.
 */
fun buildBackendStreamUrl(originalPostUrl: String, mode: String = "video", height: Int? = null): String {
    val encodedUrl = java.net.URLEncoder.encode(originalPostUrl, "UTF-8")
    return buildString {
        append(BACKEND_BASE_URL)
        append("/api/download?url=").append(encodedUrl)
        append("&mode=").append(mode)
        if (height != null) append("&height=").append(height)
    }
}
