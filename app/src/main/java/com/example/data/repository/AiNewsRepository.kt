package com.example.data.repository

import com.example.data.model.AiNewsItem

object AiNewsRepository {

    val dailyNews: List<AiNewsItem> = listOf(
        AiNewsItem(
            id = "news_1",
            title = "Next-Gen Multi-Agent Systems Achieve Autonomous Full-Stack App Deployment",
            summary = "New benchmark studies reveal autonomous agent teams can now plan, write tests, refactor, and deploy complete web and mobile applications with 92% pass rates.",
            source = "TechCrunch AI",
            timeAgo = "1 hour ago",
            categoryTag = "Breakthrough",
            impactBadge = "🔥 Top Story",
            readTime = "3 min read",
            keyTakeaways = listOf(
                "Agents communicate via structured JSON RPC protocols to divide database, backend, and UI tasks.",
                "Error recovery and automated regression testing reduced hallucinated code bugs by 68%.",
                "Developer role is shifting toward architectural oversight and prompt constraint design."
            ),
            fullAnalysis = "The frontier of AI software engineering has crossed a critical milestone. Rather than generating single isolated functions, cooperative multi-agent ensembles now verify outputs against automated test suites in isolated sandbox containers. Early benchmarks indicate engineering teams using these autonomous pipelines ship updates 3.8x faster while maintaining code quality.",
            relatedToolIds = listOf("cursor", "github_copilot", "claude", "v0")
        ),
        AiNewsItem(
            id = "news_2",
            title = "Real-Time 4K Video Synthesis Breakthrough: 60 FPS Generated from Text",
            summary = "Generative video architectures achieve sub-second latency rendering photorealistic camera moves, volumetric lighting, and physics-consistent fluid dynamics.",
            source = "MIT Technology Review",
            timeAgo = "8 hours ago",
            categoryTag = "Model Release",
            impactBadge = "⚡ Breaking",
            readTime = "2 min read",
            keyTakeaways = listOf(
                "Diffusion transformers now support temporal keyframe conditioning at 60 frames per second.",
                "Virtual production studios can iterate camera angles and scene lighting in real time.",
                "Consumer hardware with neural accelerators can stream interactive visual worlds."
            ),
            fullAnalysis = "Previously, high-fidelity generative video required minutes of render farm processing per second of output. The latest latent trajectory compression models have made near real-time rendering feasible. This breakthrough opens up live broadcast visual effects, interactive story experiences, and rapid cinematic pre-visualization.",
            relatedToolIds = listOf("runway", "heygen")
        ),
        AiNewsItem(
            id = "news_3",
            title = "On-Device Neural Models Cloud Latency",
            summary = "Compact 4-bit quantized foundation models running locally on smartphones deliver sub-100ms voice dialogue with zero cloud dependency.",
            source = "Ars Technica",
            timeAgo = "10 hours ago",
            categoryTag = "On-Device AI",
            impactBadge = "🚀 Trending",
            readTime = "4 min read",
            keyTakeaways = listOf(
                "Local inference guarantees complete user data privacy and works without internet connectivity.",
                "Sub-100ms response time enables fluid, human-like voice conversations without lag.",
                "Hardware acceleration leverages mobile NPU chips with negligible battery drain."
            ),
            fullAnalysis = "By migrating reasoning directly to device edge silicon, mobile applications can now maintain continuous contextual memory without uploading personal telemetry to remote servers. This architecture underpins next-generation voice assistants and ambient productivity companions.",
            relatedToolIds = listOf("chatgpt", "elevenlabs")
        ),
        AiNewsItem(
            id = "news_4",
            title = "Open Source Weights Released for State-of-the-Art Code & Reasoning Model",
            summary = "Community researchers celebrate the release of open weights that match proprietary frontier model performance on competitive programming and mathematical proof benchmarks.",
            source = "VentureBeat",
            timeAgo = "7 hours ago",
            categoryTag = "Open Source",
            impactBadge = "💡 Key Insight",
            readTime = "3 min read",
            keyTakeaways = listOf(
                "Permissive Apache 2.0 license enables commercial self-hosting and private fine-tuning.",
                "Excels at multi-language translation between Kotlin, Rust, Python, and C++.",
                "Runs on consumer workstations with unified memory architectures."
            ),
            fullAnalysis = "Open-weights models continue to close the gap with closed proprietary offerings. The democratization of frontier reasoning empowers startups and enterprise teams with strict compliance mandates to deploy private air-gapped models without recurring API token overhead.",
            relatedToolIds = listOf("cursor", "perplexity")
        ),
        AiNewsItem(
            id = "news_5",
            title = "Audio Synthesis Frontier: Full Orchestral & Vocal Arrangements Created in 30 Seconds",
            summary = "Generative audio platforms introduce stems isolation, custom emotional timber, and studio-grade mastering directly from user voice memos.",
            source = "Billboard Tech",
            timeAgo = "9 hours ago",
            categoryTag = "Audio AI",
            impactBadge = "🎵 Creator Hub",
            readTime = "2 min read",
            keyTakeaways = listOf(
                "Separates vocals, drums, bass, and synth stems automatically for DAW export.",
                "Supports multilingual singing with native regional phrasing and accent nuance.",
                "Royalty-free commercial licensing options for indie developers and podcasters."
            ),
            fullAnalysis = "Music creators and sound designers are rapidly adopting generative audio tools as ideation copilots. Rather than replacing musicians, these platforms act as virtual jam partners that generate backing arrangements and experimental chord progressions on command.",
            relatedToolIds = listOf("suno", "elevenlabs")
        )
    )

    fun getNewsById(id: String): AiNewsItem? {
        return dailyNews.find { it.id == id }
    }
}
