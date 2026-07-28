package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "user_101",
    val name: String = "Pro Gamer",
    val email: String = "gamer@battlix.gg",
    val phone: String = "+91 9876543210",
    val gameUsername: String = "BattliX_Slayer",
    val freeFireUid: String = "1234567890",
    val referralCode: String = "BTLX7A9K",
    val depositBalance: Double = 50.0,
    val winningBalance: Double = 120.0,
    val totalWinnings: Double = 450.0,
    val totalKills: Int = 38,
    val matchesPlayed: Int = 14,
    val isAdmin: Boolean = true, // Default enabled so user can test admin features easily
    val isBlocked: Boolean = false,
    val lastWithdrawalTime: Long = 0L, // Epoch millis
    val referredBy: String = "",
    val hasClaimedReferralDepositBonus: Boolean = false,
    val isPhoneVerified: Boolean = true,
    val isBanned: Boolean = false,
    val banReason: String = "",
    val bannedAt: Long = 0L,
    val deviceFingerprint: String = "DEV-8F92A1B0-SEC",
    val riskScore: Int = 5,
    val firebaseUid: String = "fb_uid_101",
    val lastLoginIp: String = "192.168.1.100",
    val walletChecksum: String = "CHK_991204"
) {
    val totalBalance: Double
        get() = depositBalance + winningBalance
}

