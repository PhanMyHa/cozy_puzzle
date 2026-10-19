package com.example.cozypuzzle.core.findpair

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.findpair.engine.FindPairEngine
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.findpair.model.FindPairLevel
import com.example.cozypuzzle.core.findpair.model.FlipResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FindPairEngineTest {

    private lateinit var engine: FindPairEngine
    private lateinit var sampleLevel: FindPairLevel

    @Before
    fun setUp() {
        engine = FindPairEngine()
        sampleLevel = FindPairLevel(
            id = "test_pair_1",
            number = 1,
            title = "Level 1",
            difficulty = FindPairDifficulty.EASY,
            columns = 3,
            rows = 4,
            timeLimitSeconds = 120,
            threeStarMaxFlips = 16,
            twoStarMaxFlips = 24,
            images = listOf(
                R.drawable.img_puzzle_cute_cat,
                R.drawable.img_puzzle_cozy_room,
                R.drawable.img_puzzle_little_garden,
                R.drawable.img_puzzle_animals,
                R.drawable.img_puzzle_picnic,
                R.drawable.img_puzzle_nature
            )
        )
    }

    @Test
    fun `initial state has correct cards count and pairs`() {
        val state = engine.createInitialState(sampleLevel, seed = 42L)
        assertEquals(12, state.cards.size)
        assertEquals(6, state.totalPairs)
        assertEquals(0, state.flipsCount)
        assertEquals(0, state.matchedPairCount)
        assertEquals(120, state.remainingSeconds)
        assertFalse(state.isCompleted)
        assertFalse(state.isGameOver)
        assertFalse(state.isPaused)

        // Each pairId 0..5 appears exactly twice
        for (pairId in 0 until 6) {
            val matchingCards = state.cards.filter { it.pairId == pairId }
            assertEquals(2, matchingCards.size)
        }

        // All cards face down and unmatched
        assertTrue(state.cards.all { !it.isFaceUp && !it.isMatched })
    }

    @Test
    fun `flipping first card reveals it and increments flips`() {
        val state = engine.createInitialState(sampleLevel, seed = 42L)
        val (nextState, result) = engine.flipCard(state, 0)

        assertNotNull(result)
        assertTrue(result is FlipResult.CardRevealed)
        assertEquals(0, (result as FlipResult.CardRevealed).cardId)
        assertEquals(1, nextState.flipsCount)
        assertEquals(listOf(0), nextState.revealedCardIds)
        assertTrue(nextState.cards[0].isFaceUp)
        assertFalse(nextState.cards[0].isMatched)
    }

    @Test
    fun `flipping second matching card completes the pair`() {
        val state = engine.createInitialState(sampleLevel, seed = 42L)
        val card0 = state.cards[0]
        val matchingCard = state.cards.first { it.id != 0 && it.pairId == card0.pairId }

        val (state1, _) = engine.flipCard(state, 0)
        val (state2, result) = engine.flipCard(state1, matchingCard.id)

        assertNotNull(result)
        assertTrue(result is FlipResult.CardsMatched)
        val matchResult = result as FlipResult.CardsMatched
        assertEquals(0, matchResult.firstCardId)
        assertEquals(matchingCard.id, matchResult.secondCardId)

        assertEquals(2, state2.flipsCount)
        assertEquals(1, state2.matchedPairCount)
        assertTrue(state2.revealedCardIds.isEmpty())
        assertTrue(state2.cards[0].isMatched)
        assertTrue(state2.cards[matchingCard.id].isMatched)
    }

    @Test
    fun `flipping second non-matching card triggers mismatch resolution state`() {
        val state = engine.createInitialState(sampleLevel, seed = 42L)
        val card0 = state.cards[0]
        val nonMatchingCard = state.cards.first { it.id != 0 && it.pairId != card0.pairId }

        val (state1, _) = engine.flipCard(state, 0)
        val (state2, result) = engine.flipCard(state1, nonMatchingCard.id)

        assertNotNull(result)
        assertTrue(result is FlipResult.CardsMismatched)
        val mismatch = result as FlipResult.CardsMismatched
        assertEquals(0, mismatch.firstCardId)
        assertEquals(nonMatchingCard.id, mismatch.secondCardId)

        assertEquals(2, state2.flipsCount)
        assertEquals(0, state2.matchedPairCount)
        assertTrue(state2.isResolvingMismatch)
        assertEquals(2, state2.revealedCardIds.size)

        // Cannot flip any other card while resolving mismatch
        assertFalse(engine.canFlipCard(state2, 2))

        // Resolve mismatch flips cards back down
        val resolvedState = engine.resolveMismatch(state2)
        assertFalse(resolvedState.isResolvingMismatch)
        assertTrue(resolvedState.revealedCardIds.isEmpty())
        assertFalse(resolvedState.cards[0].isFaceUp)
        assertFalse(resolvedState.cards[nonMatchingCard.id].isFaceUp)
    }

    @Test
    fun `matching all pairs completes the level and awards stars`() {
        var state = engine.createInitialState(sampleLevel, seed = 42L)

        // Match all 6 pairs sequentially
        for (pairId in 0 until 6) {
            val pairCards = state.cards.filter { it.pairId == pairId }
            val (s1, _) = engine.flipCard(state, pairCards[0].id)
            val (s2, _) = engine.flipCard(s1, pairCards[1].id)
            state = s2
        }

        assertTrue(state.isCompleted)
        assertEquals(6, state.matchedPairCount)
        assertEquals(12, state.flipsCount)
        assertEquals(3, state.starsEarned)
    }

    @Test
    fun `timer expiry causes game over and blocks flips`() {
        var state = engine.createInitialState(sampleLevel, seed = 42L)
        state = state.copy(remainingSeconds = 1)

        val ticked = engine.tickTimer(state)
        assertEquals(0, ticked.remainingSeconds)
        assertTrue(ticked.isGameOver)
        assertFalse(ticked.isCompleted)
        assertFalse(engine.canFlipCard(ticked, 0))
    }

    @Test
    fun `use hint reveals an unmatched pair and decrements remaining hints`() {
        var state = engine.createInitialState(sampleLevel, seed = 42L)
        assertEquals(3, state.hintsRemaining)

        val (hintState, hintedIds) = engine.useHint(state)
        assertNotNull(hintedIds)
        assertEquals(2, hintedIds?.size)
        assertEquals(2, hintState.hintsRemaining)
        assertEquals(1, hintState.hintsUsed)
        assertEquals(hintedIds?.toSet(), hintState.hintedCardIds)

        // Both cards belong to the same pair
        val cardA = hintState.cards[hintedIds!![0]]
        val cardB = hintState.cards[hintedIds[1]]
        assertEquals(cardA.pairId, cardB.pairId)
    }

    @Test
    fun `restart resets level state to initial playable condition`() {
        var state = engine.createInitialState(sampleLevel, seed = 42L)
        state = engine.flipCard(state, 0).first
        state = engine.tickTimer(state)

        val restarted = engine.restart(state, seed = 99L)
        assertEquals(0, restarted.flipsCount)
        assertEquals(0, restarted.matchedPairCount)
        assertEquals(120, restarted.remainingSeconds)
        assertTrue(restarted.cards.all { !it.isFaceUp && !it.isMatched })
    }
}
