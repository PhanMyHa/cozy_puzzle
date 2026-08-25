package com.example.cozypuzzle.presentation.profile

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getProfileUseCase().collect { player ->
                _uiState.update { state ->
                    state.copy(
                        playerName = player.name,
                        level = player.level,
                        coins = player.coins,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.EditNameClicked ->
                _effects.tryEmit(ProfileEffect.FeatureComingSoon)
            ProfileEvent.SettingsClicked ->
                _effects.tryEmit(ProfileEffect.FeatureComingSoon)
        }
    }
}
