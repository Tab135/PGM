package com.example.pgm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.view.Comic.ChapterManagementAdapter
import com.example.pgm.view.Comic.ComicViewerActivity

class ChapterManagementActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChapterManagementAdapter
    private lateinit var chapterController: ChapterController
    private lateinit var addButton: Button
    private lateinit var backButton: ImageButton

    private var comicId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_management)

        comicId = intent.getIntExtra("comicId", -1)
        if (comicId == -1) {
            Toast.makeText(this, "Invalid comic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupController()
        loadChapters()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.chapterRecyclerView)
        addButton = findViewById(R.id.addChapterButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupController() {
        chapterController = ChapterController(this)
    }

    private fun loadChapters() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        val chapters = chapterController.getChaptersByComicId(comicId)

        adapter = ChapterManagementAdapter(
            chapters = chapters,
            onViewClick = { chapter -> viewChapter(chapter) },
            onEditClick = { chapter -> editChapter(chapter) },
            onDeleteClick = { chapter -> confirmDeleteChapter(chapter) }
        )
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener {
            addNewChapter()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun addNewChapter() {
        val intent = Intent(this, AddEditChapterActivity::class.java)
        intent.putExtra("comicId", comicId)
        intent.putExtra("mode", "add")
        startActivity(intent)
    }

    private fun editChapter(chapter: Chapter) {
        val intent = Intent(this, AddEditChapterActivity::class.java)
        intent.putExtra("comicId", comicId)
        intent.putExtra("chapterId", chapter.id)
        intent.putExtra("mode", "edit")
        startActivity(intent)
    }

    private fun viewChapter(chapter: Chapter) {
        val intent = Intent(this, ComicViewerActivity::class.java)
        intent.putExtra("chapterId", chapter.id)
        intent.putExtra("comicId", comicId)
        startActivity(intent)
    }

    private fun confirmDeleteChapter(chapter: Chapter) {
        AlertDialog.Builder(this)
            .setTitle("Delete Chapter")
            .setMessage("Are you sure you want to delete Chapter ${chapter.chapterNumber}: ${chapter.title}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteChapter(chapter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteChapter(chapter: Chapter) {
        val success = chapterController.deleteChapter(chapter.id)

        if (success) {
            Toast.makeText(this, "Chapter deleted successfully", Toast.LENGTH_SHORT).show()
            loadChapters() // Refresh list
        } else {
            Toast.makeText(this, "Failed to delete chapter", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadChapters() // Refresh list
    }
}