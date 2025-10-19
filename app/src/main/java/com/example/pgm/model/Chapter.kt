package com.example.pgm.model

data class Chapter(
    val id: Int = 0,
    val comicId: Int,
    val chapterNumber: Int,
    val title: String,
    val thumbnailUrl: String? = null,
    val releaseDate: String? = null,
    val likeCount: Int = 0,
    // legacy flags kept for backward compatibility; prefer using freeDate when present
    val isLocked: Boolean = false,
    val cost: Int = 0, // Cost in coins if locked
    val freeDays: Int = 0, // legacy: days until free (used when freeDate is not provided)
    val freeDate: Long? = null, // epoch millis when chapter becomes free/unlocked
    val pages: Int? = null,
    val localPath: String? = null,
    val remoteUrl: String? = null
) {
    /**
     * Returns whether the chapter is currently locked. If freeDate is set, that takes precedence.
     */
    fun isCurrentlyLocked(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return freeDate?.let { nowMillis < it } ?: isLocked
    }

    /**
     * Returns remaining days until the chapter becomes free. If already free or no date, returns 0.
     */
    fun daysUntilFree(nowMillis: Long = System.currentTimeMillis()): Int {
        freeDate?.let {
            val diff = it - nowMillis
            if (diff <= 0) return 0
            val days = (diff / (1000L * 60L * 60L * 24L)).toInt()
            return if (days > 0) days else 0
        }
        return freeDays
    }
}