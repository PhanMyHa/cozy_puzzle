package com.example.cozypuzzle.presentation.mycats.collection

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentMyCatsBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyCatsFragment : BaseFragment<FragmentMyCatsBinding>(FragmentMyCatsBinding::inflate) {

    private val viewModel: MyCatsViewModel by viewModels()
    private lateinit var adapter: MyCatsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupRecyclerView() {
        adapter = MyCatsAdapter(
            onCatClicked = { catId -> viewModel.onCatClicked(catId) },
            onLockedCatClicked = { item -> viewModel.onLockedCatClicked(item) }
        )
        binding.recyclerCats.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerCats.adapter = adapter
    }

    private fun setupListeners() {
        binding.layoutCurrencyPill.setOnClickListener {
            com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.newInstance()
                .show(childFragmentManager, com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.TAG)
        }
        binding.btnFilterAll.setOnClickListener {
            viewModel.onFilterSelected(CatFilter.ALL)
        }
        binding.btnFilterOwned.setOnClickListener {
            viewModel.onFilterSelected(CatFilter.OWNED)
        }
        binding.btnFilterLocked.setOnClickListener {
            viewModel.onFilterSelected(CatFilter.LOCKED)
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
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is MyCatsEffect.NavigateToDetail -> {
                            val args = bundleOf(KEY_CAT_ID to effect.catId)
                            findNavController().navigate(R.id.action_my_cats_to_cat_detail, args)
                        }
                        is MyCatsEffect.ShowNewCatUnlocked -> {
                            CozyDialogs.showNewCatUnlockedDialog(
                                context = requireContext(),
                                cat = effect.cat,
                                onDismiss = { viewModel.onCelebrationDismissed(effect.cat.id) }
                            )
                        }
                        is MyCatsEffect.ShowLockedCondition -> {
                            val progressStr = getString(
                                R.string.cat_unlock_progress_format,
                                effect.current,
                                effect.target
                            )
                            Snackbar.make(
                                binding.root,
                                "🔒 ${effect.cat.unlockHint} ($progressStr)",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun render(state: MyCatsUiState) {
        binding.tvStarCount.text = state.playerStars.toString()
        binding.tvCollectionProgress.text = getString(
            R.string.my_cats_progress_format,
            state.ownedCatsCount,
            state.totalCats
        )
        binding.tvCollectionPercentage.text = "${state.progressPercentage}%"
        binding.pbCollection.progress = state.progressPercentage

        updateFilterButtons(state.filter)

        adapter.submitList(state.cats)
        binding.layoutEmptyState.visibility = if (state.cats.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateFilterButtons(selectedFilter: CatFilter) {
        val pinkColor = ContextCompat.getColor(requireContext(), R.color.cozy_pastel_pink)
        val blueColor = ContextCompat.getColor(requireContext(), R.color.cozy_pastel_blue)

        stylePillButton(binding.btnFilterAll, selectedFilter == CatFilter.ALL, pinkColor, blueColor)
        stylePillButton(binding.btnFilterOwned, selectedFilter == CatFilter.OWNED, pinkColor, blueColor)
        stylePillButton(binding.btnFilterLocked, selectedFilter == CatFilter.LOCKED, pinkColor, blueColor)
    }

    private fun stylePillButton(
        button: MaterialButton,
        isSelected: Boolean,
        pinkColor: Int,
        blueColor: Int
    ) {
        if (isSelected) {
            button.backgroundTintList = ColorStateList.valueOf(pinkColor)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            button.backgroundTintList = ColorStateList.valueOf(blueColor)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    companion object {
        const val KEY_CAT_ID = "cat_id"
    }
}
