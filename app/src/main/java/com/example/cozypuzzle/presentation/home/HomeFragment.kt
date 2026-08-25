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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private fun setupListeners() {
        binding.btnPlayDaily.setOnClickListener {
            viewModel.onEvent(HomeEvent.DailyPuzzleClicked)
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
        val playerName = state.playerName.ifBlank { getString(R.string.player_default_name) }
        binding.homeGreeting.text = getString(greetingRes(), playerName)
        binding.dailyPuzzleSize.text =
            getString(R.string.daily_puzzle_size_format, state.dailyPuzzleSize)
        gameModeAdapter.submitList(state.gameModes)
    }

    private fun handleEffect(effect: HomeEffect) {
        when (effect) {
            HomeEffect.FeatureComingSoon ->
                Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT)
                    .show()
            HomeEffect.NavigateToBasicPuzzles -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_basic_puzzle_select)
                }
            }
            HomeEffect.NavigateToCatLevels -> {
                val nav = findNavController()
                if (nav.currentDestination?.id == R.id.homeFragment) {
                    nav.navigate(R.id.action_home_to_cat_level_select)
                }
            }
        }
    }

    private fun greetingRes(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
    }

    private companion object {
        const val GRID_SPAN = 2
    }
}
