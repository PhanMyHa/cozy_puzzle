package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.local.dao.InventoryDao
import com.example.cozypuzzle.data.local.entity.InventoryItemEntity
import com.example.cozypuzzle.domain.repository.InventoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val clock: () -> Long
) : InventoryRepository {

    override fun observeOwnedItemIds(): Flow<Set<String>> =
        inventoryDao.observeAll().map { items -> items.mapTo(mutableSetOf()) { it.itemId } }

    override suspend fun addItem(itemId: String) {
        inventoryDao.insert(
            InventoryItemEntity(
                itemId = itemId,
                itemType = itemId.substringBefore(ITEM_ID_SEPARATOR),
                acquiredAtEpochMs = clock()
            )
        )
    }

    private companion object {
        const val ITEM_ID_SEPARATOR = '_'
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class InventoryRepositoryModule {

    @Binds
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
}
