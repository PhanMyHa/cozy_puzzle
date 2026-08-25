package com.example.cozypuzzle.presentation.garden

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentGardenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GardenFragment : BaseFragment<FragmentGardenBinding>(FragmentGardenBinding::inflate) {

    private val viewModel: GardenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnToggleEditMode.setOnClickListener {
            viewModel.onEvent(GardenEvent.ToggleEditMode)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: GardenUiState) {
        binding.btnToggleEditMode.setText(
            if (state.isEditMode) R.string.btn_done_editing else R.string.btn_edit_garden
        )
        binding.gardenSummary.text =
            getString(R.string.garden_summary_format, state.items.size, state.petCount)
        binding.gardenHint.visibility =
            if (state.items.isEmpty() && state.petCount == 0) View.VISIBLE else View.GONE
    }
}
