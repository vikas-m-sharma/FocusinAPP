package com.example.data.recovery

data class RecoveryRecommendation(
    val title: String,
    val category: String,
    val durationText: String,
    val description: String,
    val youtubeUrl: String
)

data class RecoveryItem(
    val title: String,
    val description: String,
    val category: String, // "MUSIC", "BREATHING", "MOTIVATION", "PODCAST"
    val durationText: String,
    val actionUrl: String
)

object RecoveryRecommendations {

    val motivationalQuotes = listOf(
        "Small consistent efforts create big results.",
        "One focused session is better than a perfect plan.",
        "Action cures hesitation. Consistency creates momentum.",
        "Focus is a muscle; today you trained it with discipline.",
        "The secret of getting ahead is getting started.",
        "Clear your mind, recover your energy, and return stronger."
    )

    fun getRandomQuote(): String {
        return motivationalQuotes.random()
    }

    val postSessionItems = listOf(
        RecoveryItem(
            title = "5-Minute Box Breathing Exercise",
            description = "Reset cortisol and restore parasympathetic focus after intense cognitive load.",
            category = "BREATHING",
            durationText = "5 mins",
            actionUrl = "https://www.youtube.com/results?search_query=5+minute+box+breathing+exercise"
        ),
        RecoveryItem(
            title = "10-Minute Relaxing Ambient Soundscape",
            description = "Gentle calming frequencies to help your mind decompress before the next block.",
            category = "MUSIC",
            durationText = "10 mins",
            actionUrl = "https://www.youtube.com/results?search_query=10+minute+relaxing+study+break+music"
        ),
        RecoveryItem(
            title = "Deep Study & Focus Motivation",
            description = "Short inspiration on long-term discipline, academic resilience, and persistence.",
            category = "MOTIVATION",
            durationText = "4 mins",
            actionUrl = "https://www.youtube.com/results?search_query=study+motivation+short+discipline"
        ),
        RecoveryItem(
            title = "Huberman Lab Focus & Recovery Clip",
            description = "Science-backed protocols for post-focus recovery and dopamine maintenance.",
            category = "PODCAST",
            durationText = "8 mins",
            actionUrl = "https://www.youtube.com/results?search_query=huberman+lab+focus+recovery+protocol"
        )
    )

    val audioAndPodcasts = listOf(
        RecoveryItem(
            title = "Deep Work Podcast Insights",
            description = "Exploring strategies for undistracted focus in a hyper-connected world.",
            category = "PODCAST",
            durationText = "Podcast",
            actionUrl = "https://www.youtube.com/results?search_query=deep+work+cal+newport+podcast"
        ),
        RecoveryItem(
            title = "Binaural Alpha Waves (10 Hz)",
            description = "Pure acoustic waves to support neural relaxation during planned break intervals.",
            category = "MUSIC",
            durationText = "15 mins",
            actionUrl = "https://www.youtube.com/results?search_query=binaural+alpha+waves+break"
        )
    )
}
