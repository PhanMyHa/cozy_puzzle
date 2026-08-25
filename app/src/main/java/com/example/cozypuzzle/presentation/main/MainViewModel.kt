package com.example.cozypuzzle.presentation.main

import com.example.cozypuzzle.domain.repository.ConfigRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isRemoteConfigReady: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshBasicPuzzleConfig()
    }

    fun refreshBasicPuzzleConfig() {
        viewModelScope.launch {
            configRepository.refreshBasicPuzzleConfig()
            _uiState.update { it.copy(isRemoteConfigReady = true) }
        }
    }
}
