package com.example.pgm.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import org.json.JSONArray

class AddEditChapterActivity : AppCompatActivity() {
    private lateinit var chapterNumberInput: EditText
    private lateinit var chapterTitleInput: EditText
    private lateinit var thumbnailUrlInput: EditText
    private lateinit var ivThumbnailPreview: ImageView
    private lateinit var btnPreviewThumbnail: Button
    private lateinit var releaseDateInput: EditText
    private lateinit var pdfUrlInput: EditText
    private lateinit var isLockedCheckbox: CheckBox
    private lateinit var costInput: EditText
    private lateinit var freeDaysInput: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var chapterController: ChapterController

    private var comicId: Int = -1
    private var chapterId: Int = -1
    private var mode: String = "add"
    private var currentChapter: Chapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_chapter)

        comicId = intent.getIntExtra("comicId", -1)
        chapterId = intent.getIntExtra("chapterId", -1)
        mode = intent.getStringExtra("mode") ?: "add"

        if (comicId == -1) {
            Toast.makeText(this, "Invalid comic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupController()

        if (mode == "edit" && chapterId != -1) {
            loadChapterData()
        }

        setupClickListeners()
    }

    private fun initViews() {
        chapterNumberInput = findViewById(R.id.chapterNumberInput)
        chapterTitleInput = findViewById(R.id.chapterTitleInput)
        thumbnailUrlInput = findViewById(R.id.thumbnailUrlInput)
        ivThumbnailPreview = findViewById(R.id.ivThumbnailPreview)
        btnPreviewThumbnail = findViewById(R.id.btnPreviewThumbnail)
        releaseDateInput = findViewById(R.id.releaseDateInput)
        pdfUrlInput = findViewById(R.id.pdfUrlInput)
        isLockedCheckbox = findViewById(R.id.isLockedCheckbox)
        costInput = findViewById(R.id.costInput)
        freeDaysInput = findViewById(R.id.freeDaysInput)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupController() {
        chapterController = ChapterController(this)
    }

    private fun loadChapterData() {
        currentChapter = chapterController.getChapterById(chapterId)

        currentChapter?.let { chapter ->
            chapterNumberInput.setText(chapter.chapterNumber.toString())
            chapterTitleInput.setText(chapter.title)
            thumbnailUrlInput.setText(chapter.thumbnailUrl ?: "")
            releaseDateInput.setText(chapter.releaseDate ?: "")
            isLockedCheckbox.isChecked = chapter.isLocked
            costInput.setText(chapter.cost.toString())
            freeDaysInput.setText(chapter.freeDays.toString())

            // Load PDF URL - đọc trực tiếp từ remoteUrl (đã là string thuần)
            pdfUrlInput.setText(chapter.remoteUrl ?: "")

            // Preview thumbnail
            chapter.thumbnailUrl?.let { url ->
                loadThumbnailPreview(url)
            }
        }
    }

    private fun setupClickListeners() {
        btnPreviewThumbnail.setOnClickListener {
            val url = thumbnailUrlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                loadThumbnailPreview(url)
            } else {
                Toast.makeText(this, "Please enter thumbnail URL first", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            saveChapter()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }

        // Toggle cost and free days inputs based on locked status
        isLockedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            costInput.isEnabled = isChecked
            freeDaysInput.isEnabled = isChecked
        }
    }

    private fun loadThumbnailPreview(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_comic_placeholder)
            .error(R.drawable.ic_comic_placeholder)
            .into(ivThumbnailPreview)
    }

    private fun saveChapter() {
        if (!validateInputs()) {
            return
        }

        val chapterNumber = chapterNumberInput.text.toString().toInt()
        val title = chapterTitleInput.text.toString()
        val thumbnailUrl = thumbnailUrlInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val releaseDate = releaseDateInput.text.toString().takeIf { it.isNotEmpty() }
        val pdfUrl = pdfUrlInput.text.toString().trim() // ← LƯU TRỰC TIẾP STRING, KHÔNG DÙNG JSON
        val isLocked = isLockedCheckbox.isChecked
        val cost = if (isLocked) costInput.text.toString().toIntOrNull() ?: 0 else 0
        val freeDays = if (isLocked) freeDaysInput.text.toString().toIntOrNull() ?: 0 else 0

        val chapter = Chapter(
            id = if (mode == "edit") chapterId else 0,
            comicId = comicId,
            chapterNumber = chapterNumber,
            title = title,
            thumbnailUrl = thumbnailUrl,
            releaseDate = releaseDate,
            likeCount = currentChapter?.likeCount ?: 0,
            isLocked = isLocked,
            cost = cost,
            freeDays = freeDays,
            pages = null,
            localPath = null,
            remoteUrl = pdfUrl  // ← LƯU TRỰC TIẾP STRING
        )

        val success = if (mode == "add") {
            chapterController.addChapter(chapter)
        } else {
            chapterController.updateChapter(chapter)
        }

        if (success) {
            Toast.makeText(
                this,
                if (mode == "add") "Chapter added successfully" else "Chapter updated successfully",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            Toast.makeText(
                this,
                if (mode == "add") "Failed to add chapter" else "Failed to update chapter",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateInputs(): Boolean {
        if (chapterNumberInput.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter chapter number", Toast.LENGTH_SHORT).show()
            return false
        }

        if (chapterTitleInput.text.toString().isEmpty()) {
            Toast.makeText(this, "Please enter chapter title", Toast.LENGTH_SHORT).show()
            return false
        }

        if (pdfUrlInput.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter PDF URL", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}