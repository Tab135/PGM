package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.pgm.model.*
import java.text.SimpleDateFormat
import java.util.*

class UserComicHistoryDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    companion object {
        const val TAG = "UserComicHistoryDB"
        
        // User Comic History table
        const val TABLE_USER_COMIC_HISTORY = "user_comic_history"
        const val COLUMN_HISTORY_ID = "_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_COMIC_ID = "comic_id"
        const val COLUMN_VIEWED_CHAPTERS = "viewed_chapters"
        const val COLUMN_LATEST_VIEWED_CHAPTER = "latest_viewed_chapter"
        const val COLUMN_CURRENT_PAGE = "current_page"
        const val COLUMN_TOTAL_READING_TIME = "total_reading_time"
        const val COLUMN_LAST_READING_DATE = "last_reading_date"
        const val COLUMN_BOOKMARKED_CHAPTERS = "bookmarked_chapters"
        const val COLUMN_LIKED_CHAPTERS = "liked_chapters"
        const val COLUMN_READING_PROGRESS = "reading_progress"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val COLUMN_LAST_VIEWED_PAGE = "last_viewed_page"
        const val COLUMN_READING_STREAK = "reading_streak"
        const val COLUMN_TOTAL_CHAPTERS_READ = "total_chapters_read"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    // Helper methods for list serialization
    private fun listToString(list: List<Int>): String {
        return list.joinToString(",")
    }
    
