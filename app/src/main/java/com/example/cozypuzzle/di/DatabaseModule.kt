package com.example.cozypuzzle.di

import android.content.Context
import androidx.room.Room
import com.example.cozypuzzle.data.local.dao.AchievementDao
import com.example.cozypuzzle.data.local.dao.GardenItemDao
import com.example.cozypuzzle.data.local.dao.InventoryDao
import com.example.cozypuzzle.data.local.dao.PetDao
import com.example.cozypuzzle.data.local.dao.PlayerDao
import com.example.cozypuzzle.data.local.dao.PuzzleResultDao
import com.example.cozypuzzle.data.local.dao.PuzzleSessionDao
import com.example.cozypuzzle.data.local.database.CozyPuzzleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CozyPuzzleDatabase =
        Room.databaseBuilder(
            context,
            CozyPuzzleDatabase::class.java,
            CozyPuzzleDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun providePlayerDao(database: CozyPuzzleDatabase): PlayerDao = database.playerDao()

    @Provides
    fun providePuzzleSessionDao(database: CozyPuzzleDatabase): PuzzleSessionDao =
        database.puzzleSessionDao()

    @Provides
    fun providePuzzleResultDao(database: CozyPuzzleDatabase): PuzzleResultDao =
        database.puzzleResultDao()

    @Provides
    fun provideGardenItemDao(database: CozyPuzzleDatabase): GardenItemDao =
        database.gardenItemDao()

    @Provides
    fun providePetDao(database: CozyPuzzleDatabase): PetDao = database.petDao()

    @Provides
    fun provideInventoryDao(database: CozyPuzzleDatabase): InventoryDao = database.inventoryDao()

    @Provides
    fun provideAchievementDao(database: CozyPuzzleDatabase): AchievementDao =
        database.achievementDao()
}
