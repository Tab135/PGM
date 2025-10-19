package com.example.pgm.Controller

import android.content.Context
import android.util.Log
import com.example.pgm.model.*
import com.example.pgm.model.Database.UserComicHistoryDatabaseHelper
import java.util.*

/**
 * Controller to manage user comic history operations
 */
class UserComicHistoryController(private val context: Context) {
    
    private val dbHelper = UserComicHistoryDatabaseHelper(context)

    companion object {
        private const val TAG = "UserComicHistoryController"
    }

    // ============== HELPER METHODS (moved from UserComicHistoryHelper) ==============

    /**
     * Update reading progress
     */
    private fun updateReadingProgressHelper(
        history: UserComicHistory, 
        currentPage: Int, 
        totalPages: Int, 
        chapterId: Int
    ): UserComicHistory {
        return history.copy(
            currentPage = currentPage,
            lastViewedPage = currentPage,
            latestViewedChapter = chapterId,
            lastReadingDate = Date(),
            updatedAt = Date()
        )
    }

    /**
     * Toggle favorite status
     */
    private fun toggleFavoriteHelper(history: UserComicHistory): UserComicHistory {
        return history.copy(
            isFavorite = !history.isFavorite,
            updatedAt = Date()
        )
    }

    /**
     * Add bookmark for a chapter
     */
    private fun addBookmarkHelper(history: UserComicHistory, chapterId: Int): UserComicHistory {
        val updatedBookmarks = history.bookmarkedChapters.toMutableList()
        if (!updatedBookmarks.contains(chapterId)) {
            updatedBookmarks.add(chapterId)
        }
        
        return history.copy(
            bookmarkedChapters = updatedBookmarks,
            updatedAt = Date()
        )
    }

    /**
     * Remove bookmark for a chapter
     */
    private fun removeBookmarkHelper(history: UserComicHistory, chapterId: Int): UserComicHistory {
        val updatedBookmarks = history.bookmarkedChapters.toMutableList()
        updatedBookmarks.remove(chapterId)
        
        return history.copy(
            bookmarkedChapters = updatedBookmarks,
            updatedAt = Date()
        )
    }

    /**
     * Add liked chapter
     */
    private fun addLikedChapterHelper(history: UserComicHistory, chapterId: Int): UserComicHistory {
        val updatedLikedChapters = history.likedChapters.toMutableList()
        if (!updatedLikedChapters.contains(chapterId)) {
            updatedLikedChapters.add(chapterId)
        }
        
        return history.copy(
            likedChapters = updatedLikedChapters,
            updatedAt = Date()
        )
    }

    /**
     * Remove liked chapter
     */
    private fun removeLikedChapterHelper(history: UserComicHistory, chapterId: Int): UserComicHistory {
        val updatedLikedChapters = history.likedChapters.toMutableList()
        updatedLikedChapters.remove(chapterId)
        
        return history.copy(
            likedChapters = updatedLikedChapters,
            updatedAt = Date()
        )
    }

    /**
     * Add purchased chapter
     */
    private fun addPurchasedChapterHelper(history: UserComicHistory, chapterId: Int): UserComicHistory {
        val updatedPurchased = history.purchasedChapters.toMutableList()
        if (!updatedPurchased.contains(chapterId)) {
            updatedPurchased.add(chapterId)
        }

        return history.copy(
            purchasedChapters = updatedPurchased,
            updatedAt = Date()
        )
    }

