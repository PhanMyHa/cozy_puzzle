package com.example.cozypuzzle.presentation.garden

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.garden.GetGardenUseCase
import com.example.cozypuzzle.domain.usecase.garden.MoveGardenItemUseCase
import com.example.cozypuzzle.domain.usecase.garden.PlaceGardenItemUseCase
import com.example.cozypuzzle.domain.usecase.garden.RemoveGardenItemUseCase
import com.example.cozypuzzle.domain.usecase.garden.RotateGardenItemUseCase
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
    private val getGardenUseCase: GetGardenUseCase,
    getPetsUseCase: GetPetsUseCase,
    private val placeGardenItemUseCase: PlaceGardenItemUseCase,
    private val moveGardenItemUseCase: MoveGardenItemUseCase,
    private val rotateGardenItemUseCase: RotateGardenItemUseCase,
    private val removeGardenItemUseCase: RemoveGardenItemUseCase
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
            is GardenEvent.SelectItem -> select(event.id)
            is GardenEvent.PlaceItem -> place(event.itemType)
            is GardenEvent.MoveItem -> move(event.id, event.x, event.y)
            is GardenEvent.RotateItem -> rotate(event.id, event.rotation)
            is GardenEvent.RemoveItem -> remove(event.id)
        }
    }

    private fun select(itemId: String) {
        if (!_uiState.value.isEditMode) return
        _uiState.update { it.copy(selectedItemId = itemId) }
    }

    private fun place(itemType: String) = launchEdit {
        placeGardenItemUseCase(itemType, _uiState.value.items)
    }

    private fun move(itemId: String, x: Float, y: Float) {
        // Live drag feedback without waiting for the database round trip.
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) item.copy(x = x, y = y) else item
                },
                selectedItemId = itemId
            )
        }
        launchEdit { moveGardenItemUseCase(itemId, x, y, _uiState.value.items) }
    }

    private fun rotate(itemId: String, rotation: Float) = launchEdit {
        rotateGardenItemUseCase(itemId, rotation, _uiState.value.items)
    }

    private fun remove(itemId: String) = launchEdit {
        removeGardenItemUseCase(itemId)
        _uiState.update { state ->
            state.copy(selectedItemId = null)
        }
    }

    private fun launchEdit(block: suspend () -> Unit) {
        if (!_uiState.value.isEditMode) return
        viewModelScope.launch { block() }
    }
}
