package com.example.pgm.view.Admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.Controller.ChapterController
import com.example.pgm.R
import com.example.pgm.model.Comic

class AdminComicAdapter (
    private val comics: List<Comic>,
    private val chapterController: ChapterController,
    private val onEdit: (Comic) -> Unit,
    private val onDelete: (Comic) -> Unit,
    private val onView: (Comic) -> Unit,
    private val onManageChapters: (Comic) -> Unit
) : RecyclerView.Adapter<AdminComicAdapter.AdminComicViewHolder>() {

    class AdminComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivComicImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvComicTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvComicAuthor)
        val tvChapters: TextView = itemView.findViewById(R.id.tvComicChapters)
        val btnView: ImageButton = itemView.findViewById(R.id.btnView)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnManageChapters: Button = itemView.findViewById(R.id.btnManageChapters)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminComicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_comic, parent, false)
        return AdminComicViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminComicViewHolder, position: Int) {
        val comic = comics[position]

        holder.tvTitle.text = comic.title
        holder.tvAuthor.text = "Author: ${comic.author ?: "Unknown"}"

        val chapterCount = chapterController.getChaptersByComicId(comic.id).size
        holder.tvChapters.text = "Chapters: $chapterCount"

        // Load image
        comic.imageUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .placeholder(R.drawable.ic_comic_placeholder)
                .error(R.drawable.ic_comic_placeholder)
                .into(holder.imageView)
        } ?: run {
            holder.imageView.setImageResource(R.drawable.ic_comic_placeholder)
        }

        // Set click listeners
        holder.btnView.setOnClickListener { onView(comic) }
        holder.btnEdit.setOnClickListener { onEdit(comic) }
        holder.btnDelete.setOnClickListener { onDelete(comic) }
        holder.btnManageChapters.setOnClickListener { onManageChapters(comic) }
    }

    override fun getItemCount(): Int = comics.size
}