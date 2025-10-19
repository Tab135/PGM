package com.example.pgm.model

import java.util.Date

/**
 * Enum for reading modes
 */
enum class ReadingMode {
    SINGLE_PAGE,
    DOUBLE_PAGE,
    SCROLL,
    FIT_WIDTH,
    FIT_HEIGHT
}

/**
 * Model to track user interaction history with comics
 */
data class UserComicHistory(
    val id: Int? = null,
    val userId: Int,
    val comicId: Int,
    val viewedChapters: List<Int> = emptyList(), // List of chapter IDs that user has viewed
    val latestViewedChapter: Int? = null, // Latest chapter ID the user viewed
    val currentPage: Int = 0, // Current page in the latest chapter
    val totalReadingTime: Long = 0L, // Total time spent reading in milliseconds
    val lastReadingDate: Date? = null, // Last time user read this comic
    val bookmarkedChapters: List<Int> = emptyList(), // List of bookmarked chapter IDs
    val likedChapters: List<Int> = emptyList(), // List of liked chapter IDs
    val purchasedChapters: List<Int> = emptyList(), // List of purchased chapter IDs
    val readingProgress: Float = 0.0f, // Overall progress in the comic (0.0 to 1.0)
    val isCompleted: Boolean = false, // Whether user has completed the entire comic
    val isFavorite: Boolean = false, // Whether user has marked this comic as favorite
    val notificationsEnabled: Boolean = true, // Whether user wants notifications for new chapters
    val lastViewedPage: Int = 0, // Last page viewed in the current chapter
    val readingStreak: Int = 0, // Consecutive days of reading
    val totalChaptersRead: Int = 0, // Total number of chapters completely read
    val preferredReadingMode: ReadingMode = ReadingMode.SINGLE_PAGE, // User's preferred reading mode
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)