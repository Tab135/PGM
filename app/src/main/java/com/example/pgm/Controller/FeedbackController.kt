package com.example.pgm.Controller

import android.content.Context
import com.example.pgm.model.Feedback
import com.example.pgm.model.Database.FeedbackDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class FeedbackController(context: Context) {
    private val feedbackDatabaseHelper = FeedbackDatabaseHelper(context)
    private val userComicHistoryController = UserComicHistoryController(context)

    /**
     * Check if user is eligible to give feedback
     * User must have read at least one chapter
     */
    fun isUserEligibleToGiveFeedback(userId: Int, comicId: Int): Boolean {
        val history = userComicHistoryController.getOrCreateUserComicHistory(userId, comicId)
        return history?.viewedChapters?.isNotEmpty() ?: false
    }

    /**
     * Add new feedback
     */
    fun addFeedback(
        userId: Int,
        comicId: Int,
        rating: Float,
        comment: String,
        categories: List<String>,
        isAnonymous: Boolean = false
    ): Boolean {
        if (!isUserEligibleToGiveFeedback(userId, comicId)) {
            return false
        }

        val feedback = Feedback(
            userId = userId,
            comicId = comicId,
            rating = rating,
            comment = comment,
            categories = categories,
            isAnonymous = isAnonymous,
            createdAt = getCurrentDateTime()
        )

        return feedbackDatabaseHelper.addFeedback(feedback)
    }

    /**
     * Update existing feedback
     */
    fun updateFeedback(
        feedbackId: Int,
        userId: Int,
        comicId: Int,
        rating: Float,
        comment: String,
        categories: List<String>,
        isAnonymous: Boolean = false
    ): Boolean {
        val feedback = Feedback(
            id = feedbackId,
            userId = userId,
            comicId = comicId,
            rating = rating,
            comment = comment,
            categories = categories,
            isAnonymous = isAnonymous,
            createdAt = "", // Will not be updated
            isEdited = true
        )

        return feedbackDatabaseHelper.updateFeedback(feedback)
    }

    /**
     * Get user's feedback for a specific comic
     */
    fun getUserFeedbackForComic(userId: Int, comicId: Int): Feedback? {
        return feedbackDatabaseHelper.getUserFeedbackForComic(userId, comicId)
    }

    /**
     * Get all feedback for a comic
     */
    fun getAllFeedbackForComic(comicId: Int): List<Feedback> {
        return feedbackDatabaseHelper.getAllFeedbackForComic(comicId)
    }

    /**
     * Get average rating for a comic
     */
    fun getAverageRating(comicId: Int): Float {
        return feedbackDatabaseHelper.getAverageRating(comicId)
    }

    /**
     * Get total feedback count for a comic
     */
    fun getFeedbackCount(comicId: Int): Int {
        return feedbackDatabaseHelper.getFeedbackCount(comicId)
    }

    /**
     * Delete feedback
     */
    fun deleteFeedback(feedbackId: Int): Boolean {
        return feedbackDatabaseHelper.deleteFeedback(feedbackId)
    }

    /**
     * Like a feedback
     */
    fun likeFeedback(feedbackId: Int): Boolean {
        return feedbackDatabaseHelper.incrementLikes(feedbackId)
    }

    /**
     * Check if user has already given feedback
     */
    fun hasUserGivenFeedback(userId: Int, comicId: Int): Boolean {
        return getUserFeedbackForComic(userId, comicId) != null
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}