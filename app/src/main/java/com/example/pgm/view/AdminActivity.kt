package com.example.pgm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.Comic
import com.example.pgm.view.Comic.ComicViewerActivity
import androidx.appcompat.app.AlertDialog
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.ComicController
import com.example.pgm.view.Admin.AdminComicAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminComicAdapter
    private lateinit var comicController: ComicController
    private lateinit var chapterController: ChapterController
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var searchEditText: TextInputEditText
    private var comicsList: MutableList<Comic> = mutableListOf()
    private var allComicsList: MutableList<Comic> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is admin
        checkAdminAccess()

        setContentView(R.layout.activity_admin)

        initViews()
        loadComics()
        setupSearch()
    }

    private fun checkAdminAccess() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("userRole", "user")

        if (userRole != "admin") {
            Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.adminRecyclerView)
        fabAdd = findViewById(R.id.fabAddComic)
        searchEditText = findViewById(R.id.searchEditText) // Updated this line

        recyclerView.layoutManager = LinearLayoutManager(this)

        comicController = ComicController(this)
        chapterController = ChapterController(this)

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditComicActivity::class.java)
            startActivityForResult(intent, ADD_COMIC_REQUEST)
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (query.isEmpty()) {
                    loadComics()
                } else {
                    searchComics(query)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Optional: Handle search action on keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search is performed
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun loadComics() {
        allComicsList.clear()
        allComicsList.addAll(comicController.getAllComics())

        comicsList.clear()
        comicsList.addAll(allComicsList)

        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = AdminComicAdapter(
            comicsList,
            chapterController,
            onEdit = { comic ->
                val intent = Intent(this, AddEditComicActivity::class.java).apply {
                    putExtra("comic_id", comic.id)
                    putExtra("comic_title", comic.title)
                    putExtra("comic_author", comic.author)
                    putExtra("comic_pages", comic.pages)
                    putExtra("comic_image_url", comic.imageUrl)
                }
                startActivityForResult(intent, EDIT_COMIC_REQUEST)
            },
            onDelete = { comic ->
                showDeleteConfirmation(comic)
            },
            onView = { comic ->
                val intent = Intent(this, ComicViewerActivity::class.java).apply {
                    putExtra("comicId", comic.id)
                }
                startActivity(intent)
            },
            onManageChapters = { comic ->
                val intent = Intent(this, ChapterManagementActivity::class.java).apply {
                    putExtra("comicId", comic.id)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmation(comic: Comic) {
        AlertDialog.Builder(this)
            .setTitle("Delete Comic")
            .setMessage("Are you sure you want to delete '${comic.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteComic(comic)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComic(comic: Comic) {
        if (comicController.deleteComic(comic.id)) {
            comicsList.remove(comic)
            allComicsList.remove(comic)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Comic deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to delete comic", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Clear user session
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun searchComics(query: String) {
        val searchQuery = query.lowercase().trim()

        // Filter comics by both title and author name
        val filteredList = allComicsList.filter { comic ->
            val titleMatch = comic.title.lowercase().contains(searchQuery)
            val authorMatch = comic.author?.lowercase()?.contains(searchQuery) ?: false
            titleMatch || authorMatch
        }

        comicsList.clear()
        comicsList.addAll(filteredList)
        adapter.notifyDataSetChanged()

        // Optional: Show a message if no results found
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No comics found for '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_COMIC_REQUEST, EDIT_COMIC_REQUEST -> {
                    loadComics()
                }
            }
        }
    }

    companion object {
        const val ADD_COMIC_REQUEST = 1001
        const val EDIT_COMIC_REQUEST = 1002
    }
}