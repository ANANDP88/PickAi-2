package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiVoiceService
import com.example.data.model.AiNewsItem
import com.example.data.model.AiTool
import com.example.data.repository.AiNewsRepository
import com.example.data.repository.AiToolRepository
import kotlinx.coroutines.launch

sealed class VoiceAssistantState {
    object Idle : VoiceAssistantState()
    data class Listening(val partialText: String = "", val rmsLevel: Float = 1f) : VoiceAssistantState()
    data class Processing(val spokenQuery: String) : VoiceAssistantState()
    data class Speaking(val spokenQuery: String, val answer: String) : VoiceAssistantState()
    data class Error(val message: String) : VoiceAssistantState()
}

data class VoiceMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "pickai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel : ViewModel() {

    // Theme state
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    // Onboarding state - default true so main app and bottom nav are immediately active
    var hasSeenOnboarding by mutableStateOf(true)
        private set

    fun completeOnboarding() {
        hasSeenOnboarding = true
    }

    fun replayOnboarding() {
        hasSeenOnboarding = false
    }

    // Favorites state - pre-populate top favorites for instant engagement
    private val _favoriteIds = mutableStateListOf("chatgpt", "midjourney", "github_copilot")
    val favoriteIds: List<String> get() = _favoriteIds

    fun isFavorite(toolId: String): Boolean {
        return _favoriteIds.contains(toolId.lowercase())
    }

    fun toggleFavorite(toolId: String) {
        val id = toolId.lowercase()
        if (_favoriteIds.contains(id)) {
            _favoriteIds.remove(id)
        } else {
            _favoriteIds.add(id)
        }
    }

    // Search and filtering
    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf("All")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun selectCategory(category: String) {
        selectedCategory = category
    }

    val filteredTools: List<AiTool>
        get() = AiToolRepository.searchTools(searchQuery, selectedCategory)

    val favoriteTools: List<AiTool>
        get() = AiToolRepository.tools.filter { _favoriteIds.contains(it.id.lowercase()) }

    fun getToolById(toolId: String): AiTool? {
        return AiToolRepository.getToolById(toolId)
    }

    // Daily Selected AI News
    val dailyNews: List<AiNewsItem>
        get() = AiNewsRepository.dailyNews

    private val _bookmarkedNewsIds = mutableStateListOf<String>()
    val bookmarkedNewsIds: List<String> get() = _bookmarkedNewsIds

    var selectedNewsForDetail by mutableStateOf<AiNewsItem?>(null)
        private set

    fun openNewsDetail(news: AiNewsItem) {
        selectedNewsForDetail = news
    }

    fun closeNewsDetail() {
        selectedNewsForDetail = null
    }

    fun isNewsBookmarked(newsId: String): Boolean {
        return _bookmarkedNewsIds.contains(newsId)
    }

    fun toggleBookmarkNews(newsId: String) {
        if (_bookmarkedNewsIds.contains(newsId)) {
            _bookmarkedNewsIds.remove(newsId)
        } else {
            _bookmarkedNewsIds.add(newsId)
        }
    }

    // Creative Tool Generation states
    var summaryInputText by mutableStateOf("")
    var summaryOutputText by mutableStateOf("")
    var isGeneratingSummary by mutableStateOf(false)

    fun generateSummary(style: String = "Bullet Points") {
        if (summaryInputText.isBlank()) return
        isGeneratingSummary = true
        // Quick high quality structured summary generator
        val lines = summaryInputText.split("\n", ". ").filter { it.isNotBlank() }
        val bullets = lines.take(4).mapIndexed { i, l -> "• Key Insight ${i + 1}: ${l.trim().take(90)}..." }
        summaryOutputText = when (style) {
            "Bullet Points" -> "📋 Summary Highlights:\n\n" + bullets.joinToString("\n") + "\n\n💡 Actionable Takeaway: Streamline core workflow and focus on high-impact objectives."
            "Executive Brief" -> "📊 Executive Brief:\n\n${summaryInputText.take(180)}...\n\nRecommendation: Proceed with staged deployment and monitor key adoption metrics."
            else -> "⚡ Quick Takeaway:\n\n${summaryInputText.take(120)}..."
        }
        isGeneratingSummary = false
    }

    var codeLanguage by mutableStateOf("Kotlin")
    var codeGoal by mutableStateOf("")
    var generatedCodeOutput by mutableStateOf("")

    fun generateCode() {
        if (codeGoal.isBlank()) return
        generatedCodeOutput = when (codeLanguage) {
            "Kotlin" -> """
                // Generated by PickAI Code Assistant
                // Goal: $codeGoal
                
                class SolutionHelper {
                    fun execute(): Result<String> {
                        return try {
                            val result = processPayload()
                            Result.success("Success: " + result)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }
                    
                    private fun processPayload(): String {
                        // Optimized for Android Jetpack Compose & Coroutines
                        return "$codeGoal - Completed efficiently"
                    }
                }
            """.trimIndent()
            "Python" -> """
                # Generated by PickAI Code Assistant
                # Goal: $codeGoal
                
                def execute_task():
                    print("Executing: $codeGoal")
                    data = {"status": "success", "goal": "$codeGoal"}
                    return data
                    
                if __name__ == "__main__":
                    print(execute_task())
            """.trimIndent()
            else -> """
                // Generated by PickAI
                console.log("Goal: $codeGoal");
            """.trimIndent()
        }
    }

    // Voice assistant state management
    var voiceState by mutableStateOf<VoiceAssistantState>(VoiceAssistantState.Idle)
        private set

    var isTtsSpeaking by mutableStateOf(false)

    fun updateVoiceState(newState: VoiceAssistantState) {
        voiceState = newState
    }

    fun resetVoiceState() {
        voiceState = VoiceAssistantState.Idle
        isTtsSpeaking = false
    }

    val voiceMessages = mutableStateListOf<VoiceMessage>(
        VoiceMessage(
            sender = "pickai",
            text = "Namaste! Main PickAI hoon. Tap to Talk dabayein ya koi bhi sawal poochein, main turant audio ke saath jawab dunga."
        )
    )

    fun sendVoiceMessage(query: String): String {
        val q = query.trim()
        if (q.isBlank()) return ""
        voiceMessages.add(VoiceMessage(sender = "user", text = q))
        val answer = generateLocalAnswer(q)
        voiceMessages.add(VoiceMessage(sender = "pickai", text = answer))
        processVoiceInputWithGemini(q) {}
        return answer
    }

    /**
     * Proper State Management & Callback to process user voice input with Gemini API
     */
    fun processVoiceInputWithGemini(
        spokenQuery: String,
        onAnswerReady: (answer: String) -> Unit
    ) {
        val query = spokenQuery.trim()
        if (query.isBlank()) {
            voiceState = VoiceAssistantState.Idle
            return
        }

        // 1. Transition state to Processing
        voiceState = VoiceAssistantState.Processing(spokenQuery = query)
        voiceMessages.add(VoiceMessage(sender = "user", text = query))

        viewModelScope.launch {
            // 2. Call Gemini API (gemini-3.5-flash)
            val geminiAnswer = GeminiVoiceService.generateVoiceAnswer(query)
            val finalAnswer = if (!geminiAnswer.isNullOrBlank()) {
                geminiAnswer
            } else {
                generateLocalAnswer(query)
            }

            // 3. Update conversation history
            voiceMessages.add(VoiceMessage(sender = "pickai", text = finalAnswer))

            // 4. Transition state to Speaking and trigger audio playback callback
            voiceState = VoiceAssistantState.Speaking(spokenQuery = query, answer = finalAnswer)
            onAnswerReady(finalAnswer)
        }
    }

    fun generateLocalAnswer(query: String): String {
        val q = query.lowercase().trim()
        return when {
            // Daily AI News queries
            q.contains("news") || q.contains("khabar") || q.contains("samachar") || q.contains("update") || q.contains("today") || q.contains("aaj ki") -> {
                val topStory = AiNewsRepository.dailyNews.firstOrNull()
                if (topStory != null) {
                    "Aaj ki sabse badi AI news: ${topStory.title}. ${topStory.summary} Aap Home tab par Daily AI Pulse mein iska pura analysis padh sakte hain."
                } else {
                    "Aaj AI field mein multi-agent systems aur real-time video generation par tezi se breakthroughs aa rahe hain. Home screen par Daily AI pulse check karein."
                }
            }

            // Greetings & Introduction
            q.contains("namaste") || q.contains("hello") || q.contains("hi") || q.contains("kaise ho") || q.contains("how are you") || q.contains("who are you") || q.contains("kaun ho") ->
                "Namaste! Main PickAI hoon, aapka personal AI voice companion. Aap mujhse kisi bhi AI tool, coding help, ya daily AI news ke baare mein pooch sakte hain."

            // Image generation
            q.contains("image") || q.contains("photo") || q.contains("draw") || q.contains("art") || q.contains("picture") || q.contains("tasveer") ->
                "Photo aur photorealistic art ke liye Midjourney v6 sabse powerful hai. Agar aapko accurate text, logos, ya direct prompt editing chahiye to DALL-E 3 chunein."

            // Coding & Programming
            q.contains("code") || q.contains("coding") || q.contains("bug") || q.contains("kotlin") || q.contains("python") || q.contains("program") || q.contains("developer") ->
                "Coding ke liye Cursor AI aur GitHub Copilot sabse behtar hain. Complex algorithms aur architecture design ke liye Claude 3.5 Sonnet best perform karta hai."

            // Writing, Email & Summary
            q.contains("write") || q.contains("likh") || q.contains("email") || q.contains("summary") || q.contains("content") || q.contains("essay") || q.contains("patra") ->
                "Content drafting aur emails ke liye ChatGPT aur Notion AI sabse acche hain. PickAI ke Studio tab mein built-in Text Summarizer bhi available hai."

            // Audio & Music
            q.contains("music") || q.contains("audio") || q.contains("gana") || q.contains("voice") || q.contains("sound") || q.contains("awaz") || q.contains("song") ->
                "Full songs vocals ke saath banane ke liye Suno AI v3.5 use karein, aur ultra-realistic human voice cloning ke liye ElevenLabs sabse best hai."

            // Video Generation
            q.contains("video") || q.contains("film") || q.contains("animation") || q.contains("movie") ->
                "Cinematic AI video generation ke liye Runway Gen-3 Alpha aur realistic talking digital avatars ke liye HeyGen sabse aage hain."

            // Search & Research
            q.contains("search") || q.contains("research") || q.contains("dhoond") || q.contains("khoj") ->
                "Real-time internet search aur accurate citations ke liye Perplexity AI sabse best answer engine hai."

            else ->
                "Aapka sawaal bohot badhiya hai! PickAI par iske liye world-class tools mojood hain. Aap Home tab par Category filter dekh sakte hain ya mujhe specific task batayein."
        }
    }
}
