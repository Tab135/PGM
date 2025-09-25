package com.example.pgm.model.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class BaseDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ComicLibrary.db"
        const val DATABASE_VERSION = 1

        // Users table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NAME = "name"

        // Comics table
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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMICS")
        onCreate(db)
    }
}