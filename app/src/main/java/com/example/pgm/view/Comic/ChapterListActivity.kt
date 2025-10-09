package com.example.pgm.view.Comic

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.ComicController
import com.example.pgm.Controller.UserComicHistoryController
import com.example.pgm.utils.SessionManager
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.Comic
import com.example.pgm.model.UserComicHistory

class ChapterListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChapterAdapter
    private lateinit var chapterController: ChapterController
    private lateinit var comicController: ComicController
    private lateinit var userComicHistoryController: UserComicHistoryController
    private lateinit var sessionManager: SessionManager
    
    private lateinit var comicCover: ImageView
    private lateinit var comicTitle: TextView
    private lateinit var comicAuthor: TextView
    private lateinit var comicGenre: TextView
    private lateinit var viewCount: TextView
    private lateinit var subscriberCount: TextView
    private lateinit var rating: TextView
    private lateinit var backButton: ImageView
    private lateinit var subscribeButton: androidx.cardview.widget.CardView
    
    private var currentComic: Comic? = null
    private var comicId: Int = -1
    private var currentUserId: Int = -1
    private var userComicHistory: UserComicHistory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_list)

        comicId = intent.getIntExtra("comicId", -1)
        if (comicId == -1) {
            finish()
            return
        }

        initViews()
        setupControllers()
        loadUserSession()
        loadComicInfo()
        loadUserHistory()
        loadChapters()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.chapterRecyclerView)
        comicCover = findViewById(R.id.comicCover)
        comicTitle = findViewById(R.id.comicTitle)
        comicAuthor = findViewById(R.id.comicAuthor)
        comicGenre = findViewById(R.id.comicGenre)
        viewCount = findViewById(R.id.viewCount)
        subscriberCount = findViewById(R.id.subscriberCount)
        rating = findViewById(R.id.rating)
        backButton = findViewById(R.id.backButton)
        subscribeButton = findViewById(R.id.subscribeButton)
    }

    private fun setupControllers() {
        chapterController = ChapterController(this)
        comicController = ComicController(this)
        userComicHistoryController = UserComicHistoryController(this)
        sessionManager = SessionManager(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
        if (currentUserId == -1) {
            // User not logged in, but we can still show chapters without history
            // You might want to redirect to login page instead
        }
    }

    private fun loadUserHistory() {
        if (currentUserId != -1) {
            userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            
            // Initialize subscribe button text based on favorite status
            initializeSubscribeButton()
            
            // Show continue reading dialog if user has reading history
            userComicHistory?.latestViewedChapter?.let {
                // Delay showing dialog to ensure UI is loaded
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    showContinueReadingDialog()
                }, 500)
            }
        } else {
            // Initialize subscribe button for non-logged in users
            initializeSubscribeButton()
        }
    }

    private fun initializeSubscribeButton() {
        val subscribeText = subscribeButton.findViewById<TextView>(R.id.subscribeText)
        userComicHistory?.let { history ->
            if (history.isFavorite) {
                subscribeText?.text = "Favorited"
            } else {
                subscribeText?.text = "Subscribe"
            }
        } ?: run {
            subscribeText?.text = "Subscribe"
        }
    }

    private fun loadComicInfo() {
        // Get comic info from database or use the passed data
        val comics = comicController.getAllComics()
        currentComic = comics.find { it.id == comicId }
        
        currentComic?.let { comic ->
            comicTitle.text = comic.title
            comicAuthor.text = comic.author ?: "Unknown Author"
            comicGenre.text = "Romance" // Default genre, you can add this to Comic model later
            
            // Set sample stats (these would come from your data source)
            viewCount.text = "1.7M"
            subscriberCount.text = "122,034"
            rating.text = "9.23"
            
            // Show user's reading progress if available
            userComicHistory?.let { history ->
                val progressPercentage = (history.readingProgress * 100).toInt()
                val totalChapters = chapterController.getChaptersByComicId(comicId).size
                val overallProgress = if (totalChapters > 0) {
                    (history.viewedChapters.size.toFloat() / totalChapters * 100).toInt()
                } else 0
                
                if (overallProgress > 0) {
                    // Show reading progress in the genre field
                    comicGenre.text = "Romance • ${overallProgress}% completed • ${history.viewedChapters.size}/${totalChapters} chapters"
                    
                    // Show reading streak if > 0
                    if (history.readingStreak > 0) {
                        // You could update the view count to show streak
                        viewCount.text = "${history.readingStreak} day streak"
                    }
                    
                    // Show favorite status
                    if (history.isFavorite) {
                        subscriberCount.text = "⭐ Favorited"
                    }
                }
                
                // Show last read chapter info
                history.latestViewedChapter?.let { lastChapter ->
                    // This info will be shown in the continue reading dialog
                }
            }
            
            // Load comic cover
            comic.imageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(comicCover)
            }
        }
    }

    private fun loadChapters() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        val chapters = chapterController.getChaptersByComicId(comicId)
        
        // Get the highest chapter read by the user
        val highestChapterRead = getHighestChapterRead()

        adapter = ChapterAdapter(
            chapters = chapters,
            userComicHistory = userComicHistory,
            highestChapterRead = highestChapterRead,
            onChapterClick = { chapter -> openChapter(chapter) },
            onLikeClick = { chapter -> toggleChapterLike(chapter) }
        )
        recyclerView.adapter = adapter
    }

    private fun getHighestChapterRead(): Int {
        userComicHistory?.let { history ->
            if (history.viewedChapters.isNotEmpty()) {
                val allChapters = chapterController.getChaptersByComicId(comicId)
                val viewedChapterNumbers = history.viewedChapters.mapNotNull { chapterId ->
                    allChapters.find { it.id == chapterId }?.chapterNumber
                }
                return viewedChapterNumbers.maxOrNull() ?: 0
            }
        }
        return 0
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        subscribeButton.setOnClickListener {
            // Handle subscription/favorite logic
            if (currentUserId != -1) {
                userComicHistoryController.toggleFavorite(currentUserId, comicId)
                userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
                
                // Update UI to reflect favorite status
                updateFavoriteButton()
            } else {
                // Show login prompt
                android.widget.Toast.makeText(this, "Please log in to add favorites", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteButton() {
        // Update the subscribe button based on favorite status
        userComicHistory?.let { history ->
            val subscribeText = subscribeButton.findViewById<TextView>(R.id.subscribeText)
            if (history.isFavorite) {
                subscribeText?.text = "Favorited"
                android.widget.Toast.makeText(this, "Added to favorites", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                subscribeText?.text = "Subscribe"
                android.widget.Toast.makeText(this, "Removed from favorites", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openChapter(chapter: Chapter) {
        if (chapter.isLocked) {
            // Show unlock dialog or purchase screen
            showUnlockDialog(chapter)
        } else {
            // Record in user comic history if user is logged in
            if (currentUserId != -1) {
                userComicHistoryController.recordChapterViewed(
                    currentUserId, 
                    comicId, 
                    chapter.id,
                    chapter.pages ?: 0
                )
                
                // Update local history object
                userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            }
            
            // Open chapter viewer (you can create this activity later)
            val intent = Intent(this, ComicViewerActivity::class.java)
            intent.putExtra("chapterId", chapter.id)
            intent.putExtra("comicId", comicId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning from chapter viewer to show updated read status
        loadChapters()
    }

    private fun toggleChapterLike(chapter: Chapter) {

            // Update user comic history if user is logged in
            if (currentUserId != -1) {
                // Use the new toggle function that checks current like status automatically
                userComicHistoryController.toggleChapterLike(currentUserId, comicId, chapter.id)
                
                // Update local history object
                userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            }

        val success = chapterController.updateChapterLike(
            chapter.id,
            chapter.likeCount
        )

        // Refresh the list to show updated like status
        loadChapters()

    }

    private fun showUnlockDialog(chapter: Chapter) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Unlock Chapter")
        builder.setMessage("This chapter costs ${chapter.cost} coins or will be free in ${chapter.freeDays} days.")
        builder.setPositiveButton("Unlock") { _, _ ->
            // Handle unlock with coins
            // For now, just unlock it for demonstration
            unlockChapter(chapter)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun addBookmark(chapter: Chapter) {
        if (currentUserId != -1) {
            userComicHistoryController.addBookmark(currentUserId, comicId, chapter.id)
            userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            loadChapters() // Refresh to show bookmark
            android.widget.Toast.makeText(this, "Bookmark added", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeBookmark(chapter: Chapter) {
        if (currentUserId != -1) {
            userComicHistoryController.removeBookmark(currentUserId, comicId, chapter.id)
            userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            loadChapters() // Refresh to remove bookmark
            android.widget.Toast.makeText(this, "Bookmark removed", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContinueReadingDialog() {
        userComicHistory?.latestViewedChapter?.let { lastChapterId ->
            val lastChapter = chapterController.getChapterById(lastChapterId)
            lastChapter?.let { chapter ->
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Continue Reading")
                builder.setMessage("Continue from Chapter ${chapter.chapterNumber}: ${chapter.title}?")
                builder.setPositiveButton("Continue") { _, _ ->
                    openChapter(chapter)
                }
                builder.setNegativeButton("Start from Beginning", null)
                builder.show()
            }
        }
    }

    private fun unlockChapter(chapter: Chapter) {
        // In a real app, you would handle the coin transaction here
        // For demonstration, we'll just open the chapter
        val intent = Intent(this, ComicViewerActivity::class.java)
        intent.putExtra("chapterId", chapter.id)
        intent.putExtra("comicId", comicId)
        startActivity(intent)
    }
}