package com.example.cozypuzzle.domain.model

data class Achievement(
    val id: String,
    val isUnlocked: Boolean,
    val unlockedAtEpochDay: Long? = null
)
