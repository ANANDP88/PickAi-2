package com.example.data.model

data class AiNewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val url: String = "",
    val pubDateMillis: Long = System.currentTimeMillis(),
    val formattedDateTime: String = "",
    val timeAgo: String = "Just now",
    val categoryTag: String = "Breakthrough", // "Breakthrough", "Model Release", "Industry", "Research", "Coding", "Audio AI"
    val impactBadge: String = "🔥 Top Story", // "🔥 Top Story", "⚡ Breaking", "💡 Key Insight", "🚀 Trending"
    val readTime: String = "2 min read",
    val keyTakeaways: List<String> = emptyList(),
    val fullAnalysis: String = "",
    val relatedToolIds: List<String> = emptyList()
)
