package com.example

import com.example.data.model.AiNewsItem
import com.example.data.repository.AiNewsRepository
import com.example.data.repository.AiToolRepository
import com.example.data.repository.SavedArticlesRepository
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ShareHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFavoriteToggle() {
        val viewModel = MainViewModel()
        val testToolId = "claude"

        val initialFav = viewModel.isFavorite(testToolId)
        viewModel.toggleFavorite(testToolId)
        assertEquals(!initialFav, viewModel.isFavorite(testToolId))

        viewModel.toggleFavorite(testToolId)
        assertEquals(initialFav, viewModel.isFavorite(testToolId))
    }

    @Test
    fun testCategoryFiltering() {
        val codingTools = AiToolRepository.getToolsByCategory("Coding")
        assertTrue(codingTools.isNotEmpty())
        assertTrue(codingTools.all { it.category == "Coding" })
    }

    @Test
    fun testSearchTools() {
        val searchResults = AiToolRepository.searchTools("midjourney")
        assertTrue(searchResults.isNotEmpty())
        assertEquals("midjourney", searchResults.first().id)
    }

    @Test
    fun testThemeToggle() {
        val viewModel = MainViewModel()
        val initialTheme = viewModel.isDarkMode
        viewModel.toggleTheme()
        assertEquals(!initialTheme, viewModel.isDarkMode)
    }

    @Test
    fun testOnboardingComplete() {
        val viewModel = MainViewModel()
        assertTrue(viewModel.hasSeenOnboarding)
        viewModel.replayOnboarding()
        assertFalse(viewModel.hasSeenOnboarding)
        viewModel.completeOnboarding()
        assertTrue(viewModel.hasSeenOnboarding)
    }

    @Test
    fun testDailyAiNews() {
        val newsList = AiNewsRepository.dailyNews
        assertTrue(newsList.isNotEmpty())
        val firstStory = newsList.first()
        assertNotNull(firstStory.title)
        assertTrue(firstStory.keyTakeaways.isNotEmpty())
    }

    @Test
    fun testNewsBookmarkToggle() {
        SavedArticlesRepository.clearForTest()
        val viewModel = MainViewModel()
        val testNewsId = "news_test_101"
        assertFalse(viewModel.isNewsBookmarked(testNewsId))
        viewModel.toggleBookmarkNews(testNewsId)
        assertTrue(viewModel.isNewsBookmarked(testNewsId))
        viewModel.toggleBookmarkNews(testNewsId)
        assertFalse(viewModel.isNewsBookmarked(testNewsId))
    }

    @Test
    fun testShareUrlValidationValidUrls() {
        val validSamples = listOf(
            "https://techcrunch.com/2026/04/10/ai-breakthrough",
            "http://openai.com/index/gpt-5-announcement",
            "anthropic.com/claude-3-7-sonnet",
            "https://deepmind.google/technologies/gemini/",
            "https://subdomain.example.co.uk/news?id=123#headline"
        )
        for (url in validSamples) {
            val (isValid, normalized) = ShareHelper.validateAndNormalizeUrl(url)
            assertTrue("Expected URL '$url' to be valid, but got: $normalized", isValid)
            assertTrue("Normalized URL should start with http:// or https://: $normalized",
                normalized.startsWith("http://") || normalized.startsWith("https://"))
        }
    }

    @Test
    fun testShareUrlValidationInvalidUrls() {
        val invalidSamples = listOf(
            "",
            "   ",
            "not a url at all",
            "http://",
            "https://",
            "https:// invalid spaces .com",
            "justwordswithoutdomain",
            "http://nodotdomain",
            "https://.com"
        )
        for (url in invalidSamples) {
            val (isValid, errorMsg) = ShareHelper.validateAndNormalizeUrl(url)
            assertFalse("Expected URL '$url' to be invalid, but passed validation", isValid)
            assertTrue("Expected error message for '$url'", errorMsg.isNotBlank())
        }
    }

    @Test
    fun testShareUrlNormalization() {
        val (isValid, normalized) = ShareHelper.validateAndNormalizeUrl("techcrunch.com/article/123")
        assertTrue(isValid)
        assertEquals("https://techcrunch.com/article/123", normalized)
    }

    @Test
    fun testShareArticleIntegrity() {
        val sampleArticle = AiNewsItem(
            id = "test_ai_share_1",
            title = "OpenAI Releases Next-Gen Reasoning Model",
            summary = "A massive leap forward in multihop logic and coding benchmarks.",
            source = "VentureBeat",
            url = "https://venturebeat.com/ai/openai-reasoning"
        )
        assertNotNull(sampleArticle.title)
        assertTrue(sampleArticle.url.startsWith("https://"))
        assertFalse(sampleArticle.summary.isBlank())
    }

    @Test
    fun testSavedArticlesRepositoryUniqueness() {
        SavedArticlesRepository.clearForTest()
        val article = AiNewsItem(
            id = "uniq_1",
            title = "Exclusive AI Launch",
            source = "TechCrunch",
            url = "https://techcrunch.com/exclusive-ai"
        )
        // First save
        val nowSaved1 = SavedArticlesRepository.toggleSave(null, article)
        assertTrue(nowSaved1)
        assertTrue(SavedArticlesRepository.isSaved(article))
        assertEquals(1, SavedArticlesRepository.savedArticles.size)

        // Second toggle removes it
        val nowSaved2 = SavedArticlesRepository.toggleSave(null, article)
        assertFalse(nowSaved2)
        assertFalse(SavedArticlesRepository.isSaved(article))
        assertEquals(0, SavedArticlesRepository.savedArticles.size)
    }
}
