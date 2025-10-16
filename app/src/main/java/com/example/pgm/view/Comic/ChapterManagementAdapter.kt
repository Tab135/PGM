package com.example.pgm.view.Comic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.Chapter

class ChapterManagementAdapter (
    private val chapters: List<Chapter>,
    private val onViewClick: (Chapter) -> Unit,      // THÊM callback view
    private val onEditClick: (Chapter) -> Unit,
    private val onDeleteClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterManagementAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterNumber: TextView = itemView.findViewById(R.id.chapterNumber)
        val chapterTitle: TextView = itemView.findViewById(R.id.chapterTitle)
        val chapterInfo: TextView = itemView.findViewById(R.id.chapterInfo)
        val viewButton: ImageButton = itemView.findViewById(R.id.viewButton)      // THÊM nút view
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter_management, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapters[position]

        holder.chapterNumber.text = "Chapter ${chapter.chapterNumber}"
        holder.chapterTitle.text = chapter.title

        // Build info string
        val infoBuilder = StringBuilder()
        infoBuilder.append("Pages: ${chapter.pages ?: 0}")
        if (chapter.isLocked) {
            infoBuilder.append(" • Locked (${chapter.cost} coins)")
        } else {
            infoBuilder.append(" • Free")
        }
        infoBuilder.append(" • Likes: ${chapter.likeCount}")
        holder.chapterInfo.text = infoBuilder.toString()

        holder.viewButton.setOnClickListener {
            onViewClick(chapter)
        }

        holder.editButton.setOnClickListener {
            onEditClick(chapter)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(chapter)
        }
    }

    override fun getItemCount(): Int = chapters.size
}