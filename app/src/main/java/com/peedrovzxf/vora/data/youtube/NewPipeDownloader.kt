package com.peedrovzxf.vora.data.youtube

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.net.HttpURLConnection
import java.net.URL

object NewPipeDownloader : Downloader() {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    override fun execute(request: Request): Response {
        val conn = URL(request.url()).openConnection() as HttpURLConnection
        conn.requestMethod = request.httpMethod()
        conn.connectTimeout = 15_000
        conn.readTimeout    = 15_000
        conn.setRequestProperty("User-Agent", USER_AGENT)

        request.headers().forEach { (key, values) ->
            values.forEach { value -> conn.setRequestProperty(key, value) }
        }

        val body = request.dataToSend()
        if (body != null) {
            conn.doOutput = true
            conn.outputStream.use { it.write(body) }
        }

        val responseCode = conn.responseCode
        if (responseCode == 429) throw ReCaptchaException("Rate limited", request.url())

        val responseBody = try {
            conn.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: ""
        }

        val responseHeaders: Map<String, List<String>> = conn.headerFields
            .filterKeys { it != null }

        return Response(responseCode, conn.responseMessage, responseHeaders, responseBody, request.url())
    }
}