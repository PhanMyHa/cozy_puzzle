package com.example.cozypuzzle.presentation.garden

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.customview.GardenCanvasView
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentGardenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GardenFragment : BaseFragment<FragmentGardenBinding>(FragmentGardenBinding::inflate) {

    private val viewModel: GardenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCanvas()
        setupListeners()
        observeUiState()
    }

    private fun setupCanvas() {
        binding.gardenCanvas.onItemSelected = { itemId ->
            viewModel.onEvent(GardenEvent.SelectItem(itemId))
        }
        binding.gardenCanvas.onItemMoved = { itemId, x, y ->
            viewModel.onEvent(GardenEvent.MoveItem(itemId, x, y))
        }
        binding.gardenCanvas.onEmptyTapped = {
            // Deselect handled implicitly; nothing to persist.
        }
    }

    private fun setupListeners() {
        binding.btnToggleEditMode.setOnClickListener {
            viewModel.onEvent(GardenEvent.ToggleEditMode)
        }
        binding.btnRotateItem.setOnClickListener {
            val selected = viewModel.uiState.value.selectedItemId ?: return@setOnClickListener
            val current = viewModel.uiState.value.items
                .firstOrNull { it.id == selected }?.rotation ?: return@setOnClickListener
            viewModel.onEvent(GardenEvent.RotateItem(selected, current + ROTATION_STEP_DEGREES))
        }
        binding.btnRemoveItem.setOnClickListener {
            val selected = viewModel.uiState.value.selectedItemId ?: return@setOnClickListener
            viewModel.onEvent(GardenEvent.RemoveItem(selected))
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
        binding.gardenHint.isVisible = state.items.isEmpty() && state.petCount == 0

        binding.paletteScroll.isVisible = state.isEditMode
        binding.selectionRow.isVisible = state.isEditMode && state.selectedItemId != null
        renderPalette(state)

        binding.gardenCanvas.render(
            items = state.items,
            selectedItemId = state.selectedItemId,
            isEditMode = state.isEditMode
        )
    }

    private fun renderPalette(state: GardenUiState) {
        val paletteRow = binding.paletteRow
        if (paletteRow.childCount != state.paletteItems.size) {
            paletteRow.removeAllViews()
            state.paletteItems.forEach { itemType ->
                paletteRow.addView(buildPaletteButton(itemType))
            }
        }
    }

    private fun buildPaletteButton(itemType: String): Button {
        val button = Button(requireContext())
        button.text = emojiFor(itemType)
        button.textSize = EMOJI_TEXT_SIZE_SP
        button.isAllCaps = false
        button.gravity = Gravity.CENTER
        button.minimumWidth = PALETTE_BUTTON_MIN_WIDTH_DP.dpToPx()
        button.setOnClickListener { viewModel.onEvent(GardenEvent.PlaceItem(itemType)) }
        return button
    }

    private fun emojiFor(itemType: String): String = when (itemType) {
        GardenCanvasView.ITEM_FLOWER_TULIP -> "🌷"
        GardenCanvasView.ITEM_FLOWER_SAKURA -> "🌸"
        GardenCanvasView.ITEM_TREE -> "🌳"
        GardenCanvasView.ITEM_BUSH -> "🌿"
        GardenCanvasView.ITEM_MUSHROOM -> "🍄"
        GardenCanvasView.ITEM_ROCK -> "🪨"
        GardenCanvasView.ITEM_HOUSE -> "🏡"
        else -> "🌼"
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val ROTATION_STEP_DEGREES = 45f
        const val EMOJI_TEXT_SIZE_SP = 24f
        const val PALETTE_BUTTON_MIN_WIDTH_DP = 56
    }
}
