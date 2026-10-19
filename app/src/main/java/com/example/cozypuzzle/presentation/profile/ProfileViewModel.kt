package com.example.cozypuzzle.presentation.profile

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.profile.ProfileLogic
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.repository.CatCollectionRepository
import com.example.cozypuzzle.domain.repository.CatPuzzleProgressRepository
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
import com.example.cozypuzzle.domain.repository.DailyTasksRepository
import com.example.cozypuzzle.domain.repository.FindPairProgressRepository
import com.example.cozypuzzle.domain.repository.SettingsRepository
import com.example.cozypuzzle.domain.repository.SlideCatProgressRepository
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
import com.example.cozypuzzle.domain.usecase.profile.SavePlayerAvatarUseCase
import com.example.cozypuzzle.domain.usecase.profile.UpdatePlayerNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase,
    private val savePlayerAvatarUseCase: SavePlayerAvatarUseCase,
    private val updatePlayerNameUseCase: UpdatePlayerNameUseCase,
    catPuzzleProgressRepository: CatPuzzleProgressRepository,
    colorCatProgressRepository: ColorCatProgressRepository,
    slideCatProgressRepository: SlideCatProgressRepository,
    findPairProgressRepository: FindPairProgressRepository,
    catCollectionRepository: CatCollectionRepository,
    dailyTasksRepository: DailyTasksRepository,
    private val settingsRepository: SettingsRepository
) : BaseViewModel() {

    private data class GamesProgress(
        val catPuzzle: Map<String, Int>,
        val colorCat: Map<String, Int>,
        val slideCat: Map<String, Int>,
        val findPair: Map<String, Int>
    )

    private val gamesProgressFlow = combine(
        catPuzzleProgressRepository.observeLevelStars(),
        colorCatProgressRepository.observeLevelStars(),
        slideCatProgressRepository.observeLevelStars(),
        findPairProgressRepository.observeLevelStars()
    ) { catPuzzle, colorCat, slideCat, findPair ->
        GamesProgress(catPuzzle, colorCat, slideCat, findPair)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        getProfileUseCase(),
        gamesProgressFlow,
        catCollectionRepository.observeOwnedCatIds(),
        dailyTasksRepository.observeUiState(),
        settingsRepository.observeSettings()
    ) { player, games, ownedCats, dailyState, settings ->
        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = games.catPuzzle,
            colorCatStars = games.colorCat,
            slideCatStars = games.slideCat,
            findPairStars = games.findPair,
            ownedCatsCount = ownedCats.size,
            totalCatsCount = 12,
            dailyStreak = dailyState.streak.streakCount
        )

        val achievements = ProfileLogic.evaluateAchievements(stats)
        val unlockedCount = achievements.count { it.isUnlocked }

        ProfileUiState(
            playerName = player.name,
            avatarUri = player.avatarUri,
            level = player.level,
            levelProgress = ((player.coins % 100).coerceIn(20, 85)),
            coins = player.coins,
            stats = stats,
            achievements = achievements,
            unlockedAchievementsCount = unlockedCount,
            totalAchievementsCount = achievements.size,
            settings = settings,
            appVersion = "1.0",
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ProfileUiState(isLoading = true)
    )

    private val _effects = MutableSharedFlow<ProfileEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.EditAvatarClicked ->
                emitEffect(ProfileEffect.LaunchAvatarPicker)
            is ProfileEvent.AvatarPicked ->
                importAvatar(event.sourceUri)
            ProfileEvent.EditNameClicked ->
                emitEffect(ProfileEffect.ShowEditNameDialog(uiState.value.playerName))
            is ProfileEvent.NameConfirmed ->
                confirmName(event.name)
            is ProfileEvent.ToggleSound ->
                setSoundEffectsEnabled(event.enabled)
            is ProfileEvent.ToggleMusic ->
                setMusicEnabled(event.enabled)
            is ProfileEvent.ToggleVibration ->
                setVibrationEnabled(event.enabled)
            ProfileEvent.PrivacyPolicyClicked ->
                emitEffect(
                    ProfileEffect.ShowInfoDialog(
                        title = "Privacy Policy",
                        message = "Cozy Puzzle respects your privacy. All your puzzle progress, collection, and settings are stored locally on your device without collecting personal data."
                    )
                )
            ProfileEvent.TermsOfServiceClicked ->
                emitEffect(
                    ProfileEffect.ShowInfoDialog(
                        title = "Terms of Service",
                        message = "Cozy Puzzle is designed to provide a relaxing, cozy offline puzzle experience for all players."
                    )
                )
        }
    }

    private fun importAvatar(sourceUri: String) {
        viewModelScope.launch {
            try {
                val saved = savePlayerAvatarUseCase(sourceUri)
                if (!saved) emitEffect(ProfileEffect.AvatarSaveFailed)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emitEffect(ProfileEffect.AvatarSaveFailed)
            }
        }
    }

    private fun confirmName(name: String) {
        viewModelScope.launch {
            if (!updatePlayerNameUseCase(name)) {
                emitEffect(ProfileEffect.NameInvalid)
            }
        }
    }

    private fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEffectsEnabled(enabled)
        }
    }

    private fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    private fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }

    private fun emitEffect(effect: ProfileEffect) {
        _effects.tryEmit(effect)
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
