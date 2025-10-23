package com.example.pgm.view.Payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pgm.R
import com.example.pgm.model.TokenPackage

class TokenPackageAdapter(
    private val packages: List<TokenPackage>,
    private val onPackageClick: (TokenPackage) -> Unit
) : RecyclerView.Adapter<TokenPackageAdapter.TokenPackageViewHolder>() {

    inner class TokenPackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packageCard: CardView = itemView.findViewById(R.id.packageCard)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
        val tokenAmount: TextView = itemView.findViewById(R.id.tokenAmount)
        val packagePrice: TextView = itemView.findViewById(R.id.packagePrice)
        val packageDescription: TextView = itemView.findViewById(R.id.packageDescription)
        val popularBadge: TextView = itemView.findViewById(R.id.popularBadge)
        val bonusLabel: TextView = itemView.findViewById(R.id.bonusLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenPackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_token_package, parent, false)
        return TokenPackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenPackageViewHolder, position: Int) {
        val tokenPackage = packages[position]

        holder.packageName.text = tokenPackage.name
        holder.tokenAmount.text = "🪙 ${tokenPackage.tokens + tokenPackage.bonus}"
        holder.packagePrice.text = "$${String.format("%.2f", tokenPackage.price)}"
        holder.packageDescription.text = tokenPackage.description

        // Show popular badge
        if (tokenPackage.isPopular) {
            holder.popularBadge.visibility = View.VISIBLE
        } else {
            holder.popularBadge.visibility = View.GONE
        }

        // Show bonus label
        if (tokenPackage.bonus > 0) {
            holder.bonusLabel.visibility = View.VISIBLE
            holder.bonusLabel.text = "+${tokenPackage.bonus} BONUS"
        } else {
            holder.bonusLabel.visibility = View.GONE
        }

        // Highlight popular package
        if (tokenPackage.isPopular) {
            holder.packageCard.setCardBackgroundColor(
                holder.itemView.context.getColor(android.R.color.holo_blue_light)
            )
        } else {
            holder.packageCard.setCardBackgroundColor(
                holder.itemView.context.getColor(android.R.color.white)
            )
        }

        holder.packageCard.setOnClickListener {
            onPackageClick(tokenPackage)
        }
    }

    override fun getItemCount() = packages.size
}