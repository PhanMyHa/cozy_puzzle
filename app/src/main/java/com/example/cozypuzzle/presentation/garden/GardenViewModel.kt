package com.example.cozypuzzle.presentation.garden

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.garden.GetGardenUseCase
import com.example.cozypuzzle.domain.usecase.pet.GetPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GardenViewModel @Inject constructor(
    getGardenUseCase: GetGardenUseCase,
    getPetsUseCase: GetPetsUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getGardenUseCase(),
                getPetsUseCase()
            ) { items, pets ->
                items to pets
            }.collect { (items, pets) ->
                _uiState.update { state ->
                    state.copy(
                        items = items,
                        petCount = pets.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: GardenEvent) {
        when (event) {
            GardenEvent.ToggleEditMode ->
                _uiState.update { it.copy(isEditMode = !it.isEditMode, selectedItemId = null) }
            is GardenEvent.SelectItem -> Unit
            is GardenEvent.MoveItem -> Unit
            is GardenEvent.RotateItem -> Unit
            is GardenEvent.RemoveItem -> Unit
        }
    }
}
