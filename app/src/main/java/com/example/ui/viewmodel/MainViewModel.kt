package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiVoiceService
import com.example.data.api.VoiceApiResult
import com.example.data.model.AiNewsItem
import com.example.data.model.AiTool
import com.example.data.repository.AiNewsRepository
import com.example.data.repository.AiToolRepository
import com.example.data.repository.SavedArticlesRepository
import com.example.util.AiVoiceManager
import kotlinx.coroutines.launch

sealed class VoiceAssistantState {
    object Idle : VoiceAssistantState()
    data class Listening(val partialText: String = "", val rmsLevel: Float = 1f) : VoiceAssistantState()
    data class Processing(val spokenQuery: String) : VoiceAssistantState()
    data class Speaking(val spokenQuery: String, val answer: String) : VoiceAssistantState()
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val failedQuery: String = ""
    ) : VoiceAssistantState()
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

    // -------------------------------------------------------------------------
    // AUTOMATIC LATEST AI NEWS (Real RSS, Caching, Offline Support, Refresh)
    // -------------------------------------------------------------------------
    var newsList by mutableStateOf<List<AiNewsItem>>(AiNewsRepository.getCachedOrFallbackNews())
        private set

    var isNewsLoading by mutableStateOf(false)
        private set

    var newsErrorMessage by mutableStateOf<String?>(null)
        private set

    var isNewsOffline by mutableStateOf(false)
        private set

    val dailyNews: List<AiNewsItem>
        get() = newsList

    fun loadNews(context: Context? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (context != null) {
                SavedArticlesRepository.init(context)
            }
            isNewsLoading = true
            newsErrorMessage = null
            try {
                val result = AiNewsRepository.fetchNews(context = context, forceRefresh = forceRefresh)
                newsList = result.items
                isNewsOffline = result.isOffline
                newsErrorMessage = result.errorMessage
            } catch (e: Exception) {
                Log.w("MainViewModel", "Failed to fetch real news: ${e.message}")
                newsErrorMessage = "Unable to fetch live news. Showing cached articles."
                val cached = AiNewsRepository.getCachedOrFallbackNews(context)
                if (cached.isNotEmpty()) {
                    newsList = cached
                    isNewsOffline = true
                }
            } finally {
                isNewsLoading = false
            }
        }
    }

    val savedArticles: List<AiNewsItem>
        get() = SavedArticlesRepository.savedArticles

    val bookmarkedNewsIds: List<String>
        get() = SavedArticlesRepository.savedArticles.map { it.id }

    var selectedNewsForDetail by mutableStateOf<AiNewsItem?>(null)
        private set

    fun openNewsDetail(news: AiNewsItem) {
        selectedNewsForDetail = news
    }

    fun closeNewsDetail() {
        selectedNewsForDetail = null
    }

    fun initSavedArticles(context: Context) {
        SavedArticlesRepository.init(context)
    }

    fun isArticleSaved(article: AiNewsItem): Boolean {
        return SavedArticlesRepository.isSaved(article)
    }

    fun isNewsBookmarked(newsId: String): Boolean {
        return SavedArticlesRepository.isSaved(newsId)
    }

    fun toggleSaveArticle(context: Context?, article: AiNewsItem): Boolean {
        return SavedArticlesRepository.toggleSave(context, article)
    }

    fun toggleBookmarkNews(newsId: String, context: Context? = null) {
        val found = dailyNews.find { it.id == newsId }
            ?: savedArticles.find { it.id == newsId }
            ?: AiNewsItem(
                id = newsId,
                title = "News $newsId",
                summary = "AI breakthrough announcement",
                source = "AI News"
            )
        SavedArticlesRepository.toggleSave(context, found)
    }

    init {
        // Automatically load latest real AI news on startup
        loadNews()
    }

    // Creative Tool Generation states
    var summaryInputText by mutableStateOf("")
    var summaryOutputText by mutableStateOf("")
    var isGeneratingSummary by mutableStateOf(false)

    fun generateSummary(style: String = "Bullet Points") {
        if (summaryInputText.isBlank()) return
        isGeneratingSummary = true
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

    // -------------------------------------------------------------------------
    // VOICE ASSISTANT STATE MANAGEMENT & REAL AI REPLIES
    // -------------------------------------------------------------------------
    var voiceState by mutableStateOf<VoiceAssistantState>(VoiceAssistantState.Idle)
        private set

    var isTtsSpeaking by mutableStateOf(false)

    var voiceSpeechRate by mutableStateOf(1.0f)
        private set

    var preferredVoiceGender by mutableStateOf("natural") // "natural", "female", "male"
        private set

    var isSpeechOutputEnabled by mutableStateOf(true)
        private set

    @Volatile
    private var isGenerating = false

    fun updateSpeechRate(rate: Float) {
        voiceSpeechRate = rate
        AiVoiceManager.speechRate = rate
    }

    fun updatePreferredVoiceGender(gender: String) {
        preferredVoiceGender = gender
        AiVoiceManager.preferredStyle = gender
    }

    fun toggleSpeechOutput() {
        isSpeechOutputEnabled = !isSpeechOutputEnabled
    }

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
            text = "Namaste! Main PickAI hoon. Center orb dabayein ya text se sawal poochein, main real AI response dunga."
        )
    )

    fun sendVoiceMessage(query: String, onAnswerReady: (answer: String) -> Unit = {}) {
        val q = query.trim()
        if (q.isBlank()) return
        processVoiceInputWithGemini(q, onAnswerReady)
    }

    /**
     * Processes user voice or text input directly with Gemini 3.5 Flash API.
     * Never silently falls back to fake or unrelated answers.
     * Prevents duplicate requests.
     */
    fun processVoiceInputWithGemini(
        spokenQuery: String,
        onAnswerReady: (answer: String) -> Unit
    ) {
        val query = spokenQuery.trim()
        if (query.isBlank()) {
            voiceState = VoiceAssistantState.Error(
                message = "Aawaz detect nahi hui. Kripya orb dabakar dobara bolein.",
                canRetry = false
            )
            return
        }

        if (isGenerating) {
            Log.d("MainViewModel", "Ignoring duplicate request while already generating")
            return
        }
        isGenerating = true

        // 1. Transition state to Processing and record user transcript
        voiceState = VoiceAssistantState.Processing(spokenQuery = query)
        voiceMessages.add(VoiceMessage(sender = "user", text = query))

        viewModelScope.launch {
            try {
                // 2. Call Gemini API
                when (val result = GeminiVoiceService.generateVoiceAnswer(query)) {
                    is VoiceApiResult.Success -> {
                        val answer = result.answer
                        // 3. Update conversation history
                        voiceMessages.add(VoiceMessage(sender = "pickai", text = answer))
                        // 4. Transition to Speaking and trigger TTS
                        voiceState = VoiceAssistantState.Speaking(spokenQuery = query, answer = answer)
                        onAnswerReady(answer)
                    }
                    is VoiceApiResult.Error -> {
                        Log.w("MainViewModel", "Gemini Voice API failed: ${result.technicalDetail}")
                        // Transition to Error with clear user feedback and retry ability
                        voiceState = VoiceAssistantState.Error(
                            message = result.userFriendlyMessage,
                            canRetry = true,
                            failedQuery = query
                        )
                    }
                }
            } finally {
                isGenerating = false
            }
        }
    }

    fun retryVoiceQuery(onAnswerReady: (answer: String) -> Unit) {
        val current = voiceState
        if (current is VoiceAssistantState.Error && current.failedQuery.isNotBlank()) {
            processVoiceInputWithGemini(current.failedQuery, onAnswerReady)
        } else {
            resetVoiceState()
        }
    }
}
