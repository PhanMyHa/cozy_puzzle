package com.example.cozypuzzle.core.findpair.engine

import com.example.cozypuzzle.core.findpair.model.FindPairCard
import com.example.cozypuzzle.core.findpair.model.FindPairLevel
import com.example.cozypuzzle.core.findpair.model.FindPairState
import com.example.cozypuzzle.core.findpair.model.FlipResult
import kotlin.random.Random

class FindPairEngine {

    fun createInitialState(level: FindPairLevel, seed: Long? = null): FindPairState {
        val totalPairs = level.totalPairs
        require(level.images.size >= totalPairs) {
            "Level requires $totalPairs images, but only ${level.images.size} provided"
        }

        val random = if (seed != null) Random(seed) else Random.Default

        val cardsList = mutableListOf<FindPairCard>()
        for (pairIdx in 0 until totalPairs) {
            val img = level.images[pairIdx]
            cardsList.add(FindPairCard(id = 0, pairId = pairIdx, imageRes = img))
            cardsList.add(FindPairCard(id = 0, pairId = pairIdx, imageRes = img))
        }

        // Shuffle cards
        cardsList.shuffle(random)

        // Assign unique IDs and alternating card back colors
        val cards = cardsList.mapIndexed { index, card ->
            val isPink = (index % 2 == 1)
            card.copy(id = index, isCardBackPink = isPink)
        }

        return FindPairState(
            level = level,
            cards = cards,
            revealedCardIds = emptyList(),
            matchedPairCount = 0,
            flipsCount = 0,
            hintsRemaining = FindPairState.DEFAULT_HINTS,
            hintsUsed = 0,
            hintedCardIds = emptySet(),
            remainingSeconds = level.timeLimitSeconds,
            isResolvingMismatch = false,
            isCompleted = false,
            isGameOver = false,
            isPaused = false,
            starsEarned = 0
        )
    }

    fun canFlipCard(state: FindPairState, cardId: Int): Boolean {
        if (state.isCompleted || state.isGameOver || state.isPaused || state.isResolvingMismatch) {
            return false
        }
        if (cardId !in state.cards.indices) return false
        if (state.revealedCardIds.size >= 2) return false
        if (cardId in state.revealedCardIds) return false

        val targetCard = state.cards[cardId]
        if (targetCard.isMatched || targetCard.isFaceUp) return false

        return true
    }

    fun flipCard(state: FindPairState, cardId: Int): Pair<FindPairState, FlipResult?> {
        if (!canFlipCard(state, cardId)) {
            return Pair(state, null)
        }

        val updatedCards = state.cards.toMutableList()
        val card = updatedCards[cardId]
        updatedCards[cardId] = card.copy(isFaceUp = true)

        val newRevealed = state.revealedCardIds + cardId
        val newFlips = state.flipsCount + 1

        if (newRevealed.size == 1) {
            val nextState = state.copy(
                cards = updatedCards,
                revealedCardIds = newRevealed,
                flipsCount = newFlips
            )
            return Pair(nextState, FlipResult.CardRevealed(cardId))
        }

        // Second card flipped: check matching
        val firstId = newRevealed[0]
        val secondId = newRevealed[1]
        val firstCard = updatedCards[firstId]
        val secondCard = updatedCards[secondId]

        return if (firstCard.pairId == secondCard.pairId) {
            // Match found!
            updatedCards[firstId] = firstCard.copy(isMatched = true, isFaceUp = true)
            updatedCards[secondId] = secondCard.copy(isMatched = true, isFaceUp = true)

            val newMatchedCount = state.matchedPairCount + 1
            val isComplete = (newMatchedCount == state.totalPairs)
            val stars = if (isComplete) {
                calculateStars(newFlips, state.remainingSeconds, state.level)
            } else 0

            val nextState = state.copy(
                cards = updatedCards,
                revealedCardIds = emptyList(),
                matchedPairCount = newMatchedCount,
                flipsCount = newFlips,
                isCompleted = isComplete,
                starsEarned = stars
            )
            Pair(nextState, FlipResult.CardsMatched(firstId, secondId, isComplete, stars))
        } else {
            // Mismatch
            val nextState = state.copy(
                cards = updatedCards,
                revealedCardIds = newRevealed,
                flipsCount = newFlips,
                isResolvingMismatch = true
            )
            Pair(nextState, FlipResult.CardsMismatched(firstId, secondId))
        }
    }

    fun resolveMismatch(state: FindPairState): FindPairState {
        if (!state.isResolvingMismatch) return state

        val updatedCards = state.cards.toMutableList()
        for (cardId in state.revealedCardIds) {
            if (cardId in updatedCards.indices) {
                val card = updatedCards[cardId]
                if (!card.isMatched) {
                    updatedCards[cardId] = card.copy(isFaceUp = false)
                }
            }
        }

        return state.copy(
            cards = updatedCards,
            revealedCardIds = emptyList(),
            isResolvingMismatch = false
        )
    }

    fun useHint(state: FindPairState): Pair<FindPairState, List<Int>?> {
        if (state.hintsRemaining <= 0 || state.isCompleted || state.isGameOver || state.isPaused) {
            return Pair(state, null)
        }

        // Find first unmatched pair
        val unmatchedPair = state.cards
            .filter { !it.isMatched }
            .groupBy { it.pairId }
            .values
            .firstOrNull { it.size == 2 } ?: return Pair(state, null)

        val pairIds = unmatchedPair.map { it.id }
        val nextState = state.copy(
            hintsRemaining = state.hintsRemaining - 1,
            hintsUsed = state.hintsUsed + 1,
            hintedCardIds = pairIds.toSet()
        )
        return Pair(nextState, pairIds)
    }

    fun clearHint(state: FindPairState): FindPairState {
        return state.copy(hintedCardIds = emptySet())
    }

    fun tickTimer(state: FindPairState): FindPairState {
        if (state.isPaused || state.isCompleted || state.isGameOver) return state

        val newRemaining = (state.remainingSeconds - 1).coerceAtLeast(0)
        val gameOver = (newRemaining == 0 && !state.isCompleted)

        return state.copy(
            remainingSeconds = newRemaining,
            isGameOver = gameOver
        )
    }

    fun pause(state: FindPairState): FindPairState {
        if (state.isCompleted || state.isGameOver) return state
        return state.copy(isPaused = true)
    }

    fun resume(state: FindPairState): FindPairState {
        return state.copy(isPaused = false)
    }

    fun restart(state: FindPairState, seed: Long? = null): FindPairState {
        return createInitialState(state.level, seed)
    }

    fun calculateStars(flips: Int, remainingSeconds: Int, level: FindPairLevel): Int {
        val quarterTime = level.timeLimitSeconds / 4
        return when {
            flips <= level.threeStarMaxFlips && remainingSeconds >= quarterTime -> 3
            flips <= level.twoStarMaxFlips -> 2
            else -> 1
        }
    }
}