    /**
     * Update reading streak
     */
    private fun updateReadingStreakHelper(history: UserComicHistory): UserComicHistory {
        val today = Calendar.getInstance()
        val lastRead = Calendar.getInstance()
        
        history.lastReadingDate?.let { lastRead.time = it }
        
        val daysDifference = ((today.timeInMillis - lastRead.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        
        val newStreak = when {
            daysDifference == 0 -> history.readingStreak // Same day
            daysDifference == 1 -> history.readingStreak + 1 // Next day
            else -> 1 // Reset streak
        }
        
        return history.copy(
            readingStreak = newStreak,
            updatedAt = Date()
        )
    }

    /**
     * Mark comic as completed
     */
    private fun markAsCompletedHelper(history: UserComicHistory, totalChaptersInComic: Int): UserComicHistory {
        return history.copy(
            isCompleted = true,
            readingProgress = 1.0f,
            totalChaptersRead = totalChaptersInComic,
            updatedAt = Date()
        )
    }

    /**
     * Calculate overall progress in the comic
     */
    private fun calculateOverallProgressHelper(history: UserComicHistory, totalChaptersInComic: Int): Float {
        return if (totalChaptersInComic > 0) {
            history.viewedChapters.size.toFloat() / totalChaptersInComic
        } else {
            0.0f
        }
    }

    /**
     * Get reading summary
     */
    private fun getReadingSummaryHelper(history: UserComicHistory): ReadingSummary {
        return ReadingSummary(
            totalChaptersRead = history.totalChaptersRead,
            totalReadingTime = history.totalReadingTime,
            readingStreak = history.readingStreak,
            favoriteChapters = history.likedChapters.size,
            bookmarkedChapters = history.bookmarkedChapters.size,
            overallProgress = history.readingProgress,
            isCompleted = history.isCompleted,
            lastReadingDate = history.lastReadingDate
        )
    }

    /**
     * Check for achievements
     */
    private fun checkAchievementsHelper(history: UserComicHistory): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // Reading streak achievements
        when (history.readingStreak) {
            7 -> achievements.add(Achievement("Week Warrior", "Read for 7 consecutive days!"))
            30 -> achievements.add(Achievement("Monthly Master", "Read for 30 consecutive days!"))
            100 -> achievements.add(Achievement("Century Reader", "Read for 100 consecutive days!"))
        }
        
        // Chapter achievements
        when (history.totalChaptersRead) {
            10 -> achievements.add(Achievement("Chapter Champion", "Read 10 chapters!"))
            50 -> achievements.add(Achievement("Reading Enthusiast", "Read 50 chapters!"))
            100 -> achievements.add(Achievement("Page Turner", "Read 100 chapters!"))
        }
        
        // Completion achievement
        if (history.isCompleted) {
            achievements.add(Achievement("Comic Completed", "Finished reading the entire comic!"))
        }
        
        return achievements
    }

    // ============== PUBLIC METHODS ==============
    
    /**
     * Get or create user comic history
     */
    fun getOrCreateUserComicHistory(userId: Int, comicId: Int): UserComicHistory {
        var history = dbHelper.getUserComicHistory(userId, comicId)
        
        if (history == null) {
            // Create new history record
            history = UserComicHistory(
                userId = userId,
                comicId = comicId,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val id = dbHelper.insertOrUpdateUserComicHistory(history)
            Log.d(TAG, "Created new user comic history with ID: $id")
        }
        
        return history
    }
    
    /**
     * Record that user has viewed a chapter
     */
    fun recordChapterViewed(userId: Int, comicId: Int, chapterId: Int, pageCount: Int = 0): Boolean {
        return try {
            var history = getOrCreateUserComicHistory(userId, comicId)
            val updatedViewedChapters = history.viewedChapters.toMutableList()
            if (!updatedViewedChapters.contains(chapterId)) {
                updatedViewedChapters.add(chapterId)
            }

            history = history.copy(
                viewedChapters = updatedViewedChapters,
                latestViewedChapter = chapterId,
                totalChaptersRead = updatedViewedChapters.size,
                lastReadingDate = Date(),
                updatedAt = Date()
            )
            
            val streakUpdatedHistory = updateReadingStreakHelper(history)
            
            dbHelper.insertOrUpdateUserComicHistory(streakUpdatedHistory)
            Log.d(TAG, "Recorded chapter $chapterId viewed for user $userId, comic $comicId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error recording chapter viewed", e)
            false
        }
    }
    
    /**
     * Update reading progress
     */
//    fun updateReadingProgress(
//        userId: Int,
//        comicId: Int,
//        chapterId: Int,
//        currentPage: Int,
//        totalPages: Int
//    ): Boolean {
//        return try {
//            val history = getOrCreateUserComicHistory(userId, comicId)
//            val updatedHistory = updateReadingProgressHelper(history, currentPage, totalPages, chapterId)
//
//            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
//            Log.d(TAG, "Updated reading progress for user $userId, comic $comicId, page $currentPage/$totalPages")
//            true
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating reading progress", e)
//            false
//        }
//    }

    /**
     * Toggle favorite status for a comic
     */
    fun toggleFavorite(userId: Int, comicId: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = toggleFavoriteHelper(history)
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Toggled favorite for user $userId, comic $comicId: ${updatedHistory.isFavorite}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling favorite", e)
            false
        }
    }
    
    /**
     * Add bookmark for a chapter
     */
    fun addBookmark(userId: Int, comicId: Int, chapterId: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = addBookmarkHelper(history, chapterId)
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Added bookmark for user $userId, comic $comicId, chapter $chapterId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding bookmark", e)
            false
        }
    }
    
