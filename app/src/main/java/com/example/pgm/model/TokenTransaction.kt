package com.example.pgm.model

data class TokenTransaction(
    val id: Int = 0,
    val userId: Int,
    val type: TransactionType,
    val amount: Int,
    val price: Double = 0.0,
    val paymentMethod: String = "",
    val packageName: String = "",
    val description: String = "",
    val timestamp: String,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionType {
    PURCHASE,      // Buying tokens
    SPEND,         // Spending tokens on chapters
    BONUS,         // Free tokens (daily bonus, promotions)
    REFUND         // Refunded tokens
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}