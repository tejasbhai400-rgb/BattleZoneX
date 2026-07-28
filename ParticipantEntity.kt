package com.example.data

import androidx.room.Entity

@Entity(tableName = "participants", primaryKeys = ["tournamentId", "userId"])
data class ParticipantEntity(
    val tournamentId: String,
    val userId: String,
    val inGameUsername: String,
    val slotNumber: Int,
    val joinedAt: Long = System.currentTimeMillis(),
    val kills: Int = 0,
    val rankWon: Int = 0,
    val prizeWon: Double = 0.0
)
