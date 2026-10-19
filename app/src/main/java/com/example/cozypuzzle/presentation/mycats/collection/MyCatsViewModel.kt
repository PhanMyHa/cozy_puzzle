package com.example.cozypuzzle.presentation.mycats.collection

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.repository.CatCollectionRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCatsViewModel @Inject constructor(
    private val catRepository: CatCollectionRepository,
    private val playerRepository: PlayerRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MyCatsUiState(isLoading = true))
    val uiState: StateFlow<MyCatsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<MyCatsEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<MyCatsEffect> = _effects.asSharedFlow()

    private val selectedFilter = MutableStateFlow(CatFilter.ALL)
    private var pendingCelebrations = mutableListOf<CatModel>()

    init {
        checkUnlockProgress()
        observeData()
    }

    private fun checkUnlockProgress() {
        viewModelScope.launch {
            val newlyUnlocked = catRepository.evaluateUnlockConditions()
            if (newlyUnlocked.isNotEmpty()) {
                pendingCelebrations.addAll(newlyUnlocked)
                triggerNextCelebration()
            }
        }
    }

    private fun triggerNextCelebration() {
        if (pendingCelebrations.isNotEmpty()) {
            val cat = pendingCelebrations.removeAt(0)
            _effects.tryEmit(MyCatsEffect.ShowNewCatUnlocked(cat))
        }
    }

    fun onCelebrationDismissed(catId: String) {
        viewModelScope.launch {
            catRepository.markCelebrated(catId)
            triggerNextCelebration()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                selectedFilter,
                catRepository.observeOwnedCatIds(),
                catRepository.observeFavoriteCatIds(),
                playerRepository.observePlayer()
            ) { filter, ownedIds, favoriteIds, player ->
                val allCats = catRepository.getAllCats()
                val totalCount = allCats.size
                val ownedCount = allCats.count { ownedIds.contains(it.id) }

                val themeProgress = CatTheme.values().map { theme ->
                    val themeCats = allCats.filter { it.theme == theme }
                    val themeOwned = themeCats.count { ownedIds.contains(it.id) }
                    ThemeProgressUiModel(
                        theme = theme,
                        ownedCount = themeOwned,
                        totalCount = themeCats.size
                    )
                }

                val progressMap = catRepository.getAllCatsUnlockProgress()

                val catItems = allCats.map { cat ->
                    val isOwned = ownedIds.contains(cat.id)
                    val isFav = favoriteIds.contains(cat.id)
                    val (curr, target) = progressMap[cat.id] ?: Pair(0, 1)
                    CatItemUiModel(
                        cat = cat,
                        isOwned = isOwned,
                        isFavorite = isFav,
                        currentProgress = curr,
                        targetProgress = target
                    )
                }

                val filteredCats = when (filter) {
                    CatFilter.ALL -> catItems
                    CatFilter.OWNED -> catItems.filter { it.isOwned }
                    CatFilter.LOCKED -> catItems.filter { !it.isOwned }
                }

                _uiState.update { state ->
                    state.copy(
                        filter = filter,
                        cats = filteredCats,
                        totalCats = totalCount,
                        ownedCatsCount = ownedCount,
                        playerStars = player.coins,
                        themeProgress = themeProgress,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun onFilterSelected(filter: CatFilter) {
        selectedFilter.value = filter
    }

    fun onCatClicked(catId: String) {
        _effects.tryEmit(MyCatsEffect.NavigateToDetail(catId))
    }

    fun onLockedCatClicked(item: CatItemUiModel) {
        _effects.tryEmit(
            MyCatsEffect.ShowLockedCondition(
                cat = item.cat,
                current = item.currentProgress,
                target = item.targetProgress
            )
        )
    }
}
