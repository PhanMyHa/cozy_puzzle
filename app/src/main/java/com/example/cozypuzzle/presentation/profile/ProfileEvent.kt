package com.example.cozypuzzle.presentation.profile

sealed interface ProfileEvent {

    data object EditAvatarClicked : ProfileEvent

    data class AvatarPicked(val sourceUri: String) : ProfileEvent

    data object EditNameClicked : ProfileEvent

    data class NameConfirmed(val name: String) : ProfileEvent

    data class ToggleSound(val enabled: Boolean) : ProfileEvent

    data class ToggleMusic(val enabled: Boolean) : ProfileEvent

    data class ToggleVibration(val enabled: Boolean) : ProfileEvent

    data object PrivacyPolicyClicked : ProfileEvent

    data object TermsOfServiceClicked : ProfileEvent
}

sealed interface ProfileEffect {

    data object LaunchAvatarPicker : ProfileEffect

    data class ShowEditNameDialog(val currentName: String) : ProfileEffect

    data object AvatarSaveFailed : ProfileEffect

    data object NameInvalid : ProfileEffect

    data class ShowInfoDialog(val title: String, val message: String) : ProfileEffect
}
