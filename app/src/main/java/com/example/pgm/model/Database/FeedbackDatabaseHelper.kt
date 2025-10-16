package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import com.example.pgm.model.Feedback
import java.text.SimpleDateFormat
import java.util.*

class FeedbackDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    companion object {
        const val TABLE_FEEDBACK = "feedback"
        const val COLUMN_FEEDBACK_ID = "_id"
        const val COLUMN_FEEDBACK_USER_ID = "user_id"
        const val COLUMN_FEEDBACK_COMIC_ID = "comic_id"
        const val COLUMN_RATING = "rating"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_CATEGORIES = "categories"
        const val COLUMN_IS_ANONYMOUS = "is_anonymous"
        const val COLUMN_FEEDBACK_CREATED_AT = "created_at"
        const val COLUMN_FEEDBACK_UPDATED_AT = "updated_at"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_IS_EDITED = "is_edited"
    }

    override fun onCreate(db: android.database.sqlite.SQLiteDatabase?) {
        super.onCreate(db)
        createFeedbackTable(db)
    }

    private fun createFeedbackTable(db: android.database.sqlite.SQLiteDatabase?) {
        val createFeedbackTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_FEEDBACK (
                $COLUMN_FEEDBACK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FEEDBACK_USER_ID INTEGER NOT NULL,
                $COLUMN_FEEDBACK_COMIC_ID INTEGER NOT NULL,
                $COLUMN_RATING REAL NOT NULL,
                $COLUMN_COMMENT TEXT NOT NULL,
                $COLUMN_CATEGORIES TEXT DEFAULT '',
                $COLUMN_IS_ANONYMOUS INTEGER DEFAULT 0,
                $COLUMN_FEEDBACK_CREATED_AT TEXT NOT NULL,
                $COLUMN_FEEDBACK_UPDATED_AT TEXT,
                $COLUMN_LIKES INTEGER DEFAULT 0,
                $COLUMN_IS_EDITED INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_FEEDBACK_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                FOREIGN KEY($COLUMN_FEEDBACK_COMIC_ID) REFERENCES $TABLE_COMICS($COLUMN_ID),
                UNIQUE($COLUMN_FEEDBACK_USER_ID, $COLUMN_FEEDBACK_COMIC_ID)
            )
        """.trimIndent()
        db?.execSQL(createFeedbackTable)
    }

    fun addFeedback(feedback: Feedback): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_FEEDBACK_USER_ID, feedback.userId)
            put(COLUMN_FEEDBACK_COMIC_ID, feedback.comicId)
            put(COLUMN_RATING, feedback.rating)
            put(COLUMN_COMMENT, feedback.comment)
            put(COLUMN_CATEGORIES, feedback.categories.joinToString(","))
            put(COLUMN_IS_ANONYMOUS, if (feedback.isAnonymous) 1 else 0)
            put(COLUMN_FEEDBACK_CREATED_AT, feedback.createdAt)
            put(COLUMN_FEEDBACK_UPDATED_AT, feedback.updatedAt)
            put(COLUMN_LIKES, feedback.likes)
            put(COLUMN_IS_EDITED, if (feedback.isEdited) 1 else 0)
        }
        val result = db.insert(TABLE_FEEDBACK, null, cv)
        db.close()
        return result != -1L
    }

    fun updateFeedback(feedback: Feedback): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_RATING, feedback.rating)
            put(COLUMN_COMMENT, feedback.comment)
            put(COLUMN_CATEGORIES, feedback.categories.joinToString(","))
            put(COLUMN_IS_ANONYMOUS, if (feedback.isAnonymous) 1 else 0)
            put(COLUMN_FEEDBACK_UPDATED_AT, getCurrentDateTime())
            put(COLUMN_IS_EDITED, 1)
        }
        val result = db.update(
            TABLE_FEEDBACK,
            cv,
            "$COLUMN_FEEDBACK_ID = ?",
            arrayOf(feedback.id.toString())
        )
        db.close()
        return result > 0
    }

    fun getUserFeedbackForComic(userId: Int, comicId: Int): Feedback? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_FEEDBACK WHERE $COLUMN_FEEDBACK_USER_ID = ? AND $COLUMN_FEEDBACK_COMIC_ID = ?",
            arrayOf(userId.toString(), comicId.toString())
        )

        var feedback: Feedback? = null
        if (cursor.moveToFirst()) {
            feedback = Feedback(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_USER_ID)),
                comicId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_COMIC_ID)),
                rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
                comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                categories = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIES))
                    .split(",").filter { it.isNotEmpty() },
                isAnonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ANONYMOUS)) == 1,
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_CREATED_AT)),
                updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_UPDATED_AT)),
                likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                isEdited = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EDITED)) == 1
            )
        }
        cursor.close()
        db.close()
        return feedback
    }

    fun getAllFeedbackForComic(comicId: Int): List<Feedback> {
        val feedbackList = mutableListOf<Feedback>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_FEEDBACK WHERE $COLUMN_FEEDBACK_COMIC_ID = ? ORDER BY $COLUMN_FEEDBACK_CREATED_AT DESC",
            arrayOf(comicId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                feedbackList.add(
                    Feedback(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_USER_ID)),
                        comicId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_COMIC_ID)),
                        rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
                        comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                        categories = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIES))
                            .split(",").filter { it.isNotEmpty() },
                        isAnonymous = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ANONYMOUS)) == 1,
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_CREATED_AT)),
                        updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDBACK_UPDATED_AT)),
                        likes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIKES)),
                        isEdited = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EDITED)) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return feedbackList
    }

    fun getAverageRating(comicId: Int): Float {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT AVG($COLUMN_RATING) as avg_rating FROM $TABLE_FEEDBACK WHERE $COLUMN_FEEDBACK_COMIC_ID = ?",
            arrayOf(comicId.toString())
        )

        var avgRating = 0f
        if (cursor.moveToFirst()) {
            avgRating = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating"))
        }
        cursor.close()
        db.close()
        return avgRating
    }

    fun getFeedbackCount(comicId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count FROM $TABLE_FEEDBACK WHERE $COLUMN_FEEDBACK_COMIC_ID = ?",
            arrayOf(comicId.toString())
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
        }
        cursor.close()
        db.close()
        return count
    }

    fun deleteFeedback(feedbackId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            TABLE_FEEDBACK,
            "$COLUMN_FEEDBACK_ID = ?",
            arrayOf(feedbackId.toString())
        )
        db.close()
        return result > 0
    }

    fun incrementLikes(feedbackId: Int): Boolean {
        val db = writableDatabase
        db.execSQL(
            "UPDATE $TABLE_FEEDBACK SET $COLUMN_LIKES = $COLUMN_LIKES + 1 WHERE $COLUMN_FEEDBACK_ID = ?",
            arrayOf(feedbackId.toString())
        )
        db.close()
        return true
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}