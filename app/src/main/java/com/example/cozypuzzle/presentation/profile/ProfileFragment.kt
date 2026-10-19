package com.example.cozypuzzle.presentation.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentProfileBinding
import com.example.cozypuzzle.presentation.profile.adapter.AchievementAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class ProfileFragment :
    BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var achievementAdapter: AchievementAdapter
    private var loadedAvatarUri: String? = null
    private var cachedAppVersion: String? = null

    private val avatarPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onEvent(ProfileEvent.AvatarPicked(it.toString())) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAchievementsList()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupAchievementsList() {
        achievementAdapter = AchievementAdapter()
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAchievements.adapter = achievementAdapter
    }

    private fun setupListeners() {
        binding.layoutCoinsPill.setOnClickListener {
            com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.newInstance()
                .show(childFragmentManager, com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.TAG)
        }

        binding.layoutAvatarContainer.setOnClickListener {
            viewModel.onEvent(ProfileEvent.EditAvatarClicked)
        }

        binding.layoutEditNickname.setOnClickListener {
            viewModel.onEvent(ProfileEvent.EditNameClicked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.uiState.value.settings.soundEffectsEnabled) {
                viewModel.onEvent(ProfileEvent.ToggleSound(isChecked))
            }
        }

        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.uiState.value.settings.musicEnabled) {
                viewModel.onEvent(ProfileEvent.ToggleMusic(isChecked))
            }
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.uiState.value.settings.vibrationEnabled) {
                viewModel.onEvent(ProfileEvent.ToggleVibration(isChecked))
            }
        }

        binding.rowPrivacyPolicy.setOnClickListener {
            viewModel.onEvent(ProfileEvent.PrivacyPolicyClicked)
        }

        binding.rowTermsService.setOnClickListener {
            viewModel.onEvent(ProfileEvent.TermsOfServiceClicked)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(::handleEffect)
            }
        }
    }

    private fun render(state: ProfileUiState) {
        val playerName = state.playerName.ifBlank { getString(R.string.player_default_name) }
        binding.tvPlayerNickname.text = playerName
        binding.tvPlayerLevel.text = getString(R.string.profile_level_format, state.level)
        binding.pbPlayerLevel.progress = state.levelProgress
        binding.tvPlayerCoins.text = state.coins.toString()

        loadAvatar(state.avatarUri)

        // Stats card
        binding.tvStatStars.text = getString(R.string.profile_stars_count_format, state.stats.totalStars)
        binding.tvStatLevels.text = getString(R.string.profile_levels_count_format, state.stats.levelsCompleted)
        binding.tvStatCats.text = getString(
            R.string.profile_cats_count_format,
            state.stats.catsCollected,
            state.stats.totalCats
        )
        binding.tvStatStreak.text = getString(R.string.profile_streak_count_format, state.stats.dailyStreak)

        // Achievements
        binding.tvAchievementsCount.text = getString(
            R.string.profile_achievements_count_format,
            state.unlockedAchievementsCount,
            state.totalAchievementsCount
        )
        achievementAdapter.submitList(state.achievements)

        // Settings switches (safely update without unnecessary re-triggers)
        if (binding.switchSound.isChecked != state.settings.soundEffectsEnabled) {
            binding.switchSound.isChecked = state.settings.soundEffectsEnabled
        }
        if (binding.switchMusic.isChecked != state.settings.musicEnabled) {
            binding.switchMusic.isChecked = state.settings.musicEnabled
        }
        if (binding.switchVibration.isChecked != state.settings.vibrationEnabled) {
            binding.switchVibration.isChecked = state.settings.vibrationEnabled
        }

        binding.tvAppVersion.text = "v${getAppVersion()}"
    }

    private fun loadAvatar(avatarUri: String?) {
        if (avatarUri == loadedAvatarUri) return
        loadedAvatarUri = avatarUri

        if (avatarUri == null) {
            showPlaceholderAvatar()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                decodeSampledBitmap(File(avatarUri), TARGET_AVATAR_SIZE_PX)
            }
            if (bitmap != null && viewLifecycleOwner.lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED
                )
            ) {
                binding.ivProfileAvatar.setImageBitmap(bitmap)
                binding.ivProfileAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.ivProfileAvatar.setPadding(0, 0, 0, 0)
            } else {
                showPlaceholderAvatar()
            }
        }
    }

    private fun showPlaceholderAvatar() {
        binding.ivProfileAvatar.setImageResource(R.drawable.img_cat_milo)
        binding.ivProfileAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        binding.ivProfileAvatar.setPadding(0, 0, 0, 0)
    }

    private fun decodeSampledBitmap(file: File, targetSize: Int): Bitmap? = runCatching {
        if (!file.exists()) return@runCatching null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetSize ||
            bounds.outHeight / (sample * 2) >= targetSize
        ) {
            sample *= 2
        }
        BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample }
        )
    }.getOrNull()

    private fun handleEffect(effect: ProfileEffect) {
        when (effect) {
            ProfileEffect.LaunchAvatarPicker -> launchAvatarPicker()
            is ProfileEffect.ShowEditNameDialog -> showEditNameDialog(effect.currentName)
            ProfileEffect.AvatarSaveFailed ->
                Snackbar.make(binding.root, R.string.profile_avatar_failed, Snackbar.LENGTH_SHORT)
                    .show()
            ProfileEffect.NameInvalid ->
                Snackbar.make(binding.root, R.string.profile_name_invalid, Snackbar.LENGTH_SHORT)
                    .show()
            is ProfileEffect.ShowInfoDialog -> {
                CozyDialogs.showInfoDialog(
                    context = requireContext(),
                    title = effect.title,
                    message = effect.message
                )
            }
        }
    }

    private fun launchAvatarPicker() {
        avatarPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showEditNameDialog(currentName: String) {
        CozyDialogs.showEditNicknameDialog(
            context = requireContext(),
            currentName = currentName
        ) { newName ->
            viewModel.onEvent(ProfileEvent.NameConfirmed(newName))
        }
    }

    private fun getAppVersion(): String {
        cachedAppVersion?.let { return it }
        val version = try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
        cachedAppVersion = version
        return version
    }

    private companion object {
        const val TARGET_AVATAR_SIZE_PX = 512
    }
}
