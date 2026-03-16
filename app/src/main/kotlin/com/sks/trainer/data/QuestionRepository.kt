package com.sks.trainer.data

import android.content.Context
import com.sks.trainer.model.SksQuestion
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Repository to handle loading and filtering SKS questions from the local JSON asset.
 */
class QuestionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Loads all questions from the assets/sks.json file.
     */
    fun loadQuestions(): List<SksQuestion> {
        return try {
            val jsonString = context.assets.open("sks.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<SksQuestion>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Returns questions filtered by category.
     */
    fun getQuestionsByCategory(category: String): List<SksQuestion> {
        return loadQuestions().filter { it.category.equals(category, ignoreCase = true) }
    }

    /**
     * Returns 30 random questions from a specific category or from all categories.
     */
    fun getRandomQuestions(category: String? = null, count: Int = 30): List<SksQuestion> {
        val all = if (category == null || category == "Zufällig") {
            loadQuestions()
        } else {
            getQuestionsByCategory(category)
        }
        return all.shuffled().take(count)
    }
}