    private fun stringToList(str: String?): List<Int> {
        return if (str.isNullOrEmpty()) {
            emptyList()
        } else {
            str.split(",").mapNotNull { it.toIntOrNull() }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Table creation is now handled by BaseDatabaseHelper
        super.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Table upgrade is now handled by BaseDatabaseHelper
        super.onUpgrade(db, oldVersion, newVersion)
    }

    // UserComicHistory CRUD operations

    fun insertOrUpdateUserComicHistory(history: UserComicHistory): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, history.userId)
            put(COLUMN_COMIC_ID, history.comicId)
            put(COLUMN_VIEWED_CHAPTERS, listToString(history.viewedChapters))
            put(COLUMN_LATEST_VIEWED_CHAPTER, history.latestViewedChapter)
            put(COLUMN_CURRENT_PAGE, history.currentPage)
            put(COLUMN_TOTAL_READING_TIME, history.totalReadingTime)
            put(COLUMN_LAST_READING_DATE, history.lastReadingDate?.let { dateFormat.format(it) })
            put(COLUMN_BOOKMARKED_CHAPTERS, listToString(history.bookmarkedChapters))
            put(COLUMN_LIKED_CHAPTERS, listToString(history.likedChapters))
            put(COLUMN_READING_PROGRESS, history.readingProgress)
            put(COLUMN_IS_COMPLETED, if (history.isCompleted) 1 else 0)
            put(COLUMN_IS_FAVORITE, if (history.isFavorite) 1 else 0)
            put(COLUMN_NOTIFICATIONS_ENABLED, if (history.notificationsEnabled) 1 else 0)
            put(COLUMN_LAST_VIEWED_PAGE, history.lastViewedPage)
            put(COLUMN_READING_STREAK, history.readingStreak)
            put(COLUMN_TOTAL_CHAPTERS_READ, history.totalChaptersRead)
            put(COLUMN_CREATED_AT, dateFormat.format(history.createdAt))
            put(COLUMN_UPDATED_AT, dateFormat.format(history.updatedAt))
        }

        return try {
            // Try to update first
            val rowsUpdated = db.update(
                TABLE_USER_COMIC_HISTORY,
                values,
                "$COLUMN_USER_ID = ? AND $COLUMN_COMIC_ID = ?",
                arrayOf(history.userId.toString(), history.comicId.toString())
            )
            
            if (rowsUpdated > 0) {
                Log.d(TAG, "Updated user comic history for user ${history.userId}, comic ${history.comicId}")
                rowsUpdated.toLong()
            } else {
                // Insert if update didn't affect any rows
                val id = db.insert(TABLE_USER_COMIC_HISTORY, null, values)
                Log.d(TAG, "Inserted user comic history with ID: $id")
                id
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting/updating user comic history", e)
            -1
        }
    }

    fun getUserComicHistory(userId: Int, comicId: Int): UserComicHistory? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER_COMIC_HISTORY,
            null,
            "$COLUMN_USER_ID = ? AND $COLUMN_COMIC_ID = ?",
            arrayOf(userId.toString(), comicId.toString()),
            null, null, null
        )

        return cursor.use { c ->
            if (c.moveToFirst()) {
                cursorToUserComicHistory(c)
            } else null
        }
    }

    fun getAllUserComicHistories(userId: Int): List<UserComicHistory> {
        val db = readableDatabase
        val histories = mutableListOf<UserComicHistory>()
        
        val cursor = db.query(
            TABLE_USER_COMIC_HISTORY,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_UPDATED_AT DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                cursorToUserComicHistory(c)?.let { histories.add(it) }
            }
        }
        
        return histories
    }

    fun getFavoriteComics(userId: Int): List<UserComicHistory> {
        val db = readableDatabase
        val favorites = mutableListOf<UserComicHistory>()
        
        val cursor = db.query(
            TABLE_USER_COMIC_HISTORY,
            null,
            "$COLUMN_USER_ID = ? AND $COLUMN_IS_FAVORITE = 1",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_UPDATED_AT DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                cursorToUserComicHistory(c)?.let { favorites.add(it) }
            }
        }
        
        return favorites
    }

    fun getRecentlyReadComics(userId: Int, limit: Int = 10): List<UserComicHistory> {
        val db = readableDatabase
        val recent = mutableListOf<UserComicHistory>()
        
        val cursor = db.query(
            TABLE_USER_COMIC_HISTORY,
            null,
            "$COLUMN_USER_ID = ? AND $COLUMN_LAST_READING_DATE IS NOT NULL",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_LAST_READING_DATE DESC",
            limit.toString()
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                cursorToUserComicHistory(c)?.let { recent.add(it) }
            }
        }
        
        return recent
    }

    private fun cursorToUserComicHistory(cursor: Cursor): UserComicHistory? {
        return try {
            val viewedChaptersJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIEWED_CHAPTERS))
            val bookmarkedChaptersJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKMARKED_CHAPTERS))
            val likedChaptersJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIKED_CHAPTERS))
            
            val viewedChapters: List<Int> = stringToList(viewedChaptersJson)
            val bookmarkedChapters: List<Int> = stringToList(bookmarkedChaptersJson)
            val likedChapters: List<Int> = stringToList(likedChaptersJson)
            
            val lastReadingDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_READING_DATE))
            val lastReadingDate = lastReadingDateStr?.let { dateFormat.parse(it) }
            
            val createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
            val updatedAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
            
            UserComicHistory(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                comicId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMIC_ID)),
                viewedChapters = viewedChapters,
                latestViewedChapter = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LATEST_VIEWED_CHAPTER)).takeIf { it != 0 },
                currentPage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PAGE)),
                totalReadingTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_READING_TIME)),
                lastReadingDate = lastReadingDate,
                bookmarkedChapters = bookmarkedChapters,
                likedChapters = likedChapters,
                readingProgress = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_READING_PROGRESS)),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1,
                notificationsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATIONS_ENABLED)) == 1,
                lastViewedPage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LAST_VIEWED_PAGE)),
                readingStreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READING_STREAK)),
                totalChaptersRead = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_CHAPTERS_READ)),
                createdAt = dateFormat.parse(createdAtStr) ?: Date(),
                updatedAt = dateFormat.parse(updatedAtStr) ?: Date()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting cursor to UserComicHistory", e)
            null
        }
    }

    fun deleteUserComicHistory(userId: Int, comicId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_USER_COMIC_HISTORY,
            "$COLUMN_USER_ID = ? AND $COLUMN_COMIC_ID = ?",
            arrayOf(userId.toString(), comicId.toString())
        )
    }
}