package com.example.pgm.view.Comic

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.Controller.ComicController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.Comic

class ChapterListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChapterAdapter
    private lateinit var chapterController: ChapterController
    private lateinit var comicController: ComicController
    
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
        loadComicInfo()
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
        
        var chapters = chapterController.getChaptersByComicId(comicId)
        
        // If no chapters exist, add sample chapters for demonstration
        if (chapters.isEmpty()) {
            chapterController.addSampleChapters(comicId)
            chapters = chapterController.getChaptersByComicId(comicId)
        }
        
        adapter = ChapterAdapter(
            chapters = chapters,
            onChapterClick = { chapter -> openChapter(chapter) },
            onLikeClick = { chapter -> toggleChapterLike(chapter) }
        )
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        subscribeButton.setOnClickListener {
            // Handle subscription logic
            // You can implement a subscription feature here
        }
    }

    private fun openChapter(chapter: Chapter) {
        if (chapter.isLocked) {
            // Show unlock dialog or purchase screen
            showUnlockDialog(chapter)
        } else {
            // Mark as read and open chapter
            chapterController.markChapterAsRead(chapter.id)
            
            // Open chapter viewer (you can create this activity later)
            val intent = Intent(this, ComicViewerActivity::class.java)
            intent.putExtra("chapterId", chapter.id)
            intent.putExtra("comicId", comicId)
            startActivity(intent)
            
            // Refresh the list to show updated read status
            loadChapters()
        }
    }

    private fun toggleChapterLike(chapter: Chapter) {
        val success = chapterController.toggleChapterLike(
            chapter.id, 
            chapter.isLiked, 
            chapter.likeCount
        )
        
        if (success) {
            // Refresh the list to show updated like status
            loadChapters()
        }
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

    private fun unlockChapter(chapter: Chapter) {
        // In a real app, you would handle the coin transaction here
        // For demonstration, we'll just open the chapter
        val intent = Intent(this, ComicViewerActivity::class.java)
        intent.putExtra("chapterId", chapter.id)
        intent.putExtra("comicId", comicId)
        startActivity(intent)
    }
}