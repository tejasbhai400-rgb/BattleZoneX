package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "ENTRY_FEE", "WINNING", "REFERRAL_BONUS"
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
