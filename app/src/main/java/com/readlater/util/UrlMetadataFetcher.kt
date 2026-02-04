package com.readlater.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URI

object UrlMetadataFetcher {

    suspend fun fetchTitle(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            // Try Open Graph title first
            val ogTitle = doc.select("meta[property=og:title]").attr("content")
            if (ogTitle.isNotBlank()) {
                return@withContext ogTitle
            }

            // Try Twitter card title
            val twitterTitle = doc.select("meta[name=twitter:title]").attr("content")
            if (twitterTitle.isNotBlank()) {
                return@withContext twitterTitle
            }

            // Fall back to regular title
            val title = doc.title()
            if (title.isNotBlank()) {
                return@withContext title
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchImageUrl(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            val ogImage = doc.select("meta[property=og:image]").attr("content")
            if (ogImage.isNotBlank()) {
                return@withContext resolveUrl(url, ogImage)
            }

            val twitterImage = doc.select("meta[name=twitter:image]").attr("content")
            if (twitterImage.isNotBlank()) {
                return@withContext resolveUrl(url, twitterImage)
            }

            val imageSrc = doc.select("link[rel=image_src]").attr("href")
            if (imageSrc.isNotBlank()) {
                return@withContext resolveUrl(url, imageSrc)
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveUrl(baseUrl: String, value: String): String {
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (value.startsWith("//")) return "https:$value"
        return runCatching { URI(baseUrl).resolve(value).toString() }.getOrElse { value }
    }

    fun extractUrl(sharedText: String): String? {
        // Regex to find URLs in text
        val urlPattern = Regex(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            RegexOption.IGNORE_CASE
        )
        return urlPattern.find(sharedText)?.value
    }
}
