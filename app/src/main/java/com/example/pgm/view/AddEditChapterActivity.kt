package com.example.pgm.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.example.pgm.model.Chapter
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var freeDateInput: EditText
    private lateinit var freeDateDisplay: TextView
    private lateinit var btnAuto30Days: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var chapterController: ChapterController
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var comicId: Int = -1
    private var chapterId: Int = -1
    private var mode: String = "add"
    private var currentChapter: Chapter? = null
    private var selectedFreeDateMillis: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_chapter)

        comicId = intent.getIntExtra("comicId", -1)
        chapterId = intent.getIntExtra("chapterId", -1)

        // Tự động detect mode dựa vào chapterId
        mode = if (chapterId != -1) "edit" else "add"

        if (comicId == -1) {
            Toast.makeText(this, "Invalid comic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupController()

        // Đơn giản hóa điều kiện
        if (mode == "edit") {
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
        freeDateInput = findViewById(R.id.freeDateInput)
        freeDateDisplay = findViewById(R.id.freeDateDisplay)
        btnAuto30Days = findViewById(R.id.btnAuto30Days)
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
            
            // Load freeDate if available
            chapter.freeDate?.let { millis ->
                selectedFreeDateMillis = millis
                updateFreeDateDisplay(millis)
            }

            // Load PDF URL
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

        // Free date picker
        freeDateInput.setOnClickListener {
            showDatePicker()
        }

        // Auto 30 days button
        btnAuto30Days.setOnClickListener {
            setAuto30Days()
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

        // Toggle cost and free date inputs based on locked status
        isLockedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            costInput.isEnabled = isChecked
            freeDateInput.isEnabled = isChecked
            btnAuto30Days.isEnabled = isChecked
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // If we have a selected date, start from there; otherwise start from today
        selectedFreeDateMillis?.let {
            calendar.timeInMillis = it
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)
                
                val millis = selectedCalendar.timeInMillis
                selectedFreeDateMillis = millis
                updateFreeDateDisplay(millis)
            },
            year,
            month,
            day
        )

        // Only allow future dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun setAuto30Days() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val millis = calendar.timeInMillis
        selectedFreeDateMillis = millis
        updateFreeDateDisplay(millis)
        
        Toast.makeText(this, "Set to 30 days from now", Toast.LENGTH_SHORT).show()
    }

    private fun updateFreeDateDisplay(millis: Long) {
        val dateStr = dateFormat.format(Date(millis))
        freeDateInput.setText(dateStr)
        freeDateDisplay.text = "Selected: $dateStr"
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
        val pdfUrl = pdfUrlInput.text.toString().trim()
        val isLocked = isLockedCheckbox.isChecked
        val cost = if (isLocked) costInput.text.toString().toIntOrNull() ?: 0 else 0
        
        // Use selected freeDate (epoch millis) if locked and date is selected
        val freeDate: Long? = if (isLocked && selectedFreeDateMillis != null) {
            selectedFreeDateMillis
        } else null

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
            freeDays = 0, // deprecated, set to 0
            freeDate = freeDate,
            pages = null,
            localPath = null,
            remoteUrl = pdfUrl
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