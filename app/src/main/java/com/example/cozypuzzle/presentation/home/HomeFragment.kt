package com.example.cozypuzzle.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentHomeBinding
import com.example.cozypuzzle.presentation.home.adapter.GameModeAdapter
import com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private val gameModeAdapter = GameModeAdapter { mode ->
        viewModel.onEvent(HomeEvent.GameModeClicked(mode))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGameModes()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupGameModes() {
        binding.gameModesRecycler.layoutManager = GridLayoutManager(requireContext(), GRID_SPAN)
        binding.gameModesRecycler.adapter = gameModeAdapter
    }

    private var currentAvatarUri: String? = "__initial__"

    private fun setupListeners() {
        binding.ivPlayerAvatar.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            if (bottomNav != null) {
                bottomNav.selectedItemId = R.id.profileFragment
            } else {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_profile)
                }
            }
        }
        binding.layoutSpeechBubble.setOnClickListener {
            viewModel.onEvent(HomeEvent.DailyPuzzleClicked)
        }
        binding.ivCatMascot.setOnClickListener {
            viewModel.onEvent(HomeEvent.DailyPuzzleClicked)
        }
        binding.layoutCurrencyPill.setOnClickListener {
            StarShopBottomSheetFragment.newInstance()
                .show(childFragmentManager, StarShopBottomSheetFragment.TAG)
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

    private fun render(state: HomeUiState) {
        binding.tvPlayerLevel.text = getString(R.string.home_level_format, state.playerLevel)
        binding.tvStarCount.text = state.playerCoins.toString()
        binding.pbLevelProgress.progress = state.levelProgress
        gameModeAdapter.submitList(state.gameModes)

        renderAvatar(state.avatarUri)
    }

    private fun renderAvatar(avatarUri: String?) {
        if (avatarUri == currentAvatarUri) return
        currentAvatarUri = avatarUri

        if (!avatarUri.isNullOrBlank()) {
            try {
                val file = java.io.File(avatarUri)
                if (file.exists()) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        binding.ivPlayerAvatar.setImageBitmap(bitmap)
                        binding.ivPlayerAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        return
                    }
                }
            } catch (_: Exception) {
            }
        }
        // Always fall back to cute default cat avatar so it is never blank or broken
        binding.ivPlayerAvatar.setImageResource(DEFAULT_AVATAR_RES)
        binding.ivPlayerAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
    }

    private fun handleEffect(effect: HomeEffect) {
        when (effect) {
            HomeEffect.FeatureComingSoon ->
                Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT)
                    .show()
            HomeEffect.NavigateToDailyPuzzle -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_daily_puzzle)
                }
            }
            HomeEffect.NavigateToCatPuzzle -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_cat_puzzle_select)
                }
            }
            HomeEffect.NavigateToColorCatSelect -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_color_cat_select)
                }
            }
            HomeEffect.NavigateToSlideCatSelect -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_slide_cat_select)
                }
            }
            HomeEffect.NavigateToFindPairSelect -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_find_pair_select)
                }
            }
        }
    }

    private companion object {
        const val GRID_SPAN = 2
        val DEFAULT_AVATAR_RES = R.drawable.img_cat_milo
    }
}
