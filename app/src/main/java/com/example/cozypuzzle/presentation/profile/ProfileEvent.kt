package com.example.cozypuzzle.presentation.profile

sealed interface ProfileEvent {

    data object EditNameClicked : ProfileEvent

    data object SettingsClicked : ProfileEvent
}

sealed interface ProfileEffect {

    data object FeatureComingSoon : ProfileEffect
}
