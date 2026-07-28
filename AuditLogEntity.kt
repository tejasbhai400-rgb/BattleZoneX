package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String = "",
    val action: String, // e.g., "LOGIN", "PHONE_VERIFIED", "DEPOSIT_SUBMITTED", "WITHDRAWAL_REQUESTED", "ADMIN_APPROVED_DEPOSIT", "BAN_APPLIED", "INTEGRITY_TAMPER_FLAG"
    val details: String,
    val severity: String = "INFO", // "INFO", "WARNING", "CRITICAL"
    val deviceFingerprint: String = "",
    val ipAddress: String = "127.0.0.1",
    val riskScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