    /**
     * Remove bookmark for a chapter
     */
    fun removeBookmark(userId: Int, comicId: Int, chapterId: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = removeBookmarkHelper(history, chapterId)
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Removed bookmark for user $userId, comic $comicId, chapter $chapterId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing bookmark", e)
            false
        }
    }
    
    /**
     * Toggle like status for a chapter
     */
    fun toggleChapterLike(userId: Int, comicId: Int, chapterId: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            
            // Check if chapter is currently liked
            val isCurrentlyLiked = history.likedChapters.contains(chapterId)
            
            val updatedHistory = if (isCurrentlyLiked) {
                // Currently liked, so unlike it
                removeLikedChapterHelper(history, chapterId)
            } else {
                // Not liked, so like it
                addLikedChapterHelper(history, chapterId)
            }
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Toggled like for user $userId, comic $comicId, chapter $chapterId. Now liked: ${!isCurrentlyLiked}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling chapter like", e)
            false
        }
    }

    /**
     * Record that a chapter was purchased/unlocked by the user
     */
    fun recordChapterPurchase(userId: Int, comicId: Int, chapterId: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = addPurchasedChapterHelper(history, chapterId)

            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Recorded purchase for user $userId, comic $comicId, chapter $chapterId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error recording chapter purchase", e)
            false
        }
    }

    /**
     * Check if a chapter has been purchased by the user
     */
    fun isChapterPurchased(userId: Int, comicId: Int, chapterId: Int): Boolean {
        val history = dbHelper.getUserComicHistory(userId, comicId)
        return history?.purchasedChapters?.contains(chapterId) ?: false
    }
    
    /**
     * Check if a chapter is liked by the user
     */
    fun isChapterLiked(userId: Int, comicId: Int, chapterId: Int): Boolean {
        val history = dbHelper.getUserComicHistory(userId, comicId)
        return history?.likedChapters?.contains(chapterId) ?: false
    }
    
    /**
     * Get user's reading history for all comics
     */
    fun getUserReadingHistory(userId: Int): List<UserComicHistory> {
        return dbHelper.getAllUserComicHistories(userId)
    }
    
    /**
     * Get user's favorite comics
     */
    fun getUserFavoriteComics(userId: Int): List<UserComicHistory> {
        return dbHelper.getFavoriteComics(userId)
    }
    
    /**
     * Get recently read comics
     */
    fun getRecentlyReadComics(userId: Int, limit: Int = 10): List<UserComicHistory> {
        return dbHelper.getRecentlyReadComics(userId, limit)
    }
    
    /**
     * Get reading summary for a specific comic
     */
    fun getReadingSummary(userId: Int, comicId: Int): ReadingSummary? {
        val history = dbHelper.getUserComicHistory(userId, comicId)
        return history?.let { getReadingSummaryHelper(it) }
    }
    
    /**
     * Check if user has any new achievements
     */
    fun checkUserAchievements(userId: Int, comicId: Int): List<Achievement> {
        val history = dbHelper.getUserComicHistory(userId, comicId)
        return history?.let { checkAchievementsHelper(it) } ?: emptyList()
    }

    /**
     * Update reading preferences
     */
    fun updateReadingPreferences(
        userId: Int, 
        comicId: Int, 
        readingMode: ReadingMode,
        notificationsEnabled: Boolean
    ): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = history.copy(
                preferredReadingMode = readingMode,
                notificationsEnabled = notificationsEnabled,
                updatedAt = Date()
            )
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Updated reading preferences for user $userId, comic $comicId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reading preferences", e)
            false
        }
    }
    
    /**
     * Mark comic as completed
     */
    fun markComicAsCompleted(userId: Int, comicId: Int, totalChaptersInComic: Int): Boolean {
        return try {
            val history = getOrCreateUserComicHistory(userId, comicId)
            val updatedHistory = markAsCompletedHelper(history, totalChaptersInComic)
            
            dbHelper.insertOrUpdateUserComicHistory(updatedHistory)
            Log.d(TAG, "Marked comic as completed for user $userId, comic $comicId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking comic as completed", e)
            false
        }
    }
    
    /**
     * Get overall reading progress for a comic
     */
    fun getOverallProgress(userId: Int, comicId: Int, totalChaptersInComic: Int): Float {
        val history = dbHelper.getUserComicHistory(userId, comicId)
        return history?.let { calculateOverallProgressHelper(it, totalChaptersInComic) } ?: 0.0f
    }
    
    /**
     * Delete user comic history
     */
    fun deleteUserComicHistory(userId: Int, comicId: Int): Boolean {
        return try {
            val result = dbHelper.deleteUserComicHistory(userId, comicId)
            Log.d(TAG, "Deleted user comic history for user $userId, comic $comicId")
            result > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user comic history", e)
            false
        }
    }
}

/**
 * Data class for reading summary
 */
data class ReadingSummary(
    val totalChaptersRead: Int,
    val totalReadingTime: Long,
    val readingStreak: Int,
    val favoriteChapters: Int,
    val bookmarkedChapters: Int,
    val overallProgress: Float,
    val isCompleted: Boolean,
    val lastReadingDate: Date?
)

/**
 * Data class for achievements
 */
data class Achievement(
    val title: String,
    val description: String,
    val dateEarned: Date = Date()
)