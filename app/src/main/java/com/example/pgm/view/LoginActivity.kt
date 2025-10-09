package com.example.pgm.view

import com.example.pgm.Controller.LoginController
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.Comic
import com.example.pgm.model.Database.ChapterDatabaseHelper
import com.example.pgm.model.Database.ComicDatabaseHelper

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = ComicDatabaseHelper(this)
        val chapdb = ChapterDatabaseHelper(this)
        // ✅ Insert a test comic
        db.addComic(
            Comic(
                title = "testy",
                author = "Demo Author",
                pages = null, // optional
                localPath = null,
                imageUrl = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fpbs.twimg.com%2Fprofile_images%2F344513261579032958%2F3172d8c8e90d8be9910ff0e87a3a57b3_400x400.png&f=1&nofb=1&ipt=ff64798cdaaa4726a5c41bea220c20618b781abb2ca474b1cb8cebc11e18488f",
                remoteUrl = "https://getsamplefiles.com/download/pdf/sample-1.pdf"
            )
        );
        chapdb.addChapter(
            Chapter(
                id = 1,
                comicId = 1,
                chapterNumber = 1,
                title = "The Beginning of the End",
                thumbnailUrl = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fpbs.twimg.com%2Fprofile_images%2F344513261579032958%2F3172d8c8e90d8be9910ff0e87a3a57b3_400x400.png&f=1&nofb=1&ipt=ff64798cdaaa4726a5c41bea220c20618b781abb2ca474b1cb8cebc11e18488f",
                releaseDate = "Jan 01, 2024",
                likeCount = 15402,
                isRead = true,
                pages = 45,
                remoteUrl = "https://getsamplefiles.com/download/pdf/sample-1.pdf"
            )
        )

        LoginController(this, findViewById(R.id.loginContainer))
    }

}