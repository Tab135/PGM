package com.example.pgm.view.Comic

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.Controller.ComicController
import com.example.pgm.Controller.UserComicHistoryController
import com.example.pgm.utils.SessionManager
import com.example.pgm.view.ProfileActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ComicListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComicAdapter
    private lateinit var comicController: ComicController
    private lateinit var userComicHistoryController: UserComicHistoryController
    private lateinit var sessionManager: SessionManager
    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchEditText: TextInputEditText
    private var allComics = listOf<com.example.pgm.model.Comic>()
    private var filteredComics = listOf<com.example.pgm.model.Comic>()
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_list)

        setupControllers()
        loadUserSession()
        setupToolbar()
        setupRecyclerView()
        setupSearchAndFilter()
        setupStats()
    }

    private fun setupControllers() {
        comicController = ComicController(this)
        userComicHistoryController = UserComicHistoryController(this)
        sessionManager = SessionManager(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }



    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.comicRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        allComics = comicController.getAllComics()
        filteredComics = allComics

        adapter = ComicAdapter(filteredComics) { comic ->
            val intent = Intent(this, ChapterListActivity::class.java)
            intent.putExtra("comicId", comic.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Show empty state if no comics
        updateEmptyState(filteredComics.isEmpty())
    }

    private fun setupSearchAndFilter() {
        searchEditText = findViewById(R.id.searchEditText)

        // Setup search text change listener
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                performSearch(query)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Handle search action on keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when search is performed
                val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }

        val filterChipGroup = findViewById<ChipGroup>(R.id.filterChipGroup)
        filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            applyFilters(checkedIds)
        }

        // Setup sort button
        val btnSort = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSort)
        btnSort.setOnClickListener {
            showSortOptions()
        }
    }

    private fun performSearch(query: String) {
        val searchQuery = query.lowercase().trim()

        if (searchQuery.isEmpty()) {
            // If search is empty, show all comics with current filters
            applyFilters(getCurrentChipFilters())
        } else {
            // Filter comics by title and author
            val searchFiltered = allComics.filter { comic ->
                val titleMatch = comic.title.lowercase().contains(searchQuery)
                val authorMatch = comic.author?.lowercase()?.contains(searchQuery) ?: false
                titleMatch || authorMatch
            }

            // Apply chip filters on top of search results
            val chipFiltered = applyChipFilters(searchFiltered, getCurrentChipFilters())
            filteredComics = chipFiltered
            adapter.updateData(filteredComics)
            updateEmptyState(filteredComics.isEmpty())
            updateSectionTitle(getCurrentChipFilters(), searchQuery)
        }
    }

    private fun setupStats() {
        updateStats()
    }

    private fun updateStats() {
        // Get all comics from database
        val allComicsInDb = comicController.getAllComics()
        val totalComicsInDb = allComicsInDb.size

        if (currentUserId != -1) {
            // User is logged in - show reading statistics based on user history
            val stats = userComicHistoryController.getUserReadingStatistics(currentUserId)
            
            findViewById<android.widget.TextView>(R.id.tvTotalCount).text = totalComicsInDb.toString()
            findViewById<android.widget.TextView>(R.id.tvReadingCount).text = stats.reading.toString()
        } else {
            // User not logged in - show total comics but 0 for reading
            findViewById<android.widget.TextView>(R.id.tvTotalCount).text = totalComicsInDb.toString()
            findViewById<android.widget.TextView>(R.id.tvReadingCount).text = "0"
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val emptyStateLayout = findViewById<android.widget.LinearLayout>(R.id.emptyStateLayout)
        val recyclerView = findViewById<RecyclerView>(R.id.comicRecyclerView)

        if (isEmpty) {
            emptyStateLayout.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            emptyStateLayout.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE
        }
    }

    private fun applyFilters(checkedIds: List<Int>) {
        val searchQuery = searchEditText.text?.toString() ?: ""
        val comicsToFilter = if (searchQuery.isNotEmpty()) {
            // If there's a search query, filter from all comics first
            allComics.filter { comic ->
                val titleMatch = comic.title.lowercase().contains(searchQuery.lowercase())
                val authorMatch = comic.author?.lowercase()?.contains(searchQuery.lowercase()) ?: false
                titleMatch || authorMatch
            }
        } else {
            allComics
        }

        filteredComics = applyChipFilters(comicsToFilter, checkedIds)
        adapter.updateData(filteredComics)
        updateEmptyState(filteredComics.isEmpty())
        updateSectionTitle(checkedIds, searchQuery)
    }

    private fun applyChipFilters(comics: List<com.example.pgm.model.Comic>, checkedIds: List<Int>): List<com.example.pgm.model.Comic> {
        if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
            return comics
        }

        // If user is not logged in, we can't filter by reading status
        if (currentUserId == -1) {
            return when {
                checkedIds.contains(R.id.chipReading) -> emptyList()
                checkedIds.contains(R.id.chipFavorites) -> emptyList()
                else -> comics
            }
        }

        // Fetch all user history once to avoid multiple database calls
        val allUserHistory = userComicHistoryController.getUserReadingHistory(currentUserId)
        val historyMap = allUserHistory.associateBy { it.comicId }

        return comics.filter { comic ->
            val history = historyMap[comic.id]
            
            when {
                checkedIds.contains(R.id.chipReading) -> {
                    // Filter comics that are being read (has viewed chapters)
                    history != null && history.viewedChapters.isNotEmpty()
                }
                checkedIds.contains(R.id.chipFavorites) -> {
                    // Filter favorite comics
                    history != null && history.isFavorite
                }
                else -> true
            }
        }
    }

    private fun getCurrentChipFilters(): List<Int> {
        val filterChipGroup = findViewById<ChipGroup>(R.id.filterChipGroup)
        return filterChipGroup.checkedChipIds
    }

    private fun updateSectionTitle(checkedIds: List<Int>, searchQuery: String = "") {
        val sectionTitle = findViewById<android.widget.TextView>(R.id.tvSectionTitle)

        val baseTitle = when {
            checkedIds.contains(R.id.chipReading) -> "Reading Comics"
            checkedIds.contains(R.id.chipFavorites) -> "Favorite Comics"
            else -> "All Comics"
        }

        val finalTitle = if (searchQuery.isNotEmpty()) {
            "$baseTitle · \"$searchQuery\""
        } else {
            baseTitle
        }

        sectionTitle.text = finalTitle
    }

    private fun showSortOptions() {
        val items = arrayOf("Title A-Z", "Title Z-A", "Recently Added", "Most Chapters")

        android.app.AlertDialog.Builder(this)
            .setTitle("Sort Comics")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> sortComicsByTitle(true)
                    1 -> sortComicsByTitle(false)
                    2 -> sortComicsByDate()
                    3 -> sortComicsByChapters()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sortComicsByTitle(ascending: Boolean) {
        val sortedComics = if (ascending) {
            filteredComics.sortedBy { it.title }
        } else {
            filteredComics.sortedByDescending { it.title }
        }
        adapter.updateData(sortedComics)
        Toast.makeText(this, if (ascending) "Sorted A-Z" else "Sorted Z-A", Toast.LENGTH_SHORT).show()
    }

    private fun sortComicsByDate() {
        // Implement date sorting logic when you have date field in Comic model
        // For now, just show a message
        Toast.makeText(this, "Sorted by Recent", Toast.LENGTH_SHORT).show()
    }

    private fun sortComicsByChapters() {
        // Implement chapter count sorting when you have chapterCount field
        // For now, just show a message
        Toast.makeText(this, "Sorted by Chapters", Toast.LENGTH_SHORT).show()
    }

    // Menu inflation
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Focus on search field when search icon is clicked
                searchEditText.requestFocus()
                val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                true
            }

            R.id.action_profile -> {
                goToProfile()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun openFilter() {
        // Show filter information or toggle chip visibility
        Toast.makeText(this, "Use chips below to filter comics", Toast.LENGTH_LONG).show()
    }

    private fun showAddComicDialog() {
        // TODO: Implement dialog or activity for adding comics
        Toast.makeText(this, "Add comic functionality", Toast.LENGTH_SHORT).show()

        // Example of how you might implement this:
        /*
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Add New Comic")
            .setView(R.layout.dialog_add_comic)
            .setPositiveButton("Add") { dialog, which ->
                // Handle comic addition
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        */
    }

    // Refresh data when returning to activity
    override fun onResume() {
        super.onResume()
        refreshComicsList()
    }

    private fun refreshComicsList() {
        allComics = comicController.getAllComics()
        // Reapply current search and filters
        performSearch(searchEditText.text?.toString() ?: "")
        updateStats()
    }

    // Clear search when needed
    fun clearSearch() {
        searchEditText.setText("")
        performSearch("")
    }
}