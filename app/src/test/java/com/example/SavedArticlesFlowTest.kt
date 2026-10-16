package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AiNewsItem
import com.example.data.repository.SavedArticlesRepository
import com.example.ui.viewmodel.MainViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SavedArticlesFlowTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SavedArticlesRepository.clearForTest()
    }

    @Test
    fun testSaveArticle_appearsInSavedImmediately() {
        val viewModel = MainViewModel()
        val article = AiNewsItem(
            id = "top_story_1",
            title = "Next-Gen Autonomous Agent Ensembles Reach Commercial Full-Stack Deployment",
            summary = "Benchmark reports show multi-agent systems now autonomously plan, test, and deploy applications.",
            source = "TechCrunch",
            url = "https://techcrunch.com/2026/10/16/autonomous-agents"
        )

        // 1. Initial state: not saved
        assertFalse(viewModel.isArticleSaved(article))
        assertTrue(viewModel.savedArticles.isEmpty())

        // 2. Save the article
        val saved = viewModel.toggleSaveArticle(context, article)
        assertTrue(saved)

        // 3. Confirm article appears immediately in savedArticles
        assertEquals(1, viewModel.savedArticles.size)
        assertEquals("top_story_1", viewModel.savedArticles.first().id)
        assertEquals(article.title, viewModel.savedArticles.first().title)
        assertTrue(viewModel.isArticleSaved(article))
    }

    @Test
    fun testPersistence_survivesAppRestart() {
        val article = AiNewsItem(
            id = "story_persistence",
            title = "Quantum Tensor Processing Accelerates On-Device Neural Synthesis",
            summary = "Edge silicon achieves real-time inference.",
            source = "MIT Technology Review",
            url = "https://technologyreview.com/ai/quantum-tensor"
        )

        // 1. Save article via repository
        SavedArticlesRepository.toggleSave(context, article)
        assertTrue(SavedArticlesRepository.isSaved(article))

        // 2. Simulate closing the app by resetting in-memory repository cache
        SavedArticlesRepository.clearForTest()
        assertTrue("In-memory list is cleared", SavedArticlesRepository.savedArticles.isEmpty())

        // 3. Simulate opening the app fresh: re-initialize from local storage
        SavedArticlesRepository.init(context)

        // 4. Confirm the exact article was restored from local persistent storage
        assertEquals(1, SavedArticlesRepository.savedArticles.size)
        val restored = SavedArticlesRepository.savedArticles.first()
        assertEquals(article.title, restored.title)
        assertEquals(article.url, restored.url)
        assertEquals(article.source, restored.source)
    }

    @Test
    fun testUnsaveArticle_removesFromSavedImmediately() {
        val viewModel = MainViewModel()
        val article = AiNewsItem(
            id = "story_unsave",
            title = "Diffusion Video Latencies Drop Under 10ms",
            summary = "Interactive video generation benchmark.",
            source = "VentureBeat",
            url = "https://venturebeat.com/ai/video-latency"
        )

        // Save
        viewModel.toggleSaveArticle(context, article)
        assertEquals(1, viewModel.savedArticles.size)
        assertTrue(viewModel.isArticleSaved(article))

        // Unsave
        val stillSaved = viewModel.toggleSaveArticle(context, article)
        assertFalse(stillSaved)
        assertEquals(0, viewModel.savedArticles.size)
        assertFalse(viewModel.isArticleSaved(article))

        // Verify disk storage is also updated
        val fromStorage = SavedArticlesRepository.loadFromStorage(context)
        assertTrue("Storage should reflect unsaved state", fromStorage.isEmpty())
    }

    @Test
    fun testSaveMultipleArticles_firstArticleUnaffected() {
        val viewModel = MainViewModel()
        val article1 = AiNewsItem(
            id = "art_1",
            title = "Article One",
            summary = "Summary One",
            source = "TechCrunch",
            url = "https://techcrunch.com/article-1"
        )
        val article2 = AiNewsItem(
            id = "art_2",
            title = "Article Two",
            summary = "Summary Two",
            source = "Ars Technica",
            url = "https://arstechnica.com/article-2"
        )

        // Save article 1
        viewModel.toggleSaveArticle(context, article1)
        assertEquals(1, viewModel.savedArticles.size)
        assertTrue(viewModel.isArticleSaved(article1))

        // Save article 2
        viewModel.toggleSaveArticle(context, article2)
        assertEquals(2, viewModel.savedArticles.size)

        // Confirm article 1 is unaffected and article 2 is saved
        assertTrue(viewModel.isArticleSaved(article1))
        assertTrue(viewModel.isArticleSaved(article2))

        // Unsave article 2; confirm article 1 remains intact
        viewModel.toggleSaveArticle(context, article2)
        assertEquals(1, viewModel.savedArticles.size)
        assertTrue(viewModel.isArticleSaved(article1))
        assertFalse(viewModel.isArticleSaved(article2))
    }

    @Test
    fun testDeduplication_preventsDuplicateSaves() {
        val viewModel = MainViewModel()
        val articleA = AiNewsItem(
            id = "rss_feed_100",
            title = "Autonomous Coding Assistants Outperform Human Benchmarks",
            summary = "Original RSS feed item",
            source = "TechCrunch",
            url = "https://techcrunch.com/2026/10/coding-assistants/"
        )
        // Same article with slightly different whitespace, trailing slash, or ID
        val articleADuplicate = AiNewsItem(
            id = "google_news_200",
            title = "Autonomous Coding Assistants Outperform Human Benchmarks",
            summary = "Google News aggregated copy",
            source = "Google News",
            url = "https://techcrunch.com/2026/10/coding-assistants"
        )

        // Save first copy
        SavedArticlesRepository.saveArticle(context, articleA)
        assertEquals(1, viewModel.savedArticles.size)

        // Attempt to save duplicate copy
        SavedArticlesRepository.saveArticle(context, articleADuplicate)
        assertEquals("Duplicate should not be added", 1, viewModel.savedArticles.size)
    }
}
