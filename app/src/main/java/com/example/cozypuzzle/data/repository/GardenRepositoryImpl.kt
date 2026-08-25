package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.local.dao.GardenItemDao
import com.example.cozypuzzle.data.local.entity.GardenItemEntity
import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GardenRepositoryImpl @Inject constructor(
    private val gardenItemDao: GardenItemDao
) : GardenRepository {

    override fun observeGardenItems(): Flow<List<GardenItem>> =
        gardenItemDao.observeAll().map { items -> items.map { it.toDomain() } }

    override suspend fun upsertItem(item: GardenItem) {
        gardenItemDao.upsert(item.toEntity())
    }

    override suspend fun removeItem(itemId: String) {
        gardenItemDao.deleteById(itemId)
    }

    private fun GardenItemEntity.toDomain(): GardenItem = GardenItem(
        id = id,
        itemType = itemType,
        x = x,
        y = y,
        rotation = rotation,
        scale = scale
    )

    private fun GardenItem.toEntity(): GardenItemEntity = GardenItemEntity(
        id = id,
        itemType = itemType,
        x = x,
        y = y,
        rotation = rotation,
        scale = scale
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GardenRepositoryModule {

    @Binds
    abstract fun bindGardenRepository(impl: GardenRepositoryImpl): GardenRepository
}
