package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean,
    val unlockedAtEpochDay: Long?
)
