package com.example.data.model

data class PromptExample(
    val title: String,
    val prompt: String,
    val category: String = ""
)

data class AiTool(
    val id: String,
    val name: String,
    val tagline: String,
    val category: String,
    val description: String,
    val pricing: String, // "Free", "Freemium", "Paid"
    val rating: Float,
    val reviewsCount: String,
    val badge: String? = null, // "Trending", "Editor's Pick", "Popular", "New"
    val websiteUrl: String,
    val tags: List<String>,
    val features: List<String>,
    val examplePrompts: List<PromptExample>
)
