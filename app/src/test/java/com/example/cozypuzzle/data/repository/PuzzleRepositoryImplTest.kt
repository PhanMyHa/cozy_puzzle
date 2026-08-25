package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.data.local.dao.PuzzleResultDao
import com.example.cozypuzzle.data.local.dao.PuzzleSessionDao
import com.example.cozypuzzle.data.local.entity.PuzzleResultEntity
import com.example.cozypuzzle.data.local.entity.PuzzleSessionEntity
import com.example.cozypuzzle.domain.model.PuzzleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PuzzleRepositoryImplTest {

    @Test
    fun `saved result is persisted as a mapped entity`() = runTest {
        val dao = FakePuzzleResultDao()
        val repository = PuzzleRepositoryImpl(NoOpPuzzleSessionDao(), dao)
        val result = domainResult(durationMs = 42_000L)

        val id = repository.saveResult(result)

        assertEquals(1L, id)
        val stored = dao.inserted.single()
        assertEquals("cute_cat", stored.puzzleId)
        assertEquals(PuzzleMode.BASIC.name, stored.puzzleMode)
        assertEquals(Difficulty.EASY.name, stored.difficulty)
        assertEquals(3, stored.rows)
        assertEquals(3, stored.columns)
        assertEquals(42_000L, stored.durationMs)
        assertEquals(7, stored.moveCount)
        assertEquals(2, stored.hintsUsed)
        assertEquals(100L, stored.completedAtEpochMs)
    }

    @Test
    fun `stored entities are mapped back to domain results`() = runTest {
        val dao = FakePuzzleResultDao()
        val repository = PuzzleRepositoryImpl(NoOpPuzzleSessionDao(), dao)

        repository.saveResult(domainResult())
        repository.saveResult(domainResult(durationMs = 10_000L))

        val results = repository.getResultsForPuzzle("cute_cat")
        assertEquals(2, results.size)
        val first = results.first()
        assertEquals("cute_cat", first.puzzleId)
        assertEquals(PuzzleMode.BASIC, first.mode)
        assertEquals(Difficulty.EASY, first.difficulty)
        assertEquals(42_000L, first.durationMs)
        assertEquals(7, first.moveCount)
        assertEquals(2, first.hintsUsed)
    }

    private fun domainResult(durationMs: Long = 42_000L): PuzzleResult = PuzzleResult(
        puzzleId = "cute_cat",
        mode = PuzzleMode.BASIC,
        difficulty = Difficulty.EASY,
        rows = 3,
        columns = 3,
        durationMs = durationMs,
        moveCount = 7,
        hintsUsed = 2,
        completedAtEpochMs = 100L
    )

    private class NoOpPuzzleSessionDao : PuzzleSessionDao {
        override fun observeAll(): Flow<List<PuzzleSessionEntity>> =
            kotlinx.coroutines.flow.MutableStateFlow(emptyList())

        override suspend fun getById(sessionId: Long): PuzzleSessionEntity? = null

        override suspend fun insert(session: PuzzleSessionEntity): Long = 0L

        override suspend fun update(session: PuzzleSessionEntity) = Unit

        override suspend fun deleteById(sessionId: Long) = Unit
    }

    private class FakePuzzleResultDao : PuzzleResultDao {

        val inserted = mutableListOf<PuzzleResultEntity>()
        private var nextId = 1L

        override fun observeAll(): Flow<List<PuzzleResultEntity>> =
            kotlinx.coroutines.flow.MutableStateFlow(inserted.toList())

        override suspend fun getByPuzzleId(puzzleId: String): List<PuzzleResultEntity> =
            inserted.filter { it.puzzleId == puzzleId }

        override suspend fun insert(result: PuzzleResultEntity): Long {
            val entity = result.copy(id = nextId++)
            inserted.add(entity)
            return entity.id
        }
    }
}
