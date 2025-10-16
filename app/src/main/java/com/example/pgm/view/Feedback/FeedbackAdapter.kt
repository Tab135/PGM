package com.example.pgm.view.Feedback

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.Feedback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.pgm.model.User
class FeedbackAdapter(
    private val feedbackList: List<Feedback>,
    private val userMap: Map<Int, User>,
    private val currentUserId: Int,
    private val onLikeClick: (Feedback) -> Unit,
    private val onEditClick: (Feedback) -> Unit,
    private val onDeleteClick: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val categoriesChipGroup: ChipGroup = itemView.findViewById(R.id.categoriesChipGroup)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val editButton: ImageView = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val editedBadge: TextView = itemView.findViewById(R.id.editedBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        val user = userMap[feedback.userId]

        // Set user name
        if (feedback.isAnonymous) {
            holder.userNameText.text = "Anonymous User"
        } else {
            holder.userNameText.text = user?.name ?: "Unknown User"
        }

        // Set rating
        holder.ratingBar.rating = feedback.rating
        holder.ratingText.text = String.format("%.1f", feedback.rating)

        // Set comment
        holder.commentText.text = feedback.comment

        // Set categories
        holder.categoriesChipGroup.removeAllViews()
        feedback.categories.forEach { category ->
            val chip = Chip(holder.itemView.context).apply {
                text = category
                isClickable = false
                isCheckable = false
            }
            holder.categoriesChipGroup.addView(chip)
        }

        // Set date
        holder.dateText.text = formatDate(feedback.createdAt)

        // Show edited badge if edited
        if (feedback.isEdited) {
            holder.editedBadge.visibility = View.VISIBLE
        } else {
            holder.editedBadge.visibility = View.GONE
        }

        // Set likes
        holder.likeCount.text = feedback.likes.toString()

        // Show edit/delete buttons only for user's own feedback
        if (feedback.userId == currentUserId) {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
        } else {
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
        }

        // Set click listeners
        holder.likeButton.setOnClickListener { onLikeClick(feedback) }
        holder.editButton.setOnClickListener { onEditClick(feedback) }
        holder.deleteButton.setOnClickListener { onDeleteClick(feedback) }
    }

    override fun getItemCount(): Int = feedbackList.size

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}