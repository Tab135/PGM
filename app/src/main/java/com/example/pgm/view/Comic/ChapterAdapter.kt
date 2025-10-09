package com.example.pgm.view.Comic

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.R
import com.example.pgm.model.Chapter
import com.example.pgm.model.UserComicHistory

class ChapterAdapter(
    private val chapters: List<Chapter>,
    private val userComicHistory: UserComicHistory? = null,
    private val highestChapterRead: Int = 0,
    private val onChapterClick: (Chapter) -> Unit,
    private val onLikeClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterThumbnail: ImageView = itemView.findViewById(R.id.chapterThumbnail)
        val chapterTitle: TextView = itemView.findViewById(R.id.chapterTitle)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val releaseDate: TextView = itemView.findViewById(R.id.releaseDate)
        val lockStatus: TextView = itemView.findViewById(R.id.lockStatus)
        val chapterCost: TextView = itemView.findViewById(R.id.chapterCost)
        val upIndicator: TextView = itemView.findViewById(R.id.upIndicator)
        val chapterNumber: TextView = itemView.findViewById(R.id.chapterNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]

        // Set basic info
        holder.chapterTitle.text = "Ep. ${chapter.chapterNumber} - ${chapter.title}"
        holder.likeCount.text = chapter.likeCount.toString()
        holder.releaseDate.text = chapter.releaseDate ?: ""
        holder.chapterNumber.text = "#${chapter.chapterNumber}"

        // Load thumbnail
        chapter.thumbnailUrl?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.chapterThumbnail)
        }

        // Handle like status - check both chapter and user history
        val isLiked = userComicHistory?.likedChapters?.contains(chapter.id) ?: false

        if (isLiked) {
            holder.likeIcon.setImageResource(android.R.drawable.btn_star_big_on)
            holder.likeIcon.setColorFilter(Color.YELLOW)
        } else {
            holder.likeIcon.setImageResource(android.R.drawable.btn_star_big_off)
            holder.likeIcon.setColorFilter(Color.GRAY)
        }

        // Handle lock status and cost
        if (chapter.isLocked) {
            holder.chapterCost.visibility = View.VISIBLE
            holder.chapterCost.text = "${chapter.cost} Coins"
            holder.chapterCost.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_green_dark
                )
            )

            if (chapter.freeDays > 0) {
                holder.lockStatus.visibility = View.VISIBLE
                holder.lockStatus.text = "Free in ${chapter.freeDays} Day(s)"
            } else {
                holder.lockStatus.visibility = View.GONE
            }
        } else {
            holder.chapterCost.visibility = View.GONE
            holder.lockStatus.visibility = View.GONE
        }

        // Show UP indicator for latest chapters, current reading chapter, or special status
        val isLatestViewed = userComicHistory?.latestViewedChapter == chapter.id
        val isRead = userComicHistory?.viewedChapters?.contains(chapter.id) ?: false
        val isNewChapter = chapter.chapterNumber > highestChapterRead

        if (isLatestViewed) {
            holder.upIndicator.visibility = View.VISIBLE
            holder.upIndicator.text = "READING"
            holder.upIndicator.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_blue_bright
                )
            )
        } else if (isNewChapter) {
            holder.upIndicator.visibility = View.VISIBLE
            holder.upIndicator.text = "NEW"
            holder.upIndicator.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_green_light
                )
            )
        } else {
            holder.upIndicator.visibility = View.GONE
        }

        if (isRead) {
            holder.itemView.alpha = 0.6f
            holder.chapterTitle.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.darker_gray
                )
            )
        } else {
            holder.itemView.alpha = 1.0f
            holder.chapterTitle.setTextColor(Color.WHITE)
        }

        // Highlight bookmarked chapters
        val isBookmarked = userComicHistory?.bookmarkedChapters?.contains(chapter.id) ?: false
        if (isBookmarked) {
            // Use a subtle background to indicate bookmark
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.darker_gray
                )
            )
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Set click listeners
        holder.itemView.setOnClickListener { onChapterClick(chapter) }
        holder.likeIcon.setOnClickListener { onLikeClick(chapter) }

        // Long click to bookmark (if needed)
        holder.itemView.setOnLongClickListener {
            // You could add bookmark functionality here
            true
        }
    }

    override fun getItemCount(): Int = chapters.size
}