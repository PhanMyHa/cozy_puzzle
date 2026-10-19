package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.local.dao.PlayerDao
import com.example.cozypuzzle.data.local.entity.PlayerEntity
import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.repository.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun observePlayer(): Flow<Player> =
        playerDao.observeById(PlayerEntity.DEFAULT_PLAYER_ID)
            .map { entity -> entity?.toDomain() ?: Player.DEFAULT }

    override suspend fun updatePlayerName(name: String) {
        val current = playerDao.getById(PlayerEntity.DEFAULT_PLAYER_ID)
            ?: defaultPlayerEntity()
        playerDao.upsert(current.copy(name = name))
    }

    override suspend fun updateAvatarUri(avatarUri: String?) {
        val current = playerDao.getById(PlayerEntity.DEFAULT_PLAYER_ID)
            ?: defaultPlayerEntity()
        playerDao.upsert(current.copy(avatarUri = avatarUri))
    }

    override suspend fun addCoins(amount: Int) {
        val current = playerDao.getById(PlayerEntity.DEFAULT_PLAYER_ID)
            ?: defaultPlayerEntity()
        playerDao.upsert(current.copy(coins = (current.coins + amount).coerceAtLeast(0)))
    }

    private fun PlayerEntity.toDomain(): Player = Player(
        id = id,
        name = name,
        avatarId = avatarId,
        avatarUri = avatarUri,
        level = level,
        coins = coins,
        gems = gems
    )

    private fun defaultPlayerEntity(): PlayerEntity = PlayerEntity(
        name = Player.DEFAULT.name,
        avatarId = Player.DEFAULT.avatarId,
        level = Player.DEFAULT.level,
        coins = Player.DEFAULT.coins,
        gems = Player.DEFAULT.gems
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerRepositoryModule {

    @Binds
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository
}
