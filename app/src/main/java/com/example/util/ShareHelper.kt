package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.AiNewsItem
import com.example.data.model.AiTool
import java.net.URI

/**
 * Utility for sharing AI News, web links, AI answers, and curated tools
 * using Android's native share sheet (Intent.ACTION_SEND).
 */
object ShareHelper {

    /**
     * Validates and normalizes any user-input URL string.
     * Returns Pair(isValid, normalizedUrlOrErrorMessage).
     */
    fun validateAndNormalizeUrl(rawInput: String?): Pair<Boolean, String> {
        val trimmed = rawInput?.trim() ?: ""
        if (trimmed.isBlank()) {
            return Pair(false, "Please enter a URL to share")
        }

        // Must not contain whitespace
        if (trimmed.any { it.isWhitespace() }) {
            return Pair(false, "URL cannot contain spaces")
        }

        // Add https:// scheme if user omitted protocol
        val candidate = if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }

        return try {
            val uri = URI(candidate)
            val host = uri.host
            if (host.isNullOrBlank() || !host.contains(".") || host.startsWith(".") || host.endsWith(".")) {
                Pair(false, "Invalid web address. Please enter a valid URL (e.g. https://example.com)")
            } else {
                // Verify TLD is at least 2 characters
                val parts = host.split(".")
                val tld = parts.lastOrNull()
                if (tld == null || tld.length < 2 || !tld.all { it.isLetter() || it.isDigit() }) {
                    Pair(false, "Invalid domain extension. Please check the URL.")
                } else {
                    Pair(true, candidate)
                }
            }
        } catch (e: Exception) {
            Pair(false, "Invalid URL format: ${e.message ?: "malformed link"}")
        }
    }

    /**
     * Shares an AI News article via Android's native share sheet.
     * Includes the article title, short summary, source, and authentic article URL.
     * Never fabricates URLs if missing.
     */
    fun shareArticle(context: Context, article: AiNewsItem): Boolean {
        val title = article.title.trim()
        val source = article.source.trim()
        val summary = article.summary.trim()
        val originalUrl = article.url.trim()

        val shareContent = buildString {
            append(title)
            if (source.isNotBlank()) {
                append("\n\nSource: ").append(source)
            }
            if (summary.isNotBlank()) {
                append("\n\n").append(summary)
            }
            if (originalUrl.isNotBlank() && (originalUrl.startsWith("http://") || originalUrl.startsWith("https://"))) {
                append("\n\nRead original article: ").append(originalUrl)
            }
            append("\n\n— Shared via PickAI")
        }

        return launchShareSheet(
            context = context,
            subject = title,
            text = shareContent,
            chooserTitle = "Share AI News via"
        )
    }

    /**
     * Shares any custom or pasted web link via Android's native share sheet.
     * Validates the link first. Returns Pair(success, message).
     */
    fun shareGenericLink(context: Context, rawUrl: String, optionalNote: String = ""): Pair<Boolean, String> {
        val (isValid, resultText) = validateAndNormalizeUrl(rawUrl)
        if (!isValid) {
            return Pair(false, resultText)
        }

        val shareContent = buildString {
            if (optionalNote.isNotBlank()) {
                append(optionalNote.trim()).append("\n\n")
            }
            append(resultText)
            append("\n\n— Shared via PickAI")
        }

        val launched = launchShareSheet(
            context = context,
            subject = "Link from PickAI",
            text = shareContent,
            chooserTitle = "Share Link via"
        )

        return if (launched) {
            Pair(true, "Share sheet opened")
        } else {
            Pair(false, "Could not open share sheet")
        }
    }

    /**
     * Shares an AI voice answer or chat response.
     */
    fun shareAiAnswer(context: Context, answer: String, question: String? = null): Boolean {
        val trimmedAnswer = answer.trim()
        if (trimmedAnswer.isBlank()) return false

        val shareContent = buildString {
            if (!question.isNullOrBlank()) {
                append("Q: ").append(question.trim()).append("\n\n")
            }
            append(trimmedAnswer)
            append("\n\n— Shared via PickAI Live Voice")
        }

        return launchShareSheet(
            context = context,
            subject = "PickAI Voice Insight",
            text = shareContent,
            chooserTitle = "Share AI Answer via"
        )
    }

    /**
     * Shares a curated AI Tool with its website link.
     */
    fun shareTool(context: Context, tool: AiTool): Boolean {
        val shareContent = buildString {
            append("🤖 ").append(tool.name).append(" — ").append(tool.tagline)
            append("\n\n").append(tool.description)
            if (tool.websiteUrl.isNotBlank()) {
                append("\n\nTry it: ").append(tool.websiteUrl)
            }
            append("\n\n— Discovered on PickAI")
        }

        return launchShareSheet(
            context = context,
            subject = "${tool.name} on PickAI",
            text = shareContent,
            chooserTitle = "Share AI Tool via"
        )
    }

    /**
     * Internal helper to construct Intent.ACTION_SEND and invoke Android's native chooser.
     */
    private fun launchShareSheet(
        context: Context,
        subject: String?,
        text: String,
        chooserTitle: String
    ): Boolean {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                if (!subject.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            try {
                Toast.makeText(context, "Unable to open share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {}
            false
        }
    }
}
