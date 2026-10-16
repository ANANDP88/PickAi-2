package com.example.data.model

data class AiNewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val timeAgo: String,
    val categoryTag: String, // "Breakthrough", "Model Release", "Industry", "Research", "Coding"
    val impactBadge: String, // "🔥 Top Story", "⚡ Breaking", "💡 Key Insight", "🚀 Trending"
    val readTime: String,
    val keyTakeaways: List<String>,
    val fullAnalysis: String,
    val relatedToolIds: List<String> = emptyList()
)
