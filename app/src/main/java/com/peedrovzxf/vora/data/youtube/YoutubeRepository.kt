package com.peedrovzxf.vora.data.youtube

import com.peedrovzxf.vora.data.local.AppDatabase
import com.peedrovzxf.vora.data.local.AppSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.AudioStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YoutubeRepository(private val db: AppDatabase) {

    companion object {
        const val KEY_YOUTUBE_API = "youtube_api_key"
        private const val BASE    = "https://www.googleapis.com/youtube/v3"
    }

    suspend fun getApiKey(): String? =
        db.appSettingsDao().get(KEY_YOUTUBE_API)

    suspend fun saveApiKey(key: String) =
        db.appSettingsDao().set(AppSettingsEntity(KEY_YOUTUBE_API, key.trim()))

    suspend fun deleteApiKey() =
        db.appSettingsDao().delete(KEY_YOUTUBE_API)

    suspend fun search(query: String, apiKey: String): List<YoutubeSearchResult> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")

        val url  = "$BASE/search?part=snippet&maxResults=25&q=$encoded&type=video,playlist&key=$apiKey"
        val root = JSONObject(get(url))
        val items = root.getJSONArray("items")

        val videoIds = (0 until items.length()).mapNotNull { i ->
            val item = items.getJSONObject(i)
            val kind = item.getJSONObject("id").optString("kind", "")
            if (kind == "youtube#video") item.getJSONObject("id").optString("videoId") else null
        }
        val durations = fetchDurations(videoIds, apiKey)

        (0 until items.length()).mapNotNull { i ->
            runCatching {
                val item    = items.getJSONObject(i)
                val idObj   = item.getJSONObject("id")
                val kind    = idObj.optString("kind", "")
                val snippet = item.getJSONObject("snippet")
                val thumb   = snippet
                    .getJSONObject("thumbnails")
                    .optJSONObject("medium")
                    ?.optString("url") ?: ""

                when (kind) {
                    "youtube#video" -> {
                        val videoId = idObj.getString("videoId")
                        YoutubeSearchResult.Video(
                            videoId         = videoId,
                            title           = snippet.optString("title", ""),
                            channelName     = snippet.optString("channelTitle", ""),
                            thumbnailUrl    = thumb.ifEmpty { "https://i.ytimg.com/vi/$videoId/mqdefault.jpg" },
                            durationSeconds = durations[videoId] ?: 0L
                        )
                    }
                    "youtube#playlist" -> {
                        val playlistId = idObj.getString("playlistId")
                        YoutubeSearchResult.Playlist(
                            playlistId   = playlistId,
                            title        = snippet.optString("title", ""),
                            channelName  = snippet.optString("channelTitle", ""),
                            thumbnailUrl = thumb,
                            videoCount   = 0
                        )
                    }
                    else -> null
                }
            }.getOrNull()
        }
    }

    suspend fun getPlaylistVideos(playlistId: String, apiKey: String): List<YoutubeSearchResult.Video> = withContext(Dispatchers.IO) {
        val results    = mutableListOf<YoutubeSearchResult.Video>()
        var pageToken  = ""

        do {
            val pageParam = if (pageToken.isNotEmpty()) "&pageToken=$pageToken" else ""
            val url       = "$BASE/playlistItems?part=snippet,contentDetails&maxResults=50&playlistId=$playlistId$pageParam&key=$apiKey"
            val root      = JSONObject(get(url))
            val items     = root.getJSONArray("items")

            val videoIds = (0 until items.length()).mapNotNull { i ->
                items.getJSONObject(i)
                    .getJSONObject("contentDetails")
                    .optString("videoId")
                    .takeIf { it.isNotEmpty() }
            }

            val durations = fetchDurations(videoIds, apiKey)

            for (i in 0 until items.length()) {
                runCatching {
                    val item    = items.getJSONObject(i)
                    val snippet = item.getJSONObject("snippet")
                    val videoId = item.getJSONObject("contentDetails").optString("videoId", "")
                    if (videoId.isEmpty()) return@runCatching

                    val title = snippet.optString("title", "")
                    if (title == "Private video" || title == "Deleted video") return@runCatching

                    val thumb = snippet
                        .getJSONObject("thumbnails")
                        .optJSONObject("medium")
                        ?.optString("url")
                        ?: "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"

                    results.add(
                        YoutubeSearchResult.Video(
                            videoId         = videoId,
                            title           = title,
                            channelName     = snippet.optString("videoOwnerChannelTitle", ""),
                            thumbnailUrl    = thumb,
                            durationSeconds = durations[videoId] ?: 0L
                        )
                    )
                }
            }

            pageToken = root.optString("nextPageToken", "")
        } while (pageToken.isNotEmpty())

        results
    }

    suspend fun getAudioStreamUrl(videoId: String): String = withContext(Dispatchers.IO) {
        val videoUrl  = "https://www.youtube.com/watch?v=$videoId"
        val extractor = NewPipe.getService(ServiceList.YouTube.serviceId)
            .getStreamExtractor(videoUrl) as YoutubeStreamExtractor

        extractor.fetchPage()

        val audioStreams: List<AudioStream> = extractor.audioStreams
        val best = audioStreams
            .filter { it.format?.mimeType?.contains("mp4") == true && it.averageBitrate > 0 }
            .maxByOrNull { it.averageBitrate }
            ?: audioStreams
                .filter { it.averageBitrate > 0 }
                .maxByOrNull { it.averageBitrate }
            ?: audioStreams.firstOrNull()
            ?: throw Exception("No se encontraron streams de audio para $videoId")

        best.content
    }

    private suspend fun fetchDurations(
        videoIds : List<String>,
        apiKey   : String
    ): Map<String, Long> = withContext(Dispatchers.IO) {
        if (videoIds.isEmpty()) return@withContext emptyMap()
        val ids   = videoIds.joinToString(",")
        val url   = "$BASE/videos?part=contentDetails&id=$ids&key=$apiKey"
        val root  = JSONObject(get(url))
        val items = root.getJSONArray("items")
        buildMap {
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val id   = item.getString("id")
                val iso  = item.getJSONObject("contentDetails").optString("duration", "")
                put(id, parseIso8601Duration(iso))
            }
        }
    }

    private fun parseIso8601Duration(iso: String): Long {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L
        val h = match.groupValues[1].toLongOrNull() ?: 0L
        val m = match.groupValues[2].toLongOrNull() ?: 0L
        val s = match.groupValues[3].toLongOrNull() ?: 0L
        return h * 3600 + m * 60 + s
    }

    private fun get(urlString: String): String {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 10_000
        conn.readTimeout    = 10_000
        return try {
            if (conn.responseCode !in 200..299) {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: ""
                throw Exception("HTTP ${conn.responseCode}: $error")
            }
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }
}