package com.example.cozypuzzle.presentation.shop

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentShopBinding
import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.presentation.shop.adapter.ShopItemAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopFragment : BaseFragment<FragmentShopBinding>(FragmentShopBinding::inflate) {

    private val viewModel: ShopViewModel by viewModels()
    private val itemAdapter = ShopItemAdapter(onItemClick = { id ->
        viewModel.onEvent(ShopEvent.ItemClicked(id))
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGrid()
        setupCategoryChips()
        observeUiState()
        observeEffects()
    }

    private fun setupGrid() {
        binding.shopGrid.layoutManager = GridLayoutManager(requireContext(), GRID_SPAN)
        binding.shopGrid.adapter = itemAdapter
    }

    private fun setupCategoryChips() {
        val chipToCategory = mapOf(
            binding.chipAll.id to null,
            binding.chipFlowers.id to ItemCategory.FLOWERS,
            binding.chipTrees.id to ItemCategory.TREES,
            binding.chipBuildings.id to ItemCategory.BUILDINGS,
            binding.chipFurniture.id to ItemCategory.FURNITURE,
            binding.chipDecorations.id to ItemCategory.DECORATIONS,
            binding.chipPets.id to ItemCategory.PETS,
            binding.chipThemes.id to ItemCategory.THEMES
        )
        binding.categoryChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val category = checkedIds.firstOrNull()?.let(chipToCategory::get)
            viewModel.onEvent(ShopEvent.CategorySelected(category))
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

    private fun render(state: ShopUiState) {
        binding.shopCoins.text = getString(R.string.coins_format, state.coins)
        binding.shopProgress.isVisible = state.isLoading
        itemAdapter.submitList(state.visibleItems)
    }

    private fun handleEffect(effect: ShopEffect) {
        val messageRes = when (effect) {
            ShopEffect.PurchaseSuccess -> R.string.shop_purchase_success
            ShopEffect.InsufficientCoins -> R.string.shop_insufficient_coins
            ShopEffect.AlreadyOwned -> R.string.shop_already_owned
        }
        Snackbar.make(
            binding.root,
            messageRes,
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(
            ContextCompat.getColor(
                requireContext(),
                if (effect == ShopEffect.PurchaseSuccess) {
                    R.color.cozy_green_container
                } else {
                    R.color.cozy_blossom
                }
            )
        ).show()
    }

    private companion object {
        const val GRID_SPAN = 3
    }
}
