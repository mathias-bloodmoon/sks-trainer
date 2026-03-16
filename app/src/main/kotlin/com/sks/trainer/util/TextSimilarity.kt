package com.sks.trainer.util

import kotlin.math.max

/**
 * Utility to calculate similarity between two strings.
 */
object TextSimilarity {

    /**
     * Calculates similarity percentage between two strings using Levenshtein distance.
     * 100% means identical, 0% means completely different.
     */
    fun calculateSimilarity(s1: String, s2: String): Int {
        val str1 = s1.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        val str2 = s2.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")

        if (str1 == str2) return 100
        if (str1.isEmpty() || str2.isEmpty()) return 0

        val distance = levenshtein(str1, str2)
        val maxLength = max(str1.length, str2.length)
        
        return ((maxLength - distance).toDouble() / maxLength * 100).toInt()
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
