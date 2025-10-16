package com.example.pgm.view.Comic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgm.R

class ComicPageAdapter(private val pageUrls: List<String>) :
    RecyclerView.Adapter<ComicPageAdapter.PageViewHolder>() {

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pageImage: ImageView = itemView.findViewById(R.id.pageImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageUrl = pageUrls[position]

        Glide.with(holder.itemView.context)
            .load(pageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .fitCenter()
            .into(holder.pageImage)
    }

    override fun getItemCount(): Int = pageUrls.size
}