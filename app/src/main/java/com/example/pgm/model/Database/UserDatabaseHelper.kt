package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.pgm.model.User

class UserDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    fun addUser(user: User): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
            put(COLUMN_ROLE, user.role)
            put(COLUMN_TOKENS, user.tokens)
        }
        val result = db.insert(TABLE_USERS, null, contentValues)
        return result != -1L
    }

    fun updateUser(user: User): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, user.name)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_ADDRESS, user.address)
            put(COLUMN_PROFILE_IMAGE, user.profileImage)
            // Don't update role in regular profile updates
        }
        val result = db.update(
            TABLE_USERS,
            contentValues,
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString())
        )
        return result > 0
    }

    fun updateUserPassword(userId: Int, newPassword: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val result = db.update(
            TABLE_USERS,
            contentValues,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
        return result > 0
    }

    /**
     * Get user's current token balance
     */
    fun getUserTokens(userId: Int): Int {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_TOKENS FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val tokens = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOKENS))
            cursor.close()
            tokens
        } else {
            cursor.close()
            0
        }
    }

    /**
     * Add tokens to user's balance (e.g., when purchasing tokens or rewards)
     */
    fun addTokens(userId: Int, amount: Int): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            val currentTokens = getUserTokens(userId)
            val newBalance = currentTokens + amount

            val contentValues = ContentValues().apply {
                put(COLUMN_TOKENS, newBalance)
            }
            val result = db.update(
                TABLE_USERS,
                contentValues,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString())
            )

            db.setTransactionSuccessful()
            result > 0
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Deduct tokens from user's balance (e.g., when unlocking a chapter)
     * Returns true if successful, false if insufficient tokens
     */
    fun deductTokens(userId: Int, amount: Int): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            val currentTokens = getUserTokens(userId)

            // Check if user has enough tokens
            if (currentTokens < amount) {
                db.endTransaction()
                return false
            }

            val newBalance = currentTokens - amount

            val contentValues = ContentValues().apply {
                put(COLUMN_TOKENS, newBalance)
            }
            val result = db.update(
                TABLE_USERS,
                contentValues,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString())
            )

            db.setTransactionSuccessful()
            result > 0
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Check if user has enough tokens to unlock a chapter
     */
    fun canAffordChapter(userId: Int, cost: Int): Boolean {
        return getUserTokens(userId) >= cost
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun getUserById(id: Int): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: "",
            address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)) ?: "",
            profileImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)),
            role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)) ?: "user",
            tokens = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOKENS))
        )
    }

    fun checkEmail(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exist = cursor.count > 0
        cursor.close()
        return exist
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exist = cursor.count > 0
        cursor.close()
        return exist
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor))
        }
        cursor.close()
        return users
    }
}