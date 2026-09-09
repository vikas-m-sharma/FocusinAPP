package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object YouTubeUtils {

    /**
     * Opens YouTube search or falls back to web browser safely.
     * Does NOT invent video URLs, IDs, or fake links.
     */
    fun openYouTubeSearch(context: Context, query: String) {
        try {
            // First attempt: YouTube app search intent
            val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            // Fallback: Browser YouTube search URL
            try {
                val webUrl = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Could not open YouTube or browser.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
