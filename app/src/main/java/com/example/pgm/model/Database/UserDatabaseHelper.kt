package com.example.pgm.model.Database

import android.content.ContentValues
import android.content.Context
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
        db.close()
        return result != -1L
    }

    fun checkEmail(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exist = cursor.count > 0
        cursor.close()
        db.close()
        return exist
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exist = cursor.count > 0
        cursor.close()
        db.close()
        return exist
    }
}