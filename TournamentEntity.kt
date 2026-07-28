package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val gameType: String, // "BGMI", "Free Fire", "Call of Duty", "Valorant"
    val map: String,
    val matchType: String, // "Solo", "Duo", "Squad"
    val entryFee: Double,
    val prizePool: Double,
    val perKill: Double,
    val startTime: Long, // epoch millis
    val totalSlots: Int,
    val joinedSlots: Int,
    val status: String, // "UPCOMING", "ONGOING", "COMPLETED"
    val roomId: String = "",
    val roomPassword: String = "",
    val bannerImageResName: String = "img_battlix_banner_1785044702370",
    val rules: String = "1. Hacking or cheating will result in an instant ban.\n2. Emulators are strictly prohibited unless specified.\n3. Room ID & Pass will be visible 5 minutes before match start."
) {
    fun isRoomUnlocked(currentTime: Long = System.currentTimeMillis()): Boolean {
        // Unlocks 5 minutes (300,000 ms) before start time or after start time
        val fiveMinsBefore = startTime - (5 * 60 * 1000)
        return currentTime >= fiveMinsBefore && roomId.isNotEmpty()
    }

    fun remainingTimeForRoomUnlock(currentTime: Long = System.currentTimeMillis()): Long {
        val fiveMinsBefore = startTime - (5 * 60 * 1000)
        val remaining = fiveMinsBefore - currentTime
        return if (remaining > 0) remaining else 0
    }
}
