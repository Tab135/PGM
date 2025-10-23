package com.example.pgm.view.Comic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.ComicController
import com.example.pgm.Controller.UserComicHistoryController
import com.example.pgm.Controller.FeedbackController
import com.example.pgm.utils.SessionManager
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.Comic
import com.example.pgm.model.UserComicHistory
import com.example.pgm.model.TokenManager
import com.example.pgm.model.Database.UserDatabaseHelper
import com.example.pgm.view.Feedback.FeedbackActivity
import com.example.pgm.view.Payment.TokenShopActivity
class ChapterListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChapterAdapter
    private lateinit var chapterController: ChapterController
    private lateinit var comicController: ComicController
    private lateinit var userComicHistoryController: UserComicHistoryController
    private lateinit var feedbackController: FeedbackController
    private lateinit var sessionManager: SessionManager
    private lateinit var tokenManager: TokenManager
    private lateinit var userDbHelper: UserDatabaseHelper

    private lateinit var comicCover: ImageView
    private lateinit var comicTitle: TextView
    private lateinit var comicAuthor: TextView
    private lateinit var comicGenre: TextView
    private lateinit var viewCount: TextView
    private lateinit var subscriberCount: TextView
    private lateinit var rating: TextView
    private lateinit var backButton: ImageView
    private lateinit var feedbackButton: ImageView
    private lateinit var rateButton: TextView
    private lateinit var subscribeButton: androidx.cardview.widget.CardView
    private lateinit var tokenBalanceTextView: TextView // Add token display
    private lateinit var shopButton: ImageView
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
        updateTokenDisplay() // Display user's token balance
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
        feedbackButton = findViewById(R.id.feedbackButton)
        rateButton = findViewById(R.id.rateButton)
        subscribeButton = findViewById(R.id.subscribeButton)
        tokenBalanceTextView = findViewById(R.id.tokenBalanceTextView) // Initialize token TextView
        shopButton = findViewById(R.id.shopButton)
    }

    private fun setupControllers() {
        chapterController = ChapterController(this)
        comicController = ComicController(this)
        userComicHistoryController = UserComicHistoryController(this)
        feedbackController = FeedbackController(this)
        sessionManager = SessionManager(this)
        tokenManager = TokenManager(this)
        userDbHelper = UserDatabaseHelper(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
        if (currentUserId == -1) {
            // User not logged in, but we can still show chapters without history
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

    private fun updateTokenDisplay() {
        if (currentUserId != -1) {
            // Debug the token issue
            tokenManager.debugTokenIssue(currentUserId)

            val tokenBalance = tokenManager.getUserTokenBalance(currentUserId)
            tokenBalanceTextView.text = "🪙 $tokenBalance"
            tokenBalanceTextView.visibility = android.view.View.VISIBLE
            shopButton.visibility = android.view.View.VISIBLE // Show shop button when logged in

            Log.d("TokenDebug", "Displaying tokens for user $currentUserId: $tokenBalance")
        } else {
            tokenBalanceTextView.visibility = android.view.View.GONE
            shopButton.visibility = android.view.View.GONE // Hide shop button when not logged in
            Log.d("TokenDebug", "User not logged in, hiding token display")
        }
    }

    private fun loadComicInfo() {
        // Get comic info from database or use the passed data
        val comics = comicController.getAllComics()
        currentComic = comics.find { it.id == comicId }

        currentComic?.let { comic ->
            comicTitle.text = comic.title
            comicAuthor.text = comic.author ?: "Unknown Author"
            comicGenre.text = "Romance" // Default genre

            // Set sample stats
            viewCount.text = "1.7M"
            subscriberCount.text = "122,034"

            // Load feedback rating
            loadFeedbackRating()

            // Show user's reading progress if available
            userComicHistory?.let { history ->
                val totalChapters = chapterController.getChaptersByComicId(comicId).size
                val overallProgress = if (totalChapters > 0) {
                    (history.viewedChapters.size.toFloat() / totalChapters * 100).toInt()
                } else 0

                if (overallProgress > 0) {
                    comicGenre.text = "Romance • ${overallProgress}% completed • ${history.viewedChapters.size}/${totalChapters} chapters"

                    if (history.readingStreak > 0) {
                        viewCount.text = "${history.readingStreak} day streak"
                    }

                    if (history.isFavorite) {
                        subscriberCount.text = "⭐ Favorited"
                    }
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

    private fun loadFeedbackRating() {
        val avgRating = feedbackController.getAverageRating(comicId)
        val feedbackCount = feedbackController.getFeedbackCount(comicId)

        if (feedbackCount > 0) {
            rating.text = String.format("%.1f (%d)", avgRating, feedbackCount)
        } else {
            rating.text = "No ratings"
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

        // Token balance click - show token info or shop
        tokenBalanceTextView.setOnClickListener {
            showTokenInfoDialog()
        }
        shopButton.setOnClickListener {
            openTokenShop()
        }
        // Feedback button click listener
        feedbackButton.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            intent.putExtra("comicId", comicId)
            startActivity(intent)
        }

        // Rate button click listener - also opens feedback
        rateButton.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            intent.putExtra("comicId", comicId)
            startActivity(intent)
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
    private fun openTokenShop() {
        if (currentUserId == -1) {
            android.widget.Toast.makeText(this, "Please log in to access the token shop", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, TokenShopActivity::class.java)
        startActivity(intent)
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
        if (currentUserId == -1) {
            android.widget.Toast.makeText(this, "Please log in to read chapters", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Check if chapter is accessible (free or already purchased)
        if (tokenManager.isChapterAccessible(chapter, userComicHistory)) {
            // Can read directly
            openChapterViewer(chapter)
        } else {
            // Chapter is locked - need to unlock with tokens
            showUnlockDialog(chapter)
        }
    }

    private fun openChapterViewer(chapter: Chapter) {
        // Record in user comic history
        userComicHistoryController.recordChapterViewed(
            currentUserId,
            comicId,
            chapter.id,
            chapter.pages ?: 0
        )

        // Update local history object
        userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)

        // Open chapter viewer
        val intent = Intent(this, ComicViewerActivity::class.java)
        intent.putExtra("chapterId", chapter.id)
        intent.putExtra("comicId", comicId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from chapter viewer
        loadChapters()
        loadFeedbackRating()
        updateTokenDisplay() // Refresh token balance
    }

    private fun toggleChapterLike(chapter: Chapter) {
        // Update user comic history if user is logged in
        if (currentUserId != -1) {
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
        if (currentUserId == -1) {
            android.widget.Toast.makeText(this, "Please log in to unlock chapters", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val tokenBalance = tokenManager.getUserTokenBalance(currentUserId)
        val chapterCost = chapter.cost
        val days = chapter.daysUntilFree()

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Unlock Chapter?")

        val message = buildString {
            append("Chapter: ${chapter.title}\n")
            append("Cost: $chapterCost tokens\n")
            append("Your balance: $tokenBalance tokens\n")
            if (days > 0) {
                append("\nOr wait $days day(s) for free access")
            }

            if (tokenBalance < chapterCost) {
                append("\n\n⚠️ Insufficient tokens!")
            } else {
                append("\nBalance after unlock: ${tokenBalance - chapterCost} tokens")
            }
        }

        builder.setMessage(message)

        if (tokenBalance >= chapterCost) {
            builder.setPositiveButton("Unlock") { _, _ ->
                unlockChapter(chapter)
            }
        } else {
            builder.setPositiveButton("Get More Tokens") { _, _ ->
                showGetTokensDialog()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
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
        if (currentUserId == -1) {
            android.widget.Toast.makeText(this, "Please log in to unlock chapters", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Use TokenManager to unlock chapter
        val result = tokenManager.unlockChapter(currentUserId, chapter, userComicHistory)

        if (result.success) {
            // Show success message
            android.widget.Toast.makeText(
                this,
                "Chapter unlocked! Spent ${result.tokensSpent} tokens. Balance: ${result.remainingTokens}",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // Refresh local history and UI
            userComicHistory = userComicHistoryController.getOrCreateUserComicHistory(currentUserId, comicId)
            updateTokenDisplay()
            loadChapters()

            // Open chapter viewer
            openChapterViewer(chapter)
        } else {
            // Show error message
            android.widget.Toast.makeText(this, result.message, android.widget.Toast.LENGTH_LONG).show()

            // Offer to get more tokens
            showGetTokensDialog()
        }
    }

    private fun showTokenInfoDialog() {
        if (currentUserId == -1) {
            android.widget.Toast.makeText(this, "Please log in to view tokens", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val tokenBalance = tokenManager.getUserTokenBalance(currentUserId)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Your Token Balance")
        builder.setMessage(
            "Current balance: $tokenBalance tokens\n\n" +
                    "• Use tokens to unlock locked chapters\n" +
                    "• Each chapter costs varies\n" +
                    "• Purchased chapters are yours forever!"
        )
        builder.setPositiveButton("Open Token Shop") { _, _ ->
            openTokenShop()
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun showGetTokensDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Get More Tokens")
        builder.setMessage(
            "You need more tokens to unlock this chapter!\n\n" +
                    "Ways to get tokens:\n" +
                    "• Purchase token packages\n" +
                    "• Daily login bonus\n" +
                    "• Complete comics\n" +
                    "• Special events & promotions"
        )

        builder.setPositiveButton("Open Token Shop") { _, _ ->
            openTokenShop()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

}