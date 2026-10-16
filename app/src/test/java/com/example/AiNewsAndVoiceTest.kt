package com.example

import com.example.data.api.VoiceApiResult
import com.example.data.model.AiNewsItem
import com.example.data.repository.AiNewsRepository
import com.example.util.AiVoiceManager
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class AiNewsAndVoiceTest {

    @Test
    fun testCleanTextForSpeech_removesMarkdownAndUrls() {
        val input = "Here is **bold** text with `code` and [link](https://example.com) and https://google.com/test *italic*"
        val cleaned = AiVoiceManager.cleanTextForSpeech(input)

        assertFalse("Should not contain asterisks", cleaned.contains("*"))
        assertFalse("Should not contain backticks", cleaned.contains("`"))
        assertFalse("Should not contain markdown link syntax", cleaned.contains("[link]("))
        assertFalse("Should not contain http url", cleaned.contains("https://"))
        assertTrue("Should preserve readable words", cleaned.contains("bold"))
        assertTrue("Should preserve code content", cleaned.contains("code"))
    }

    @Test
    fun testCleanTextForSpeech_stripsTechnicalFormatting() {
        val input = """
            ### Top AI Updates
            • Point 1: Gemini 3.5 Flash released
            • Point 2: Cost is 70% lower
        """.trimIndent()

        val cleaned = AiVoiceManager.cleanTextForSpeech(input)
        assertFalse("Should not contain hash heading", cleaned.contains("###"))
        assertFalse("Should not contain bullet points", cleaned.contains("•"))
        assertTrue(cleaned.contains("Gemini 3.5 Flash released"))
    }

    @Test
    fun testDetectLocale_hindiDevanagari() {
        val hindiText = "नमस्ते, आज का मौसम कैसा है?"
        val detected = AiVoiceManager.detectLocale(hindiText)
        assertEquals("hi", detected.language)
        assertEquals("IN", detected.country)
    }

    @Test
    fun testDetectLocale_hinglishMarkers() {
        val hinglishText = "Mujhe coding ke liye sabse accha AI tool bataiye kaunsa hai"
        val detected = AiVoiceManager.detectLocale(hinglishText)
        assertEquals("hi", detected.language)
        assertEquals("IN", detected.country)
    }

    @Test
    fun testDetectLocale_pureEnglish() {
        val englishText = "Summarize the latest developments in generative AI and robotics"
        val detected = AiVoiceManager.detectLocale(englishText)
        assertEquals("en", detected.language)
    }

    @Test
    fun testAiNewsSortingAndDeduplication() {
        val now = System.currentTimeMillis()
        val item1 = AiNewsItem(
            id = "1",
            title = "OpenAI launches new reasoning model",
            summary = "Summary 1",
            source = "TechCrunch",
            pubDateMillis = now - 100000
        )
        // Duplicate title with different casing and whitespace
        val item2 = AiNewsItem(
            id = "2",
            title = "  openai launches NEW reasoning model! ",
            summary = "Summary 2 (duplicate)",
            source = "Google News",
            pubDateMillis = now - 200000
        )
        // Newer distinct item
        val item3 = AiNewsItem(
            id = "3",
            title = "Google announces Gemini 3.5 Flash architecture",
            summary = "Summary 3",
            source = "VentureBeat",
            pubDateMillis = now - 50000
        )

        val processed = AiNewsRepository.deduplicateAndSort(listOf(item1, item2, item3))

        assertEquals("Duplicates should be removed", 2, processed.size)
        assertEquals("Newest item should be first", "3", processed.first().id)
    }

    @Test
    fun testCalculateRelativeTime() {
        val now = System.currentTimeMillis()
        val recentMillis = now - (15 * 60 * 1000) // 15 mins ago
        val hoursMillis = now - (3 * 3600 * 1000) // 3 hours ago
        val daysMillis = now - (2 * 86400 * 1000) // 2 days ago

        val rel1 = AiNewsRepository.formatRelativeTime(recentMillis, now)
        val rel2 = AiNewsRepository.formatRelativeTime(hoursMillis, now)
        val rel3 = AiNewsRepository.formatRelativeTime(daysMillis, now)

        assertTrue("Should format minutes", rel1.contains("m ago") || rel1 == "Just now")
        assertTrue("Should format hours", rel2.contains("h ago"))
        assertTrue("Should format days", rel3.contains("d ago"))
    }

    @Test
    fun testRfc822DateParsing() {
        val rfcDate = "Fri, 16 Oct 2026 10:30:00 GMT"
        val parsed = AiNewsRepository.parsePubDateToMillis(rfcDate)
        assertTrue("Parsed millis should be positive", parsed > 0)
    }
}
