package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banned_devices")
data class BannedDeviceEntity(
    @PrimaryKey val deviceFingerprint: String,
    val reason: String,
    val bannedBy: String = "SYSTEM_FRAUD_GUARD",
    val bannedAt: Long = System.currentTimeMillis()
)
