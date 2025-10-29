package com.example.pgm.Controller

import android.content.Context
import android.util.Log
import com.example.pgm.model.*
import com.example.pgm.model.Database.DatabaseManager
import java.util.*

/**
 * Controller to manage user comic history operations
 */
class UserComicHistoryController(private val context: Context) {
    
    private val dbHelper = DatabaseManager.getUserComicHistoryHelper(context)

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
            totalReadingTime = history.totalReadingTime,
            bookmarkedChapters = history.bookmarkedChapters.size,
            overallProgress = history.readingProgress
        )
    }

    /**
     * Check for achievements
     */
    private fun checkAchievementsHelper(history: UserComicHistory): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        // Chapter achievements based on viewed chapters count
        when (history.viewedChapters.size) {
            10 -> achievements.add(Achievement("Chapter Champion", "Read 10 chapters!"))
            50 -> achievements.add(Achievement("Reading Enthusiast", "Read 50 chapters!"))
            100 -> achievements.add(Achievement("Page Turner", "Read 100 chapters!"))
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
                updatedAt = Date()
            )
            
            dbHelper.insertOrUpdateUserComicHistory(history)
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
    
    /**
     * Get reading statistics for a user
     * Returns a map with counts for total and reading comics
     */
    fun getUserReadingStatistics(userId: Int): ReadingStatistics {
        val allHistory = dbHelper.getAllUserComicHistories(userId)
        
        // Reading: user has viewed at least one chapter
        val readingCount = allHistory.count { history ->
            history.viewedChapters.isNotEmpty()
        }
        
        // Total: all comics the user has interacted with
        val totalCount = allHistory.size
        
        return ReadingStatistics(
            total = totalCount,
            reading = readingCount
        )
    }
    
    /**
     * Get comics by reading status
     */
    fun getComicsByReadingStatus(userId: Int, status: ReadingStatus): List<Int> {
        val allHistory = dbHelper.getAllUserComicHistories(userId)
        
        return when (status) {
            ReadingStatus.READING -> {
                // Comics with at least one chapter viewed
                allHistory.filter { history ->
                    history.viewedChapters.isNotEmpty()
                }.map { it.comicId }
            }
            ReadingStatus.ALL -> {
                // All comics user has interacted with
                allHistory.map { it.comicId }
            }
        }
    }
}

/**
 * Data class for reading summary
 */
data class ReadingSummary(
    val totalReadingTime: Long,
    val bookmarkedChapters: Int,
    val overallProgress: Float
)

/**
 * Data class for achievements
 */
data class Achievement(
    val title: String,
    val description: String,
    val dateEarned: Date = Date()
)

/**
 * Data class for reading statistics
 */
data class ReadingStatistics(
    val total: Int,
    val reading: Int
)

/**
 * Enum for reading status
 */
enum class ReadingStatus {
    ALL,
    READING
}