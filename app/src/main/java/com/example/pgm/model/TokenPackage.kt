package com.example.pgm.model

data class TokenPackage(
    val id: Int,
    val name: String,
    val tokens: Int,
    val price: Double,
    val bonus: Int = 0,
    val isPopular: Boolean = false,
    val description: String = ""
) {
    fun getTotalTokens(): Int = tokens + bonus
}