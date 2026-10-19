package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.ShopItem

/** Source of the purchasable catalog; swappable for Firebase later. */
interface LocalShopCatalogDataSource {

    suspend fun getCatalog(): List<ShopItem>
}
