package com.example.pgm.model

data class Feedback(
    val id: Int = 0,
    val userId: Int,
    val comicId: Int,
    val rating: Float, // 1.0 to 5.0
    val comment: String,
    val categories: List<String> = emptyList(), // e.g., ["Story", "Art", "Characters"]
    val isAnonymous: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null,
    val likes: Int = 0,
    val isEdited: Boolean = false
)

data class FeedbackCategory(
    val name: String,
    val isSelected: Boolean = false
)

object FeedbackCategories {
    val ALL_CATEGORIES = listOf(
        "Amazing Story",
        "Beautiful Art",
        "Great Characters",
        "Emotional",
        "Funny",
        "Romantic",
        "Thrilling",
        "Well Paced",
        "Original",
        "Inspiring"
    )
}