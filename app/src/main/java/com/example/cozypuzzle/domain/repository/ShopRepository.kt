package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.ShopItem

/**
 * Access to the purchasable item catalog. Backed by bundled data today;
 * Firebase Remote Config can serve it later without touching gameplay code.
 */
interface ShopRepository {

    suspend fun getShopItems(): List<ShopItem>
}
