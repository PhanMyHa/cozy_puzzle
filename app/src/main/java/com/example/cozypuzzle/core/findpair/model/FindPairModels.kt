package com.example.cozypuzzle.core.findpair.model

import androidx.annotation.DrawableRes

enum class FindPairDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class FindPairCard(
    val id: Int,
    val pairId: Int,
    @DrawableRes val imageRes: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val isCardBackPink: Boolean = false
)

data class FindPairLevel(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: FindPairDifficulty,
    val columns: Int = 3,
    val rows: Int = 4,
    val timeLimitSeconds: Int = 120,
    val threeStarMaxFlips: Int = 16,
    val twoStarMaxFlips: Int = 24,
    val images: List<Int>,
    @DrawableRes val thumbnailRes: Int = images.first()
) {
    val totalCards: Int get() = columns * rows
    val totalPairs: Int get() = totalCards / 2
}

sealed interface FlipResult {
    data class CardRevealed(val cardId: Int) : FlipResult
    data class CardsMatched(
        val firstCardId: Int,
        val secondCardId: Int,
        val isCompleted: Boolean,
        val starsEarned: Int
    ) : FlipResult
    data class CardsMismatched(
        val firstCardId: Int,
        val secondCardId: Int
    ) : FlipResult
}

data class FindPairState(
    val level: FindPairLevel,
    val cards: List<FindPairCard>,
    val revealedCardIds: List<Int> = emptyList(),
    val matchedPairCount: Int = 0,
    val flipsCount: Int = 0,
    val hintsRemaining: Int = DEFAULT_HINTS,
    val hintsUsed: Int = 0,
    val hintedCardIds: Set<Int> = emptySet(),
    val remainingSeconds: Int,
    val isResolvingMismatch: Boolean = false,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val starsEarned: Int = 0
) {
    val totalPairs: Int get() = level.totalPairs

    companion object {
        const val DEFAULT_HINTS = 3
    }
}
