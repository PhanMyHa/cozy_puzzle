package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val itemId: String,
    val itemType: String,
    val acquiredAtEpochMs: Long
)
