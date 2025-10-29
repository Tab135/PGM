package com.example.pgm.model.Database

import android.content.Context

/**
 * Singleton Database Manager to prevent multiple database helper instances
 * and connection leaks.
 */
object DatabaseManager {
    @Volatile
    private var userComicHistoryHelper: UserComicHistoryDatabaseHelper? = null
    
    @Volatile
    private var comicHelper: ComicDatabaseHelper? = null
    
    @Volatile
    private var chapterHelper: ChapterDatabaseHelper? = null
    
    @Volatile
    private var userHelper: UserDatabaseHelper? = null
    
    @Volatile
    private var feedbackHelper: FeedbackDatabaseHelper? = null

    fun getUserComicHistoryHelper(context: Context): UserComicHistoryDatabaseHelper {
        return userComicHistoryHelper ?: synchronized(this) {
            userComicHistoryHelper ?: UserComicHistoryDatabaseHelper(context.applicationContext).also {
                userComicHistoryHelper = it
            }
        }
    }

    fun getComicHelper(context: Context): ComicDatabaseHelper {
        return comicHelper ?: synchronized(this) {
            comicHelper ?: ComicDatabaseHelper(context.applicationContext).also {
                comicHelper = it
            }
        }
    }

    fun getChapterHelper(context: Context): ChapterDatabaseHelper {
        return chapterHelper ?: synchronized(this) {
            chapterHelper ?: ChapterDatabaseHelper(context.applicationContext).also {
                chapterHelper = it
            }
        }
    }

    fun getUserHelper(context: Context): UserDatabaseHelper {
        return userHelper ?: synchronized(this) {
            userHelper ?: UserDatabaseHelper(context.applicationContext).also {
                userHelper = it
            }
        }
    }

    fun getFeedbackHelper(context: Context): FeedbackDatabaseHelper {
        return feedbackHelper ?: synchronized(this) {
            feedbackHelper ?: FeedbackDatabaseHelper(context.applicationContext).also {
                feedbackHelper = it
            }
        }
    }

    /**
     * Close all database connections.
     * Should only be called when the app is shutting down.
     */
    fun closeAll() {
        synchronized(this) {
            userComicHistoryHelper?.close()
            userComicHistoryHelper = null
            
            comicHelper?.close()
            comicHelper = null
            
            chapterHelper?.close()
            chapterHelper = null
            
            userHelper?.close()
            userHelper = null
            
            feedbackHelper?.close()
            feedbackHelper = null
        }
    }
}
