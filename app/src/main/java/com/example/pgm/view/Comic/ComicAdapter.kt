package com.example.pgm.view.Comic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.Comic
import com.bumptech.glide.Glide

class ComicAdapter(
    private val comics: List<Comic>,
    private val onClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicAdapter.ComicViewHolder>() {

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.comicTitle)
        val author: TextView = itemView.findViewById(R.id.comicAuthor)
        val image: ImageView = itemView.findViewById(R.id.comicImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comics[position]
        holder.title.text = comic.title
        holder.author.text = comic.author ?: "Unknown"
        comic.imageUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.image)
        }
        holder.itemView.setOnClickListener { onClick(comic) }
    }

    override fun getItemCount(): Int = comics.size
}