package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.data.model.AiNewsItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Single shared repository for managing and persisting user-saved AI news articles.
 * Synchronizes across Top Stories (HomeScreen) and the Saved screen (FavoritesScreen).
 */
object SavedArticlesRepository {

    private const val TAG = "SavedArticlesRepo"
    private const val SAVED_FILE_NAME = "pickai_saved_articles.json"
    private const val PREFS_NAME = "pickai_saved_prefs"
    private const val PREFS_KEY_ARTICLES = "saved_articles_json"

    // Observable Compose state list - updates UI instantly across all screens
    private val _savedArticles = mutableStateListOf<AiNewsItem>()
    val savedArticles: List<AiNewsItem> get() = _savedArticles

    private var isInitialized = false

    private fun safeLogD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) {}
    }

    private fun safeLogW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (_: Throwable) {}
    }

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try { Log.e(tag, msg, tr) } catch (_: Throwable) {}
    }

    /**
     * Initializes and loads saved articles from local persistent disk storage.
     */
    fun init(context: Context): List<AiNewsItem> {
        if (!isInitialized) {
            val loaded = loadFromStorage(context)
            _savedArticles.clear()
            _savedArticles.addAll(loaded)
            isInitialized = true
            safeLogD(TAG, "Initialized with ${_savedArticles.size} saved articles")
        }
        return _savedArticles
    }

    /**
     * Checks if an article is already saved using canonical URL, stable ID, or normalized title.
     */
    fun isSaved(article: AiNewsItem): Boolean {
        return _savedArticles.any { matches(it, article) }
    }

    /**
     * Checks if an article with the given ID or URL string is saved.
     */
    fun isSaved(idOrUrl: String): Boolean {
        if (idOrUrl.isBlank()) return false
        val clean = idOrUrl.trim().removeSuffix("/").lowercase()
        return _savedArticles.any {
            it.id.trim().equals(clean, ignoreCase = true) ||
            (it.url.isNotBlank() && it.url.trim().removeSuffix("/").lowercase() == clean)
        }
    }

    /**
     * Toggles the saved state of an article.
     * If saved, removes it. If unsaved, adds it to the top.
     * Returns true if now saved, false if removed.
     */
    fun toggleSave(context: Context?, article: AiNewsItem): Boolean {
        val existingIndex = _savedArticles.indexOfFirst { matches(it, article) }
        val nowSaved: Boolean

        if (existingIndex >= 0) {
            _savedArticles.removeAt(existingIndex)
            nowSaved = false
            safeLogD(TAG, "Removed saved article: ${article.title}")
        } else {
            // Ensure unique and prepend to top
            _savedArticles.add(0, article)
            nowSaved = true
            safeLogD(TAG, "Added saved article: ${article.title}")
        }

        if (context != null) {
            persistToStorage(context, _savedArticles.toList())
        }
        return nowSaved
    }

    /**
     * Explicitly saves an article if not already present.
     */
    fun saveArticle(context: Context?, article: AiNewsItem) {
        if (!isSaved(article)) {
            _savedArticles.add(0, article)
            if (context != null) {
                persistToStorage(context, _savedArticles.toList())
            }
        }
    }

    /**
     * Explicitly removes an article by matching item.
     */
    fun removeArticle(context: Context?, article: AiNewsItem) {
        val existingIndex = _savedArticles.indexOfFirst { matches(it, article) }
        if (existingIndex >= 0) {
            _savedArticles.removeAt(existingIndex)
            if (context != null) {
                persistToStorage(context, _savedArticles.toList())
            }
        }
    }

    /**
     * Canonical matcher: Compares URL, then ID, then normalized title.
     */
    fun matches(saved: AiNewsItem, target: AiNewsItem): Boolean {
        // 1. Canonical article URL match
        if (saved.url.isNotBlank() && target.url.isNotBlank()) {
            val u1 = saved.url.trim().removeSuffix("/").lowercase()
            val u2 = target.url.trim().removeSuffix("/").lowercase()
            if (u1 == u2) return true
        }

        // 2. Stable unique ID match
        if (saved.id.isNotBlank() && target.id.isNotBlank() && saved.id.trim() == target.id.trim()) {
            return true
        }

        // 3. Exact normalized title match (ignores punctuation & casing)
        val t1 = saved.title.lowercase().replace(Regex("[^a-z0-9]"), "")
        val t2 = target.title.lowercase().replace(Regex("[^a-z0-9]"), "")
        if (t1.length >= 10 && t1 == t2) {
            return true
        }

        return false
    }

    /**
     * Persists saved articles to both a local private JSON file and SharedPreferences backup.
     */
    private fun persistToStorage(context: Context, items: List<AiNewsItem>) {
        try {
            val jsonArray = JSONArray()
            for (item in items) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("summary", item.summary)
                obj.put("source", item.source)
                obj.put("url", item.url)
                obj.put("pubDateMillis", item.pubDateMillis)
                obj.put("formattedDateTime", item.formattedDateTime)
                obj.put("timeAgo", item.timeAgo)
                obj.put("categoryTag", item.categoryTag)
                obj.put("impactBadge", item.impactBadge)
                obj.put("readTime", item.readTime)
                obj.put("fullAnalysis", item.fullAnalysis)

                val takeawaysArr = JSONArray()
                item.keyTakeaways.forEach { takeawaysArr.put(it) }
                obj.put("keyTakeaways", takeawaysArr)

                val toolsArr = JSONArray()
                item.relatedToolIds.forEach { toolsArr.put(it) }
                obj.put("relatedToolIds", toolsArr)

                jsonArray.put(obj)
            }

            val jsonString = jsonArray.toString()

            // 1. Write to private internal file
            val file = File(context.filesDir, SAVED_FILE_NAME)
            file.writeText(jsonString)

            // 2. Write to SharedPreferences as backup
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PREFS_KEY_ARTICLES, jsonString).apply()

            safeLogD(TAG, "Persisted ${items.size} articles to storage")
        } catch (e: Exception) {
            safeLogE(TAG, "Error persisting saved articles: ${e.message}", e)
        }
    }

    /**
     * Loads saved articles from private internal file, falling back to SharedPreferences.
     */
    fun loadFromStorage(context: Context): List<AiNewsItem> {
        var jsonString: String? = null

        // 1. Try internal filesDir first
        try {
            val file = File(context.filesDir, SAVED_FILE_NAME)
            if (file.exists()) {
                val text = file.readText().trim()
                if (text.isNotBlank()) {
                    jsonString = text
                }
            }
        } catch (e: Exception) {
            safeLogW(TAG, "Could not read from file: ${e.message}")
        }

        // 2. Fallback to SharedPreferences if file was empty or missing
        if (jsonString.isNullOrBlank()) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                jsonString = prefs.getString(PREFS_KEY_ARTICLES, null)
            } catch (e: Exception) {
                safeLogW(TAG, "Could not read from SharedPreferences: ${e.message}")
            }
        }

        if (jsonString.isNullOrBlank()) {
            return emptyList()
        }

        return parseJsonToArticles(jsonString)
    }

    fun parseJsonToArticles(jsonString: String): List<AiNewsItem> {
        val list = mutableListOf<AiNewsItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val takeaways = mutableListOf<String>()
                val tools = mutableListOf<String>()

                val takeawaysArr = obj.optJSONArray("keyTakeaways")
                if (takeawaysArr != null) {
                    for (k in 0 until takeawaysArr.length()) takeaways.add(takeawaysArr.getString(k))
                }

                val toolsArr = obj.optJSONArray("relatedToolIds")
                if (toolsArr != null) {
                    for (k in 0 until toolsArr.length()) tools.add(toolsArr.getString(k))
                }

                list.add(
                    AiNewsItem(
                        id = obj.optString("id", "saved_$i"),
                        title = obj.optString("title"),
                        summary = obj.optString("summary"),
                        source = obj.optString("source"),
                        url = obj.optString("url"),
                        pubDateMillis = obj.optLong("pubDateMillis", System.currentTimeMillis()),
                        formattedDateTime = obj.optString("formattedDateTime"),
                        timeAgo = obj.optString("timeAgo", "Saved"),
                        categoryTag = obj.optString("categoryTag", "Saved Story"),
                        impactBadge = obj.optString("impactBadge", "📌 Saved"),
                        readTime = obj.optString("readTime", "2 min read"),
                        keyTakeaways = takeaways,
                        fullAnalysis = obj.optString("fullAnalysis"),
                        relatedToolIds = tools
                    )
                )
            }
        } catch (e: Exception) {
            safeLogE(TAG, "Error parsing saved articles JSON: ${e.message}")
        }
        return list
    }

    /**
     * For unit testing: sets in-memory items directly and resets initialized state.
     */
    fun setSavedArticlesForTest(items: List<AiNewsItem>) {
        _savedArticles.clear()
        _savedArticles.addAll(items)
        isInitialized = true
    }

    fun clearForTest() {
        _savedArticles.clear()
        isInitialized = false
    }
}
