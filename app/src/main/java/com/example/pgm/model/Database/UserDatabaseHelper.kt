package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.pgm.model.User

class UserDatabaseHelper(context: Context) : BaseDatabaseHelper(context) {

    fun addUser(user: User): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
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
            put(COLUMN_PROFILE_IMAGE, user. profileImage)
            // For password updates, create a separate method
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
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
            address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
            profileImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE))
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