package com.sks.trainer.data

import android.content.Context
import com.sks.trainer.model.SksQuestion
import kotlinx.serialization.json.Json

/**
 * Repository to handle loading and filtering SKS questions from the local JSON asset.
 */
class QuestionRepository(private val context: Context) {

    companion object {
        // Caching der Fragen im Arbeitsspeicher, um ständiges JSON-Parsen (Disk I/O) zu vermeiden.
        private var cachedQuestions: List<SksQuestion>? = null
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val statsManager = StatsManager(context)

    /**
     * Loads all questions from the assets/sks.json file.
     */
    fun loadQuestions(): List<SksQuestion> {
        cachedQuestions?.let { return it }
        
        return try {
            val jsonString = context.assets.open("sks.json").bufferedReader().use { it.readText() }
            val parsedList = json.decodeFromString<List<SksQuestion>>(jsonString)
            cachedQuestions = parsedList
            parsedList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Returns questions filtered by category and optionally by bookmarks.
     */
    fun getQuestions(category: String, bookmarksOnly: Boolean = false): List<SksQuestion> {
        val all = loadQuestions()
        val filteredByCategory = if (category == "Zufällig") {
            all
        } else {
            all.filter { it.category.equals(category, ignoreCase = true) }
        }

        return if (bookmarksOnly) {
            val bookmarks = statsManager.getBookmarkedQuestionIds()
            filteredByCategory.filter { it.id in bookmarks }
        } else {
            filteredByCategory
        }
    }

    /**
     * Returns the count of all questions in a category.
     */
    fun getQuestionCount(category: String): Int {
        val all = loadQuestions()
        return if (category == "Zufällig") {
            all.size
        } else {
            all.count { it.category.equals(category, ignoreCase = true) }
        }
    }

    /**
     * Returns the count of bookmarked questions in a category.
     */
    fun getBookmarkCount(category: String): Int {
        val all = loadQuestions()
        val bookmarks = statsManager.getBookmarkedQuestionIds()
        return if (category == "Zufällig") {
            all.count { it.id in bookmarks }
        } else {
            all.count { it.category.equals(category, ignoreCase = true) && it.id in bookmarks }
        }
    }
}
