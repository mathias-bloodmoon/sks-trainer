package com.sks.trainer.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

/**
 * Data model for user statistics.
 */
@Serializable
data class UserStats(
    val learningInteractions: Int = 0,
    val testsTaken: Int = 0,
    val correctAnswers: Int = 0,
    val totalTestQuestions: Int = 0
)

/**
 * Manages saving and loading user statistics to a local JSON file.
 */
class StatsManager(private val context: Context) {
    private val fileName = "user_stats.json"
    private val json = Json { prettyPrint = true }

    /**
     * Loads stats from local file.
     */
    fun loadStats(): UserStats {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            try {
                json.decodeFromString<UserStats>(file.readText())
            } catch (e: Exception) {
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
        // TODO: Sync to online JSON in private section of repo as per requirement
    }

    /**
     * Increments learning interaction count.
     */
    fun incrementLearning() {
        val current = loadStats()
        saveStats(current.copy(learningInteractions = current.learningInteractions + 1))
    }

    /**
     * Records a test result.
     */
    fun recordTestResult(correct: Int, total: Int) {
        val current = loadStats()
        saveStats(current.copy(
            testsTaken = current.testsTaken + 1,
            correctAnswers = current.correctAnswers + correct,
            totalTestQuestions = current.totalTestQuestions + total
        ))
    }
}
