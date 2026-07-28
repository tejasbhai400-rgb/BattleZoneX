package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val amount: Double,
    val utrNumber: String = "",
    val paymentMethod: String = "UPI / QR",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val timestamp: Long = System.currentTimeMillis(),
    val deviceFingerprint: String = "",
    val riskScore: Int = 0,
    val adminNote: String = "",
    val processedBy: String = ""
)
