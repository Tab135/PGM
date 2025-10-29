package com.example.pgm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pgm.Controller.LoginController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.Comic
import com.example.pgm.model.Database.ChapterDatabaseHelper
import com.example.pgm.model.Database.ComicDatabaseHelper
import com.example.pgm.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var forgotPasswordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        Log.d("LoginActivity", "=== LoginActivity Started ===")

        // ✅ CLEAR THE SESSION SO USER HAS TO LOGIN AGAIN
        sessionManager.clearSession()
        sessionManager.debugSession()

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Initialize database with test data
        initializeTestData()

        // Setup LoginController
        LoginController(this, findViewById(R.id.loginContainer))

        // Setup Forgot Password functionality
        setupForgotPassword()

        // Temporary: Debug all users in database
        debugAllUsers()
    }

    private fun setupForgotPassword() {
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        forgotPasswordText.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun initializeTestData() {
        val db = ComicDatabaseHelper(this)
        val chapdb = ChapterDatabaseHelper(this)

        // ✅ Insert a test comic
//        db.addComic(
//            Comic(
//                title = "testy",
//                author = "Demo Author",
//                pages = null,
//                imageUrl = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fpbs.twimg.com%2Fprofile_images%2F344513261579032958%2F3172d8c8e90d8be9910ff0e87a3a57b3_400x400.png&f=1&nofb=1&ipt=ff64798cdaaa4726a5c41bea220c20618b781abb2ca474b1cb8cebc11e18488f",
//            )
//        )
//
//        // Demo chapter: locked now, free in 3 days
//        val threeDaysLater = System.currentTimeMillis() + 3L * 24L * 60L * 60L * 1000L
//        chapdb.addChapter(
//            Chapter(
//                id = 1,
//                comicId = 1,
//                chapterNumber = 3,
//                title = "The Beginning of the End",
//                thumbnailUrl = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fpbs.twimg.com%2Fprofile_images%2F344513261579032958%2F3172d8c8e90d8be9910ff0e87a3a57b3_400x400.png&f=1&nofb=1&ipt=ff64798cdaaa4726a5c41bea220c20618b781abb2ca474b1cb8cebc11e18488f",
//                releaseDate = "Jan 01, 2024",
//                likeCount = 15402,
//                pages = 45,
//                cost = 300,
//                isLocked = true,
//                freeDate = threeDaysLater,
//                remoteUrl = "https://www.ics.uci.edu/~magda/cs620/ch4.pdf"
//            )
//        )
    }

    private fun debugAllUsers() {
        val userDbHelper = com.example.pgm.model.Database.UserDatabaseHelper(this)
        Log.d("LoginActivity", "=== ALL USERS IN DATABASE ===")
        val allUsers = userDbHelper.getAllUsers()
        allUsers.forEach { user ->
            val tokens = userDbHelper.getUserTokens(user.id ?: -1)
            Log.d("LoginActivity", "User: ${user.name} (ID: ${user.id}) - Role: ${user.role} - Tokens: $tokens - Email: ${user.email}")
        }
        Log.d("LoginActivity", "=== END USER DEBUG ===")
    }
}