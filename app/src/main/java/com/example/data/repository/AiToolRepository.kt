package com.example.data.repository

import com.example.data.model.AiTool
import com.example.data.model.PromptExample

object AiToolRepository {

    val categories = listOf(
        "Chatbots",
        "Content",
        "Images",
        "Video",
        "Coding",
        "Audio"
    )

    val tools: List<AiTool> = listOf(
        // Chatbots
        AiTool(
            id = "chatgpt",
            name = "ChatGPT",
            tagline = "Conversational assistant for reasoning, writing, and problem-solving.",
            category = "Chatbots",
            description = "Developed by OpenAI, ChatGPT is a versatile generative AI model powered by GPT-4o. It excels at complex reasoning, drafting emails, coding, summarizing long documents, and voice-based conversation.",
            pricing = "Freemium",
            rating = 4.9f,
            reviewsCount = "48.2k",
            badge = "Popular",
            websiteUrl = "https://chatgpt.com",
            tags = listOf("Conversational", "Reasoning", "Multimodal", "Productivity"),
            features = listOf(
                "Voice conversation mode",
                "Web browsing & citation",
                "Data analysis and python interpreter",
                "Custom GPTs creation"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Professional Leave Request",
                    prompt = "Write a polite leave application to my manager for two days because of a family function, ensuring smooth handover of current tasks."
                ),
                PromptExample(
                    title = "Executive Summary",
                    prompt = "Summarize the key trade-offs between monolithic architecture and microservices for a high-traffic fintech platform."
                ),
                PromptExample(
                    title = "Creative Ideation",
                    prompt = "Give me 5 distinctive branding concept names with taglines for an eco-friendly specialty coffee subscription."
                )
            )
        ),
        AiTool(
            id = "claude",
            name = "Claude 3.5 Sonnet",
            tagline = "Nuanced writing, large-context analysis, and state-of-the-art code generation.",
            category = "Chatbots",
            description = "Anthropic's flagship model Claude 3.5 Sonnet features best-in-class coding skills, deep empathy, 200k token context window, and Artifacts for visual previewing.",
            pricing = "Freemium",
            rating = 4.9f,
            reviewsCount = "31.5k",
            badge = "Trending",
            websiteUrl = "https://claude.ai",
            tags = listOf("Large Context", "Code Generation", "Nuanced Writing", "Artifacts"),
            features = listOf(
                "Interactive Artifacts UI preview",
                "200K context window for huge documents",
                "High steerability and nuanced human tone",
                "Vision and chart comprehension"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Code Refactoring",
                    prompt = "Refactor this Kotlin coroutine flow to prevent memory leaks and handle cancellation exceptions cleanly."
                ),
                PromptExample(
                    title = "Contract Analysis",
                    prompt = "Review this SaaS terms sheet and highlight indemnification and SLA liabilities."
                )
            )
        ),
        AiTool(
            id = "perplexity",
            name = "Perplexity AI",
            tagline = "Answer engine combining conversational AI with real-time web search.",
            category = "Chatbots",
            description = "Perplexity provides cited, accurate answers by querying real-time web sources. It eliminates hallucinations by attaching direct citations to every claim.",
            pricing = "Freemium",
            rating = 4.8f,
            reviewsCount = "19.8k",
            badge = "Trending",
            websiteUrl = "https://perplexity.ai",
            tags = listOf("Search", "Research", "Citations", "Real-Time"),
            features = listOf(
                "Source citations with every paragraph",
                "Focus mode for academic papers or YouTube",
                "Pro Search for multi-step reasoning",
                "Collections for project research"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Market Landscape",
                    prompt = "What are the latest funding rounds and market growth figures for generative video startups in 2026?"
                ),
                PromptExample(
                    title = "Academic Fact Check",
                    prompt = "Explain the biochemical mechanism of GLP-1 receptor agonists and list primary clinical trial references."
                )
            )
        ),

        // Content
        AiTool(
            id = "notion_ai",
            name = "Notion AI",
            tagline = "Integrated workplace AI that reads your entire workspace and drafts docs.",
            category = "Content",
            description = "Notion AI augments notes, documentation, project roadmaps, and wikis directly inside Notion. Ask questions across your company docs, generate tables, and summarize notes.",
            pricing = "Paid",
            rating = 4.7f,
            reviewsCount = "22.1k",
            badge = "Editor's Pick",
            websiteUrl = "https://notion.so",
            tags = listOf("Workplace", "Notes", "Documentation", "Wikis"),
            features = listOf(
                "Q&A across entire Notion workspace",
                "One-click action item extraction",
                "Tone changer and grammar polish",
                "Automated database population"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Meeting Notes to Action Items",
                    prompt = "Extract clear action items with owners and estimated timelines from these raw meeting bullet points."
                ),
                PromptExample(
                    title = "Product Requirements Document (PRD)",
                    prompt = "Generate a comprehensive PRD template for an in-app referral reward program with success metrics."
                )
            )
        ),
        AiTool(
            id = "jasper",
            name = "Jasper AI",
            tagline = "AI marketing copilot built to scale brand campaigns and blogs.",
            category = "Content",
            description = "Jasper is designed specifically for enterprise marketing teams. It maintains your brand voice, understands audience segmentation, and automates multi-channel marketing campaigns.",
            pricing = "Paid",
            rating = 4.6f,
            reviewsCount = "14.2k",
            badge = "Popular",
            websiteUrl = "https://jasper.ai",
            tags = listOf("Marketing", "Copywriting", "SEO", "Brand Voice"),
            features = listOf(
                "Custom brand voice training",
                "SEO mode with Surfer integration",
                "Multi-channel campaign generator",
                "Team workflow & asset management"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "High-Converting Ad Copy",
                    prompt = "Write 3 variants of Facebook ad copy targeting fitness enthusiasts for a low-sugar protein bar."
                )
            )
        ),

        // Images
        AiTool(
            id = "midjourney",
            name = "Midjourney v6",
            tagline = "Industry benchmark for photorealistic, painterly, and cinematic artwork.",
            category = "Images",
            description = "Midjourney translates nuanced natural language descriptions into breathtaking artistic illustrations, hyper-realistic photos, and cinematic visual assets with exquisite lighting.",
            pricing = "Paid",
            rating = 4.9f,
            reviewsCount = "62.4k",
            badge = "Popular",
            websiteUrl = "https://midjourney.com",
            tags = listOf("Photorealism", "Concept Art", "Cinematic", "Illustration"),
            features = listOf(
                "Unmatched artistic composition",
                "Inpainting and outpainting (Vary Region)",
                "Style reference parameter (--sref)",
                "Precise camera and lighting control"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Cinematic Cyberpunk Street",
                    prompt = "A neon-lit cyberpunk market in Kyoto at dusk, rain puddles reflecting holographic signs, 35mm lens, f/1.8, atmospheric volumetric mist, hyper-detailed --ar 16:9 --v 6.0"
                ),
                PromptExample(
                    title = "Minimalist 3D App Icon",
                    prompt = "Vibrant 3D claymorphic robot head icon, soft pastel purple and electric cyan palette, smooth frosted glass texture, studio softbox lighting --ar 1:1"
                )
            )
        ),
        AiTool(
            id = "dalle3",
            name = "DALL-E 3",
            tagline = "Precise prompt adherence and crisp in-image typography.",
            category = "Images",
            description = "Integrated into ChatGPT and Microsoft Copilot, DALL-E 3 faithfully adheres to complex prompt instructions and cleanly renders legible text inside generated images.",
            pricing = "Freemium",
            rating = 4.8f,
            reviewsCount = "39.0k",
            badge = "Featured",
            websiteUrl = "https://openai.com/dall-e-3",
            tags = listOf("Prompt Adherence", "Typography", "Easy to Use", "ChatGPT"),
            features = listOf(
                "Exceptional adherence to complex prompts",
                "Renders legible text and logos",
                "Interactive chat refinements in ChatGPT",
                "High dynamic range color support"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Retro Poster with Typography",
                    prompt = "A retro 1970s travel poster of Mars with bold vintage typography reading 'VISIT MARS - 2040', warm sunset tones and stippled texture."
                )
            )
        ),

        // Video
        AiTool(
            id = "runway",
            name = "Runway Gen-3 Alpha",
            tagline = "Next-generation video synthesis with cinematic motion and physics.",
            category = "Video",
            description = "Runway Gen-3 Alpha enables filmmakers and creators to produce high-fidelity cinematic video clips from text prompts or reference images with realistic camera motion and physics.",
            pricing = "Paid",
            rating = 4.8f,
            reviewsCount = "18.3k",
            badge = "Trending",
            websiteUrl = "https://runwayml.com",
            tags = listOf("Cinematic", "Motion", "Text-to-Video", "Film"),
            features = listOf(
                "Motion Brush for localized animation",
                "Director mode camera movements (pan, tilt, zoom)",
                "High frame rate fluid motion",
                "Keyframe interpolation"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "FPV Drone Flythrough",
                    prompt = "Cinematic FPV drone shot flying through a glowing crystal cave, bioluminescent mushrooms illuminating underground river, golden dust particles floating."
                )
            )
        ),
        AiTool(
            id = "heygen",
            name = "HeyGen",
            tagline = "AI studio for photorealistic avatars and multi-language video translation.",
            category = "Video",
            description = "HeyGen creates professional studio videos using realistic AI digital twins. It translates videos into 40+ languages while matching the speaker's original voice and lip movements.",
            pricing = "Freemium",
            rating = 4.7f,
            reviewsCount = "11.6k",
            badge = "New",
            websiteUrl = "https://heygen.com",
            tags = listOf("Avatars", "Video Translation", "Lip Sync", "Presentations"),
            features = listOf(
                "Voice cloning in 40+ languages",
                "Instant photorealistic avatar creation",
                "Seamless AI lip-syncing",
                "Script-to-video automation"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Product Demo Onboarding",
                    prompt = "Create a 60-second friendly customer welcome video with an enthusiastic tech founder avatar introducing key mobile features."
                )
            )
        ),

        // Coding
        AiTool(
            id = "github_copilot",
            name = "GitHub Copilot",
            tagline = "The world's most widely adopted AI pair programmer in your IDE.",
            category = "Coding",
            description = "Trained on billions of lines of code, GitHub Copilot turns natural language prompts into code suggestions across IDEs including VS Code, Android Studio, and JetBrains.",
            pricing = "Paid",
            rating = 4.8f,
            reviewsCount = "54.1k",
            badge = "Popular",
            websiteUrl = "https://github.com/features/copilot",
            tags = listOf("Pair Programmer", "IDE Integration", "Autocomplete", "Unit Tests"),
            features = listOf(
                "Context-aware inline autocomplete",
                "Copilot Chat inside Android Studio / VS Code",
                "Automated unit test generation",
                "Pull request summary automation"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Compose ViewModel Architecture",
                    prompt = "Write a Jetpack Compose ViewModel using StateFlow and clean architecture for paginating a list of items with pull-to-refresh."
                ),
                PromptExample(
                    title = "Unit Test Suite",
                    prompt = "Generate JUnit4 unit tests testing all edge cases for this monetary currency conversion calculator."
                )
            )
        ),
        AiTool(
            id = "cursor",
            name = "Cursor AI",
            tagline = "AI-first code editor designed for rapid multi-file engineering.",
            category = "Coding",
            description = "Cursor is a fork of VS Code built specifically for AI collaboration. It indexes your entire repository, allows multi-file edits in Composer, and predicts your next edit location.",
            pricing = "Freemium",
            rating = 4.9f,
            reviewsCount = "27.4k",
            badge = "Trending",
            websiteUrl = "https://cursor.com",
            tags = listOf("Code Editor", "Full Repo Index", "Composer", "Productivity"),
            features = listOf(
                "Full-codebase semantic indexing",
                "Composer for multi-file code generation",
                "Cursor Tab predictive code completions",
                "Bug finder and terminal debugging"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Database Migration",
                    prompt = "Write an Android Room database migration from version 1 to 2 adding an is_favorite column with default value false."
                )
            )
        ),
        AiTool(
            id = "v0",
            name = "v0 by Vercel",
            tagline = "Generative UI system turning prompts into production-ready frontends.",
            category = "Coding",
            description = "v0 enables designers and developers to prompt modern responsive user interfaces with Tailwind CSS, React, and Shadcn UI components in seconds.",
            pricing = "Freemium",
            rating = 4.8f,
            reviewsCount = "15.9k",
            badge = "Featured",
            websiteUrl = "https://v0.dev",
            tags = listOf("Frontend", "Design to Code", "React", "Rapid Prototyping"),
            features = listOf(
                "Instant React / Tailwind component generation",
                "Interactive visual preview and code inspector",
                "Figma-like layout editing with natural language",
                "Export clean accessible code"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Analytics Dashboard UI",
                    prompt = "Create a dark-themed metrics dashboard card with weekly revenue sparklines, percentage change badges, and filter tabs."
                )
            )
        ),

        // Audio
        AiTool(
            id = "suno",
            name = "Suno AI v3.5",
            tagline = "Generate full songs with vocals, instruments, and mixing from any prompt.",
            category = "Audio",
            description = "Suno creates complete radio-quality songs in any genre with realistic singing voices, coherent lyrics, and rich instrumental arrangements in under 60 seconds.",
            pricing = "Freemium",
            rating = 4.9f,
            reviewsCount = "41.8k",
            badge = "Popular",
            websiteUrl = "https://suno.com",
            tags = listOf("Music Generation", "Full Songs", "Vocals", "Creativity"),
            features = listOf(
                "Full 4-minute songs with intro, chorus, and outro",
                "Custom lyrics and style definition",
                "Stem separation for vocals and instruments",
                "High fidelity audio synthesis"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Lo-Fi Coding Anthem",
                    prompt = "Chill lo-fi hip hop beat with warm rhodes keys, soft vinyl crackle, gentle rain sound effects, mellow bassline, relaxed tempo."
                ),
                PromptExample(
                    title = "Indie Folk Ballad",
                    prompt = "A warm acoustic indie folk song with fingerpicked guitar, intimate female vocals, nostalgic lyrics about autumn road trips."
                )
            )
        ),
        AiTool(
            id = "elevenlabs",
            name = "ElevenLabs",
            tagline = "Unmatched emotional voice cloning and realistic text-to-speech.",
            category = "Audio",
            description = "ElevenLabs produces the most realistic, human-sounding AI voices in the world. It captures intonation, breathing, laughter, and accent with zero robotic artifacts.",
            pricing = "Freemium",
            rating = 4.9f,
            reviewsCount = "33.7k",
            badge = "Editor's Pick",
            websiteUrl = "https://elevenlabs.io",
            tags = listOf("Voice Cloning", "Text to Speech", "Emotional Voice", "Audiobooks"),
            features = listOf(
                "Instant voice cloning from 1-minute audio",
                "Emotional stability and exaggeration slider",
                "Voice changer & sound effect generation",
                "Support for 29 languages"
            ),
            examplePrompts = listOf(
                PromptExample(
                    title = "Narrator Voice Script",
                    prompt = "In a deep, comforting documentary voice: 'Across the digital horizon, artificial intelligence was no longer just a tool, but an extension of human curiosity.'"
                )
            )
        )
    )

    fun getToolById(id: String): AiTool? {
        return tools.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
    }

    fun getToolsByCategory(category: String): List<AiTool> {
        return tools.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun searchTools(query: String, category: String? = null): List<AiTool> {
        val filtered = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) {
            tools
        } else {
            getToolsByCategory(category)
        }

        if (query.isBlank()) return filtered

        val lowerQuery = query.trim().lowercase()
        return filtered.filter { tool ->
            tool.name.lowercase().contains(lowerQuery) ||
                tool.tagline.lowercase().contains(lowerQuery) ||
                tool.description.lowercase().contains(lowerQuery) ||
                tool.category.lowercase().contains(lowerQuery) ||
                tool.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }
}
