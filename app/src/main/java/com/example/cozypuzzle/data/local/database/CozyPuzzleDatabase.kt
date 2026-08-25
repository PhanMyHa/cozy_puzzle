package com.example.cozypuzzle.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cozypuzzle.data.local.dao.AchievementDao
import com.example.cozypuzzle.data.local.dao.GardenItemDao
import com.example.cozypuzzle.data.local.dao.InventoryDao
import com.example.cozypuzzle.data.local.dao.PetDao
import com.example.cozypuzzle.data.local.dao.PlayerDao
import com.example.cozypuzzle.data.local.dao.PuzzleResultDao
import com.example.cozypuzzle.data.local.dao.PuzzleSessionDao
import com.example.cozypuzzle.data.local.entity.AchievementEntity
import com.example.cozypuzzle.data.local.entity.GardenItemEntity
import com.example.cozypuzzle.data.local.entity.InventoryItemEntity
import com.example.cozypuzzle.data.local.entity.PetEntity
import com.example.cozypuzzle.data.local.entity.PlayerEntity
import com.example.cozypuzzle.data.local.entity.PuzzleResultEntity
import com.example.cozypuzzle.data.local.entity.PuzzleSessionEntity

@Database(
    entities = [
        PlayerEntity::class,
        PuzzleSessionEntity::class,
        PuzzleResultEntity::class,
        GardenItemEntity::class,
        PetEntity::class,
        InventoryItemEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class CozyPuzzleDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun puzzleSessionDao(): PuzzleSessionDao

    abstract fun puzzleResultDao(): PuzzleResultDao

    abstract fun gardenItemDao(): GardenItemDao

    abstract fun petDao(): PetDao

    abstract fun inventoryDao(): InventoryDao

    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "cozy_puzzle.db"
    }
}
