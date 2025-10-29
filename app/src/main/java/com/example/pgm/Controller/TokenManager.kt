package com.example.pgm.model

import android.content.Context
import android.util.Log
import com.example.pgm.model.Database.DatabaseManager

/**
 * Manager class to handle token-based chapter access
 */
class TokenManager(private val context: Context) {

    private val userDbHelper = DatabaseManager.getUserHelper(context)
    private val historyDbHelper = DatabaseManager.getUserComicHistoryHelper(context)

    companion object {
        private const val TAG = "TokenManager"
    }

    /**
     * Check if user can access a chapter
     * Returns AccessResult with status and message
     */
    fun canAccessChapter(userId: Int, chapter: Chapter, history: UserComicHistory?): AccessResult {
        // Check if chapter is currently locked
        if (!chapter.isCurrentlyLocked()) {
            return AccessResult(true, "Chapter is free")
        }

        // Check if user has already purchased this chapter
        if (history?.purchasedChapters?.contains(chapter.id) == true) {
            return AccessResult(true, "Already purchased")
        }

        // Check if user has enough tokens
        val userTokens = userDbHelper.getUserTokens(userId)
        val chapterCost = chapter.cost

        return if (userTokens >= chapterCost) {
            AccessResult(true, "Sufficient tokens ($userTokens >= $chapterCost)")
        } else {
            AccessResult(
                false,
                "Insufficient tokens. Need $chapterCost tokens, but you only have $userTokens tokens"
            )
        }
    }

    /**
     * Attempt to unlock and access a locked chapter by spending tokens
     * Returns UnlockResult with status, message, and updated token balance
     */
    fun unlockChapter(userId: Int, chapter: Chapter, history: UserComicHistory?): UnlockResult {
        // Check if chapter is free
        if (!chapter.isCurrentlyLocked()) {
            return UnlockResult(
                success = true,
                message = "Chapter is already free",
                tokensSpent = 0,
                remainingTokens = userDbHelper.getUserTokens(userId),
                alreadyPurchased = false
            )
        }

        // Check if already purchased
        if (history?.purchasedChapters?.contains(chapter.id) == true) {
            return UnlockResult(
                success = true,
                message = "Chapter already unlocked",
                tokensSpent = 0,
                remainingTokens = userDbHelper.getUserTokens(userId),
                alreadyPurchased = true
            )
        }

        val chapterCost = chapter.cost
        val userTokens = userDbHelper.getUserTokens(userId)

        // Check if user has enough tokens
        if (userTokens < chapterCost) {
            return UnlockResult(
                success = false,
                message = "Insufficient tokens. Need $chapterCost, have $userTokens",
                tokensSpent = 0,
                remainingTokens = userTokens,
                alreadyPurchased = false
            )
        }

        // Deduct tokens
        val deductSuccess = userDbHelper.deductTokens(userId, chapterCost)

        if (!deductSuccess) {
            Log.e(TAG, "Failed to deduct tokens for user $userId")
            return UnlockResult(
                success = false,
                message = "Failed to process token payment",
                tokensSpent = 0,
                remainingTokens = userTokens,
                alreadyPurchased = false
            )
        }

        // Update user comic history to mark chapter as purchased
        val updatedHistory = if (history != null) {
            history.copy(
                purchasedChapters = history.purchasedChapters + chapter.id,
                updatedAt = java.util.Date()
            )
        } else {
            UserComicHistory(
                userId = userId,
                comicId = chapter.comicId,
                purchasedChapters = listOf(chapter.id),
                createdAt = java.util.Date(),
                updatedAt = java.util.Date()
            )
        }

        historyDbHelper.insertOrUpdateUserComicHistory(updatedHistory)

        val newBalance = userDbHelper.getUserTokens(userId)

        Log.d(TAG, "Chapter ${chapter.id} unlocked for user $userId. Spent $chapterCost tokens. New balance: $newBalance")

        return UnlockResult(
            success = true,
            message = "Chapter unlocked successfully!",
            tokensSpent = chapterCost,
            remainingTokens = newBalance,
            alreadyPurchased = false
        )
    }

    /**
     * Get user's current token balance
     */
    fun getUserTokenBalance(userId: Int): Int {
        return userDbHelper.getUserTokens(userId)
    }

    fun debugTokenIssue(userId: Int) {
        Log.d(TAG, "=== DEBUG TOKEN ISSUE ===")
        Log.d(TAG, "Requested userId: $userId")

        // Check if user exists
        val user = userDbHelper.getUserById(userId)
        if (user == null) {
            Log.e(TAG, "User with ID $userId not found!")
            return
        }

        Log.d(TAG, "User found: ${user.name} (ID: ${user.id})")

        // Get token balance
        val tokens = userDbHelper.getUserTokens(userId)
        Log.d(TAG, "Token balance for user $userId: $tokens")

        // Also check admin user (usually ID 1)
        val adminTokens = userDbHelper.getUserTokens(1)
        Log.d(TAG, "Admin token balance: $adminTokens")

        Log.d(TAG, "=== END DEBUG ===")
    }
    /**
     * Check if chapter is free or purchased
     */
    fun isChapterAccessible(chapter: Chapter, history: UserComicHistory?): Boolean {
        return !chapter.isCurrentlyLocked() ||
                (history?.purchasedChapters?.contains(chapter.id) == true)
    }

    /**
     * Get the cost to unlock a chapter (0 if free or already purchased)
     */
    fun getChapterCost(chapter: Chapter, history: UserComicHistory?): Int {
        return if (isChapterAccessible(chapter, history)) {
            0
        } else {
            chapter.cost
        }
    }
}

/**
 * Result of checking chapter access
 */
data class AccessResult(
    val canAccess: Boolean,
    val message: String
)

/**
 * Result of unlocking a chapter
 */
data class UnlockResult(
    val success: Boolean,
    val message: String,
    val tokensSpent: Int,
    val remainingTokens: Int,
    val alreadyPurchased: Boolean
)