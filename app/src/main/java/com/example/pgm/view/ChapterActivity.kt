package com.example.pgm.view

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.UserComicHistoryController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.utils.SessionManager
import com.example.pgm.view.Comic.ComicPageAdapter
import org.json.JSONArray

class ChapterActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pageIndicator: TextView
    private lateinit var chapterTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var nextChapterButton: ImageButton
    private lateinit var prevChapterButton: ImageButton
    private lateinit var bookmarkButton: ImageButton

    private lateinit var chapterController: ChapterController
    private lateinit var userComicHistoryController: UserComicHistoryController
    private lateinit var sessionManager: SessionManager

    private var currentChapter: Chapter? = null
    private var chapterId: Int = -1
    private var comicId: Int = -1
    private var currentUserId: Int = -1
    private var pageUrls: List<String> = emptyList()
    private var allChapters: List<Chapter> = emptyList()
    private var currentChapterIndex: Int = -1
    private var startReadingTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter)

        // Get intent data
        chapterId = intent.getIntExtra("chapterId", -1)
        comicId = intent.getIntExtra("comicId", -1)

        if (chapterId == -1 || comicId == -1) {
            Toast.makeText(this, "Invalid chapter data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupControllers()
        loadUserSession()
        loadChapterData()
        setupViewPager()
        setupClickListeners()

        startReadingTime = System.currentTimeMillis()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        pageIndicator = findViewById(R.id.pageIndicator)
        chapterTitle = findViewById(R.id.chapterTitle)
        backButton = findViewById(R.id.backButton)
        nextChapterButton = findViewById(R.id.nextChapterButton)
        prevChapterButton = findViewById(R.id.prevChapterButton)
        bookmarkButton = findViewById(R.id.bookmarkButton)
    }

    private fun setupControllers() {
        chapterController = ChapterController(this)
        userComicHistoryController = UserComicHistoryController(this)
        sessionManager = SessionManager(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
    }

    private fun loadChapterData() {
        // Load current chapter
        currentChapter = chapterController.getChapterById(chapterId)

        if (currentChapter == null) {
            Toast.makeText(this, "Chapter not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load all chapters for navigation
        allChapters = chapterController.getChaptersByComicId(comicId)
        currentChapterIndex = allChapters.indexOfFirst { it.id == chapterId }

        // Update UI
        chapterTitle.text = "Chapter ${currentChapter!!.chapterNumber}: ${currentChapter!!.title}"

        // Load page URLs
        loadPageUrls()

        // Update navigation buttons
        updateNavigationButtons()

        // Check bookmark status
        updateBookmarkButton()
    }

    private fun loadPageUrls() {
        currentChapter?.let { chapter ->
            val urls = mutableListOf<String>()

            // Try to load from pagesRemote first (JSON array)
            chapter.remoteUrl?.let { remoteJson ->
                try {
                    val jsonArray = JSONArray(remoteJson)
                    for (i in 0 until jsonArray.length()) {
                        urls.add(jsonArray.getString(i))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // If no remote URLs, try local paths
            if (urls.isEmpty()) {
                chapter.localPath?.let { localJson ->
                    try {
                        val jsonArray = JSONArray(localJson)
                        for (i in 0 until jsonArray.length()) {
                            urls.add(jsonArray.getString(i))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // If still no URLs, create placeholder URLs based on page count
            if (urls.isEmpty() && chapter.pages != null && chapter.pages > 0) {
                for (i in 1..chapter.pages) {
                    urls.add("https://via.placeholder.com/800x1200?text=Page+$i")
                }
            }

            pageUrls = urls
        }
    }

    private fun setupViewPager() {
        if (pageUrls.isEmpty()) {
            Toast.makeText(this, "No pages available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val adapter = ComicPageAdapter(pageUrls)
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Update page indicator
        updatePageIndicator(0)

        // Listen for page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicator(position)

                // Save reading progress
              //  saveReadingProgress(position)

                // Check if reached last page
                if (position == pageUrls.size - 1) {
                    markChapterAsRead()
                }
            }
        })

        // Restore last reading position if available
        if (currentUserId != -1) {
            val history = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            if (history.latestViewedChapter == chapterId && history.lastViewedPage > 0) {
                viewPager.setCurrentItem(history.lastViewedPage, false)
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        nextChapterButton.setOnClickListener {
            goToNextChapter()
        }

        prevChapterButton.setOnClickListener {
            goToPreviousChapter()
        }

        bookmarkButton.setOnClickListener {
            toggleBookmark()
        }

        // Toggle UI visibility on tap
        viewPager.setOnClickListener {
            toggleUIVisibility()
        }
    }

    private fun updatePageIndicator(position: Int) {
        pageIndicator.text = "${position + 1} / ${pageUrls.size}"
    }

    private fun updateNavigationButtons() {
        prevChapterButton.isEnabled = currentChapterIndex > 0
        prevChapterButton.alpha = if (currentChapterIndex > 0) 1.0f else 0.3f

        nextChapterButton.isEnabled = currentChapterIndex < allChapters.size - 1
        nextChapterButton.alpha = if (currentChapterIndex < allChapters.size - 1) 1.0f else 0.3f
    }

    private fun updateBookmarkButton() {
        if (currentUserId != -1) {
            val history = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            val isBookmarked = history.bookmarkedChapters.contains(chapterId)

            bookmarkButton.setImageResource(
                if (isBookmarked) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

//    private fun saveReadingProgress(page: Int) {
//        if (currentUserId != -1) {
//            userComicHistoryController.updateReadingProgress(
//                currentUserId,
//                comicId,
//                chapterId,
//                page
//            )
//        }
//    }

    private fun markChapterAsRead() {
        if (currentUserId != -1) {
            userComicHistoryController.recordChapterViewed(
                currentUserId,
                comicId,
                chapterId,
                pageUrls.size
            )
        }
    }

    private fun goToNextChapter() {
        if (currentChapterIndex < allChapters.size - 1) {
            val nextChapter = allChapters[currentChapterIndex + 1]

            if (nextChapter.isLocked) {
                Toast.makeText(this, "This chapter is locked", Toast.LENGTH_SHORT).show()
                return
            }

            // Restart activity with new chapter
            intent.putExtra("chapterId", nextChapter.id)
            recreate()
        }
    }

    private fun goToPreviousChapter() {
        if (currentChapterIndex > 0) {
            val prevChapter = allChapters[currentChapterIndex - 1]

            // Restart activity with new chapter
            intent.putExtra("chapterId", prevChapter.id)
            recreate()
        }
    }

    private fun toggleBookmark() {
        if (currentUserId != -1) {
            val history = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            val isBookmarked = history.bookmarkedChapters.contains(chapterId)

            if (isBookmarked) {
                userComicHistoryController.removeBookmark(currentUserId, comicId, chapterId)
                Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show()
            } else {
                userComicHistoryController.addBookmark(currentUserId, comicId, chapterId)
                Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show()
            }

            updateBookmarkButton()
        } else {
            Toast.makeText(this, "Please login to bookmark", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleUIVisibility() {
        val visibility = if (chapterTitle.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        chapterTitle.visibility = visibility
        pageIndicator.visibility = visibility
        backButton.visibility = visibility
        nextChapterButton.visibility = visibility
        prevChapterButton.visibility = visibility
        bookmarkButton.visibility = visibility
    }

//    override fun onPause() {
//        super.onPause()
//
//        // Save reading time
//        if (currentUserId != -1) {
//            val readingTime = (System.currentTimeMillis() - startReadingTime) / 1000 // in seconds
//            userComicHistoryController.updateReadingTime(currentUserId, comicId, readingTime.toInt())
//        }
//    }

    override fun onResume() {
        super.onResume()
        startReadingTime = System.currentTimeMillis()
    }
}