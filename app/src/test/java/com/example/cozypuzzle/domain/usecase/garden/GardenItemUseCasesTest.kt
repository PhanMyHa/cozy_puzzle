package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GardenItemUseCasesTest {

    private class FakeGardenRepository : GardenRepository {

        val items = mutableListOf<GardenItem>()

        override fun observeGardenItems() = kotlinx.coroutines.flow.flow { emit(items.toList()) }

        override suspend fun upsertItem(item: GardenItem) {
            items.removeAll { it.id == item.id }
            items.add(item)
        }

        override suspend fun removeItem(itemId: String) {
            items.removeAll { it.id == itemId }
        }
    }

    @Test
    fun `placing an item stores it near the centre`() = runTest {
        val repository = FakeGardenRepository()
        val useCase = PlaceGardenItemUseCase(repository)

        useCase("flower_tulip", emptyList())

        assertEquals(1, repository.items.size)
        val placed = repository.items.single()
        assertEquals("flower_tulip", placed.itemType)
        assertTrue(placed.x in 0f..1f)
        assertTrue(placed.y in 0f..1f)
    }

    @Test
    fun `placing many items avoids occupied spots`() = runTest {
        val repository = FakeGardenRepository()
        val useCase = PlaceGardenItemUseCase(repository)

        repeat(8) { index -> useCase("tree_$index", repository.items) }

        val positions = repository.items.map { it.x to it.y }
        assertEquals(8, positions.distinct().size)
    }

    @Test
    fun `moving updates position within bounds`() = runTest {
        val repository = FakeGardenRepository()
        repository.items.add(GardenItem("a", "rock", 0.2f, 0.2f, 0f, 1f))
        MoveGardenItemUseCase(repository)("a", 1.7f, -0.5f, repository.items)

        val moved = repository.items.single()
        assertEquals(1f, moved.x, 0.001f)
        assertEquals(0f, moved.y, 0.001f)
    }

    @Test
    fun `rotating normalizes negative angles`() = runTest {
        val repository = FakeGardenRepository()
        repository.items.add(GardenItem("a", "house", 0.5f, 0.5f, 0f, 1f))

        RotateGardenItemUseCase(repository)("a", -90f, repository.items)

        assertEquals(270f, repository.items.single().rotation, 0.001f)
    }

    @Test
    fun `removing deletes the item`() = runTest {
        val repository = FakeGardenRepository()
        repository.items.add(GardenItem("a", "bush", 0.3f, 0.6f, 0f, 1f))

        RemoveGardenItemUseCase(repository)("a")

        assertNull(repository.items.firstOrNull { it.id == "a" })
    }
}
