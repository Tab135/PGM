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
import com.google.android.material.textfield.TextInputEditText

class ChapterManagementActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChapterManagementAdapter
    private lateinit var chapterController: ChapterController
    private lateinit var backButton: ImageButton
    private lateinit var addChapterButton: Button
    private lateinit var searchEditText: TextInputEditText

    private var comicId: Int = -1
    private var chaptersList: MutableList<Chapter> = mutableListOf()
    private var allChaptersList: MutableList<Chapter> = mutableListOf()

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
        setupControllers()
        loadChapters()
        setupSearch()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.chapterRecyclerView)
        backButton = findViewById(R.id.backButton)
        addChapterButton = findViewById(R.id.addChapterButton)
        searchEditText = findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupControllers() {
        chapterController = ChapterController(this)
    }

    private fun loadChapters() {
        allChaptersList.clear()
        allChaptersList.addAll(chapterController.getChaptersByComicId(comicId))

        chaptersList.clear()
        chaptersList.addAll(allChaptersList)

        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = ChapterManagementAdapter(
            chaptersList,
            onViewClick = { chapter ->
                // Open chapter viewer
                val intent = Intent(this, ChapterActivity::class.java).apply {
                    putExtra("chapterId", chapter.id)
                    putExtra("comicId", comicId)
                }
                startActivity(intent)
            },
            onEditClick = { chapter ->
                // Open edit chapter activity
                val intent = Intent(this, AddEditChapterActivity::class.java).apply {
                    putExtra("comicId", comicId)
                    putExtra("chapterId", chapter.id)
                    putExtra("chapterNumber", chapter.chapterNumber)
                    putExtra("chapterTitle", chapter.title)
                    putExtra("pages", chapter.pages)
                    putExtra("cost", chapter.cost)
                    putExtra("releaseDate", chapter.releaseDate)
                }
                startActivityForResult(intent, EDIT_CHAPTER_REQUEST)
            },
            onDeleteClick = { chapter ->
                showDeleteConfirmation(chapter)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.isEmpty()) {
                    loadChapters()
                } else {
                    searchChapters(query)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Handle search action on keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search is performed
                val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun searchChapters(query: String) {
        val searchQuery = query.lowercase().trim()

        // Filter chapters by chapter number and title
        val filteredList = allChaptersList.filter { chapter ->
            val numberMatch = chapter.chapterNumber.toString().contains(searchQuery)
            val titleMatch = chapter.title.lowercase().contains(searchQuery)
            numberMatch || titleMatch
        }

        chaptersList.clear()
        chaptersList.addAll(filteredList)
        adapter.notifyDataSetChanged()

        // Show a message if no results found
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No chapters found for '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        addChapterButton.setOnClickListener {
            val intent = Intent(this, AddEditChapterActivity::class.java).apply {
                putExtra("comicId", comicId)
            }
            startActivityForResult(intent, ADD_CHAPTER_REQUEST)
        }
    }

    private fun showDeleteConfirmation(chapter: Chapter) {
        AlertDialog.Builder(this)
            .setTitle("Delete Chapter")
            .setMessage("Are you sure you want to delete Chapter ${chapter.chapterNumber}: '${chapter.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteChapter(chapter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteChapter(chapter: Chapter) {
        if (chapterController.deleteChapter(chapter.id)) {
            chaptersList.remove(chapter)
            allChaptersList.remove(chapter)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Chapter deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete chapter", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_CHAPTER_REQUEST, EDIT_CHAPTER_REQUEST -> {
                    loadChapters()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning from other activities
        loadChapters()
    }

    companion object {
        const val ADD_CHAPTER_REQUEST = 2001
        const val EDIT_CHAPTER_REQUEST = 2002
    }
}
