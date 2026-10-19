package com.example.cozypuzzle.presentation.daily.tasks

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.model.daily.DailyMissionItemUiModel
import com.example.cozypuzzle.domain.model.daily.DailyTasksEffect
import com.example.cozypuzzle.domain.model.daily.DailyTasksUiState
import com.example.cozypuzzle.domain.repository.DailyTasksRepository
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

@HiltViewModel
class DailyTasksViewModel @Inject constructor(
    private val repository: DailyTasksRepository
) : BaseViewModel() {

    val uiState: StateFlow<DailyTasksUiState> = repository.observeUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DailyTasksUiState(isLoading = true)
        )

    private val _effects = MutableSharedFlow<DailyTasksEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<DailyTasksEffect> = _effects.asSharedFlow()

    fun onMissionActionClicked(mission: DailyMissionItemUiModel) {
        if (mission.isCompleted && !mission.isClaimed) {
            viewModelScope.launch {
                val rewarded = repository.claimMissionReward(mission.id)
                if (rewarded > 0) {
                    _effects.tryEmit(
                        DailyTasksEffect.ShowRewardCelebration(
                            amount = rewarded,
                            title = "Mission Complete!"
                        )
                    )
                }
            }
        } else if (mission.targetModeId != null) {
            _effects.tryEmit(DailyTasksEffect.NavigateToGame(mission.targetModeId))
        }
    }

    fun onClaimBonusClicked() {
        val current = uiState.value
        if (!current.isBonusAvailable || current.isBonusClaimed) return

        viewModelScope.launch {
            val rewarded = repository.claimBonusReward()
            if (rewarded > 0) {
                _effects.tryEmit(
                    DailyTasksEffect.ShowRewardCelebration(
                        amount = rewarded,
                        title = "Daily Bonus!"
                    )
                )
            }
        }
    }
}
