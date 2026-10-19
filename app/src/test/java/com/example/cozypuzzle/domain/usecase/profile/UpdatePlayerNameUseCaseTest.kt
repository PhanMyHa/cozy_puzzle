package com.example.cozypuzzle.domain.usecase.profile

import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePlayerNameUseCaseTest {

    @Test
    fun `blank names are rejected`() = runTest {
        val repo = FakePlayerRepo()

        assertFalse(UpdatePlayerNameUseCase(repo)("   "))
        assertEquals("", repo.current.name)
    }

    @Test
    fun `names are trimmed before saving`() = runTest {
        val repo = FakePlayerRepo()

        assertTrue(UpdatePlayerNameUseCase(repo)("  Lily  "))
        assertEquals("Lily", repo.current.name)
    }

    @Test
    fun `overly long names are rejected`() = runTest {
        val repo = FakePlayerRepo()
        val useCase = UpdatePlayerNameUseCase(repo)

        assertFalse(useCase("x".repeat(25)))
        assertTrue(useCase("x".repeat(24)))
        assertEquals("x".repeat(24), repo.current.name)
    }
}

private class FakePlayerRepo : PlayerRepository {

    private val flow = MutableStateFlow(Player.DEFAULT)

    val current: Player get() = flow.value

    override fun observePlayer(): Flow<Player> = flow

    override suspend fun updatePlayerName(name: String) {
        flow.value = flow.value.copy(name = name.trim())
    }

    override suspend fun updateAvatarUri(avatarUri: String?) {
        flow.value = flow.value.copy(avatarUri = avatarUri)
    }

    override suspend fun addCoins(amount: Int) = Unit
}
