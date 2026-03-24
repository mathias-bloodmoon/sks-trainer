@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)
package com.sks.trainer.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    
    @SerialName("question_image")
    val questionImage: String? = null,
    
    @SerialName("answer_image")
    val answerImage: String? = null
)
