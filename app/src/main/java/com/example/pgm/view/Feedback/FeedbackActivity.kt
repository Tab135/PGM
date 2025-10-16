package com.example.pgm.view.Feedback

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.Controller.FeedbackController
import com.example.pgm.Controller.UserComicHistoryController
import com.example.pgm.R
import com.example.pgm.model.Database.UserDatabaseHelper
import com.example.pgm.model.Feedback
import com.example.pgm.model.FeedbackCategories
import com.example.pgm.view.Feedback.FeedbackAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.pgm.Controller.ComicController
import com.example.pgm.utils.SessionManager
class FeedbackActivity : AppCompatActivity() {

    private lateinit var feedbackController: FeedbackController
    private lateinit var comicController: ComicController
    private lateinit var userDatabaseHelper: UserDatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var userComicHistoryController: UserComicHistoryController

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter
    private lateinit var addFeedbackButton: FloatingActionButton
    private lateinit var backButton: ImageView
    private lateinit var comicTitleText: TextView
    private lateinit var averageRatingText: TextView
    private lateinit var totalFeedbackText: TextView
    private lateinit var noFeedbackLayout: LinearLayout
    private lateinit var feedbackListLayout: LinearLayout

    private var comicId: Int = -1
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        comicId = intent.getIntExtra("comicId", -1)
        if (comicId == -1) {
            finish()
            return
        }

        initViews()
        setupControllers()
        loadUserSession()
        loadComicInfo()
        loadFeedback()
        setupClickListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.feedbackRecyclerView)
        addFeedbackButton = findViewById(R.id.addFeedbackButton)
        backButton = findViewById(R.id.backButton)
        comicTitleText = findViewById(R.id.comicTitleText)
        averageRatingText = findViewById(R.id.averageRatingText)
        totalFeedbackText = findViewById(R.id.totalFeedbackText)
        noFeedbackLayout = findViewById(R.id.noFeedbackLayout)
        feedbackListLayout = findViewById(R.id.feedbackListLayout)
    }

    private fun setupControllers() {
        feedbackController = FeedbackController(this)
        comicController = ComicController(this)
        userDatabaseHelper = UserDatabaseHelper(this)
        sessionManager = SessionManager(this)
        userComicHistoryController = UserComicHistoryController(this)
    }

    private fun loadUserSession() {
        currentUserId = sessionManager.getUserId()
    }

    private fun loadComicInfo() {
        val comic = comicController.getComicById(comicId)
        comicTitleText.text = comic?.title ?: "Comic Feedback"
    }

    private fun loadFeedback() {
        val feedbackList = feedbackController.getAllFeedbackForComic(comicId)
        val averageRating = feedbackController.getAverageRating(comicId)
        val feedbackCount = feedbackController.getFeedbackCount(comicId)

        averageRatingText.text = String.format("%.1f", averageRating)
        totalFeedbackText.text = "$feedbackCount Reviews"

        if (feedbackList.isEmpty()) {
            noFeedbackLayout.visibility = View.VISIBLE
            feedbackListLayout.visibility = View.GONE
        } else {
            noFeedbackLayout.visibility = View.GONE
            feedbackListLayout.visibility = View.VISIBLE

            // Get user info for all feedback
            val userMap = feedbackList
                .map { it.userId }
                .distinct()
                .mapNotNull { userId ->
                    userDatabaseHelper.getUserById(userId)?.let { user ->
                        userId to user
                    }
                }
                .toMap()

            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = FeedbackAdapter(
                feedbackList = feedbackList,
                userMap = userMap,
                currentUserId = currentUserId,
                onLikeClick = { feedback -> likeFeedback(feedback) },
                onEditClick = { feedback -> showEditFeedbackDialog(feedback) },
                onDeleteClick = { feedback -> showDeleteConfirmation(feedback) }
            )
            recyclerView.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        addFeedbackButton.setOnClickListener {
            if (currentUserId == -1) {
                Toast.makeText(this, "Please log in to give feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if user is eligible
            if (!feedbackController.isUserEligibleToGiveFeedback(currentUserId, comicId)) {
                Toast.makeText(
                    this,
                    "You need to read at least one chapter before giving feedback",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Check if user already gave feedback
            val existingFeedback = feedbackController.getUserFeedbackForComic(currentUserId, comicId)
            if (existingFeedback != null) {
                showEditFeedbackDialog(existingFeedback)
            } else {
                showAddFeedbackDialog()
            }
        }
    }

    private fun showAddFeedbackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_feedback, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)
        val categoriesChipGroup = dialogView.findViewById<ChipGroup>(R.id.categoriesChipGroup)
        val anonymousCheckbox = dialogView.findViewById<CheckBox>(R.id.anonymousCheckbox)

        // Add category chips
        FeedbackCategories.ALL_CATEGORIES.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
            }
            categoriesChipGroup.addView(chip)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Feedback")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating
                val comment = commentEditText.text.toString().trim()
                val selectedCategories = categoriesChipGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toList()
                val isAnonymous = anonymousCheckbox.isChecked

                if (rating == 0f) {
                    Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (comment.isEmpty()) {
                    Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = feedbackController.addFeedback(
                    userId = currentUserId,
                    comicId = comicId,
                    rating = rating,
                    comment = comment,
                    categories = selectedCategories,
                    isAnonymous = isAnonymous
                )

                if (success) {
                    Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                    loadFeedback() // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showEditFeedbackDialog(feedback: Feedback) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_feedback, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)
        val categoriesChipGroup = dialogView.findViewById<ChipGroup>(R.id.categoriesChipGroup)
        val anonymousCheckbox = dialogView.findViewById<CheckBox>(R.id.anonymousCheckbox)

        // Set existing values
        ratingBar.rating = feedback.rating
        commentEditText.setText(feedback.comment)
        anonymousCheckbox.isChecked = feedback.isAnonymous

        // Add category chips
        FeedbackCategories.ALL_CATEGORIES.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = feedback.categories.contains(category)
            }
            categoriesChipGroup.addView(chip)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Feedback")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val rating = ratingBar.rating
                val comment = commentEditText.text.toString().trim()
                val selectedCategories = categoriesChipGroup.children
                    .filterIsInstance<Chip>()
                    .filter { it.isChecked }
                    .map { it.text.toString() }
                    .toList()
                val isAnonymous = anonymousCheckbox.isChecked

                if (rating == 0f) {
                    Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (comment.isEmpty()) {
                    Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = feedbackController.updateFeedback(
                    feedbackId = feedback.id,
                    userId = currentUserId,
                    comicId = comicId,
                    rating = rating,
                    comment = comment,
                    categories = selectedCategories,
                    isAnonymous = isAnonymous
                )

                if (success) {
                    Toast.makeText(this, "Feedback updated successfully!", Toast.LENGTH_SHORT).show()
                    loadFeedback() // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to update feedback", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmation(feedback: Feedback) {
        AlertDialog.Builder(this)
            .setTitle("Delete Feedback")
            .setMessage("Are you sure you want to delete your feedback?")
            .setPositiveButton("Delete") { _, _ ->
                val success = feedbackController.deleteFeedback(feedback.id)
                if (success) {
                    Toast.makeText(this, "Feedback deleted", Toast.LENGTH_SHORT).show()
                    loadFeedback() // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to delete feedback", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun likeFeedback(feedback: Feedback) {
        feedbackController.likeFeedback(feedback.id)
        loadFeedback() // Refresh to show updated likes
    }

    override fun onResume() {
        super.onResume()
        loadFeedback() // Refresh when returning to this activity
    }
}