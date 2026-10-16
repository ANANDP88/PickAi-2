package com.example

import com.example.data.repository.AiToolRepository
import com.example.ui.viewmodel.MainViewModel
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
        val newsList = com.example.data.repository.AiNewsRepository.dailyNews
        assertTrue(newsList.isNotEmpty())
        val firstStory = newsList.first()
        assertNotNull(firstStory.title)
        assertTrue(firstStory.keyTakeaways.isNotEmpty())
    }

    @Test
    fun testNewsBookmarkToggle() {
        val viewModel = MainViewModel()
        val testNewsId = "news_1"
        assertFalse(viewModel.isNewsBookmarked(testNewsId))
        viewModel.toggleBookmarkNews(testNewsId)
        assertTrue(viewModel.isNewsBookmarked(testNewsId))
        viewModel.toggleBookmarkNews(testNewsId)
        assertFalse(viewModel.isNewsBookmarked(testNewsId))
    }
}
