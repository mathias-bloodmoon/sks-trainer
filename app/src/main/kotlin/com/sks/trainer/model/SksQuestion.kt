package com.sks.trainer.model

import kotlinx.serialization.Serializable

/**
 * Data model for an SKS question as defined in sks.json
 */
@Serializable
data class SksQuestion(
    val id: String,
    val number: Int,
    val category: String,
    val subcategory: String? = null,
    val question: String,
    val answer: String,
    val keywords: List<String> = emptyList(),
    val question_image: String? = null,
    val answer_image: String? = null
)
