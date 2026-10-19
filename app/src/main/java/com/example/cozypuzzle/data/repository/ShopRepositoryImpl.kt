package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.datasource.BundledShopCatalogDataSource
import com.example.cozypuzzle.data.datasource.LocalShopCatalogDataSource
import com.example.cozypuzzle.domain.model.ShopItem
import com.example.cozypuzzle.domain.repository.ShopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val dataSource: LocalShopCatalogDataSource
) : ShopRepository {

    override suspend fun getShopItems(): List<ShopItem> = dataSource.getCatalog()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ShopRepositoryModule {

    @Binds
    abstract fun bindLocalShopCatalogDataSource(
        impl: BundledShopCatalogDataSource
    ): LocalShopCatalogDataSource

    @Binds
    abstract fun bindShopRepository(impl: ShopRepositoryImpl): ShopRepository
}
