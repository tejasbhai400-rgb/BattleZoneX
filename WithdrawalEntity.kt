package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val upiId: String,
    val amount: Double,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
