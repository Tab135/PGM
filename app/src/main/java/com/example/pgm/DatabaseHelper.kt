package com.example.pgm

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object {
        const val DATABASE_NAME = "ComicLibrary.db"
        const val DATABASE_VERSION = 1

        //Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"

        //Comics table
        const val TABLE_COMICS = "comic"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "book_title"
        const val COLUMN_AUTHOR = "book_author"
        const val COLUMN_PAGES = "book_pages"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """ CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL, 
                $COLUMN_NAME TEXT NOT NULL 
                )
                """.trimIndent()
        db?.execSQL(createUsersTable)

        // Create comics table
        val createComicsTable = """
            CREATE TABLE $TABLE_COMICS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AUTHOR TEXT NOT NULL,
                $COLUMN_PAGES INTEGER NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createComicsTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        db?.execSQL(" DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL(" DROP TABLE IF EXISTS $TABLE_COMICS")
        onCreate(db)

    }

    fun addUser(email: String, password: String, name: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NAME, name)
        }
        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result != -1L
    }
    fun checkUser(email: String, password: String): Boolean{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exist = cursor.count > 0
        cursor.close()
        db.close()
        return exist
    }
    fun CheckEmail(email: String) : Boolean {
        val db = this.readableDatabase
        val query = " Select * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exist = cursor.count > 0
        cursor.close()
        db.close()
        return exist;
    }
}
