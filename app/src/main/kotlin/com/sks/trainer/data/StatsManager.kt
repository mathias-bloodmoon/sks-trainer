@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.sks.trainer.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * Data model for statistics per category.
 */
@Serializable
data class CategoryStats(
    val learningInteractions: Int = 0,
    val testsTaken: Int = 0,
    val correctAnswers: Int = 0,
    val totalTestQuestions: Int = 0
)

/**
 * Data model for global user statistics, including a map for category-specific stats.
 */
@Serializable
data class UserStats(
    val globalStats: CategoryStats = CategoryStats(),
    val categoryStats: Map<String, CategoryStats> = emptyMap(),
    val bookmarkedQuestionIds: Set<String> = emptySet()
)

/**
 * Manages saving and loading user statistics to a local JSON file.
 */
class StatsManager(private val context: Context) {
    // Neuer Dateiname, da sich die Struktur der JSON-Datei stark geändert hat
    // So vermeiden wir Abstürze beim Einlesen alter, nicht kompatibler Speicherstände.
    private val fileName = "user_stats_v2.json" 
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /**
     * Loads stats from local file.
     */
    fun loadStats(): UserStats {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            try {
                json.decodeFromString<UserStats>(file.readText())
            } catch (_: Exception) {
                UserStats()
            }
        } else {
            UserStats()
        }
    }

    /**
     * Saves stats to local file.
     */
    fun saveStats(stats: UserStats) {
        val file = File(context.filesDir, fileName)
        file.writeText(json.encodeToString(stats))
    }

    /**
     * Resets all statistics.
     */
    fun resetAllStats() {
        val current = loadStats()
        saveStats(current.copy(globalStats = CategoryStats(), categoryStats = emptyMap()))
    }

    /**
     * Resets statistics for a specific category.
     */
    fun resetCategoryStats(category: String) {
        val current = loadStats()
        val newMap = current.categoryStats.toMutableMap()
        newMap.remove(category)
        saveStats(current.copy(categoryStats = newMap))
    }

    /**
     * Toggles the bookmark status of a question.
     */
    fun toggleBookmark(questionId: String) {
        val current = loadStats()
        val newBookmarks = if (current.bookmarkedQuestionIds.contains(questionId)) {
            current.bookmarkedQuestionIds - questionId
        } else {
            current.bookmarkedQuestionIds + questionId
        }
        saveStats(current.copy(bookmarkedQuestionIds = newBookmarks))
    }

    /**
     * Checks if a question is bookmarked.
     */
    fun isBookmarked(questionId: String): Boolean {
        return loadStats().bookmarkedQuestionIds.contains(questionId)
    }

    /**
     * Returns all bookmarked question IDs.
     */
    fun getBookmarkedQuestionIds(): Set<String> {
        return loadStats().bookmarkedQuestionIds
    }

    /**
     * Increments learning interaction count globally and for the specific category.
     */
    fun incrementLearning(category: String) {
        val current = loadStats()
        val catStats = current.categoryStats[category] ?: CategoryStats()
        
        val updatedGlobal = current.globalStats.copy(
            learningInteractions = current.globalStats.learningInteractions + 1
        )
        val updatedCat = catStats.copy(
            learningInteractions = catStats.learningInteractions + 1
        )
        
        val newMap = current.categoryStats.toMutableMap()
        newMap[category] = updatedCat
        
        saveStats(current.copy(globalStats = updatedGlobal, categoryStats = newMap))
    }

    /**
     * Records a test result globally and for the specific category.
     */
    fun recordTestResult(category: String, correct: Int, total: Int) {
        val current = loadStats()
        val catStats = current.categoryStats[category] ?: CategoryStats()
        
        val updatedGlobal = current.globalStats.copy(
            testsTaken = current.globalStats.testsTaken + 1,
            correctAnswers = current.globalStats.correctAnswers + correct,
            totalTestQuestions = current.globalStats.totalTestQuestions + total
        )
        val updatedCat = catStats.copy(
            testsTaken = catStats.testsTaken + 1,
            correctAnswers = catStats.correctAnswers + correct,
            totalTestQuestions = catStats.totalTestQuestions + total
        )
        
        val newMap = current.categoryStats.toMutableMap()
        newMap[category] = updatedCat
        
        saveStats(current.copy(globalStats = updatedGlobal, categoryStats = newMap))
    }
}
