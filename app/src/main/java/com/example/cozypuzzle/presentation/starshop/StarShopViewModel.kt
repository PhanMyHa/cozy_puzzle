package com.example.cozypuzzle.presentation.starshop

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.model.starshop.StarShopState
import com.example.cozypuzzle.domain.repository.StarRewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StarShopEffect {
    data class ShowRewardCelebration(val amount: Int, val title: String) : StarShopEffect
    data object NavigateToGames : StarShopEffect
    data object NavigateToDaily : StarShopEffect
    data object Dismiss : StarShopEffect
}

@HiltViewModel
class StarShopViewModel @Inject constructor(
    private val starRewardRepository: StarRewardRepository
) : BaseViewModel() {

    val uiState: StateFlow<StarShopState> = starRewardRepository.observeStarShopState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = StarShopState()
        )

    private val _effects = MutableSharedFlow<StarShopEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<StarShopEffect> = _effects.asSharedFlow()

    fun onClaimDailyLoginClicked() {
        viewModelScope.launch {
            val awarded = starRewardRepository.claimDailyLoginReward()
            if (awarded > 0) {
                _effects.tryEmit(
                    StarShopEffect.ShowRewardCelebration(
                        amount = awarded,
                        title = "Daily Login Reward"
                    )
                )
            }
        }
    }

    fun onClaimAchievementClicked(achievementId: String) {
        viewModelScope.launch {
            val awarded = starRewardRepository.claimAchievementReward(achievementId)
            if (awarded > 0) {
                _effects.tryEmit(
                    StarShopEffect.ShowRewardCelebration(
                        amount = awarded,
                        title = "Achievement Unlocked!"
                    )
                )
            }
        }
    }

    fun onPlayLevelsClicked() {
        _effects.tryEmit(StarShopEffect.NavigateToGames)
    }

    fun onGoToDailyClicked() {
        _effects.tryEmit(StarShopEffect.NavigateToDaily)
    }

    fun onCloseClicked() {
        _effects.tryEmit(StarShopEffect.Dismiss)
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
