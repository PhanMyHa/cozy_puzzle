package com.example.cozypuzzle.presentation.mycats.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentCatDetailBinding
import com.example.cozypuzzle.presentation.mycats.collection.MyCatsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatDetailFragment : BaseFragment<FragmentCatDetailBinding>(FragmentCatDetailBinding::inflate) {

    private val viewModel: CatDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val catId = arguments?.getString(MyCatsFragment.KEY_CAT_ID)
        if (catId != null && savedInstanceState == null) {
            viewModel.loadCat(catId)
        }

        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.onFavoriteToggled()
        }

        binding.btnActionPet.setOnClickListener {
            viewModel.onModeSelected(InteractionMode.PET)
            viewModel.onInteractClicked()
        }

        binding.btnActionPlay.setOnClickListener {
            viewModel.onModeSelected(InteractionMode.PLAY)
            viewModel.onInteractClicked()
        }

        binding.btnActionSmile.setOnClickListener {
            viewModel.onModeSelected(InteractionMode.SMILE)
            viewModel.onInteractClicked()
        }

        binding.btnActionSleep.setOnClickListener {
            viewModel.onModeSelected(InteractionMode.SLEEP)
            viewModel.onInteractClicked()
        }

        binding.btnInteract.setOnClickListener {
            viewModel.onInteractClicked()
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
                        CatDetailEffect.NavigateBack -> findNavController().popBackStack()
                        is CatDetailEffect.PlayBounceReaction -> playBounceAnimation(effect.speech)
                    }
                }
            }
        }
    }

    private fun render(state: CatDetailUiState) {
        val cat = state.cat ?: return
        val context = requireContext()

        binding.tvCatName.text = if (state.isOwned) cat.name else getString(R.string.my_cats_locked_name)
        binding.tvCatDescription.text = cat.description
        binding.ivCatHero.setImageResource(cat.imageRes)

        // Rarity Badge styling
        binding.tvCatRarity.text = context.getString(cat.rarity.titleRes)
        binding.tvCatRarity.setTextColor(ContextCompat.getColor(context, cat.rarity.colorRes))
        binding.tvCatRarity.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, cat.rarity.bgRes)
        )

        // Info Rows
        binding.tvInfoRarity.text = context.getString(cat.rarity.titleRes)
        binding.tvInfoCategory.text = context.getString(cat.theme.titleRes)
        binding.tvInfoFoundIn.text = cat.foundIn

        // Favorite Button Icon
        binding.btnFavorite.setIconResource(
            if (state.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        // Speech bubble text
        binding.tvSpeechText.text = state.speechReaction

        if (state.isOwned) {
            binding.ivCatHero.alpha = 1.0f
            binding.ivCatHero.colorFilter = null
            binding.layoutInteractionSection.visibility = View.VISIBLE
            binding.cardLockedCondition.visibility = View.GONE
            binding.tvInteractTitle.text = getString(R.string.cat_detail_interact_title, cat.name)
        } else {
            binding.ivCatHero.alpha = 0.55f
            binding.layoutInteractionSection.visibility = View.GONE
            binding.cardLockedCondition.visibility = View.VISIBLE
            binding.tvLockedConditionText.text = cat.unlockHint
            binding.pbLockedCondition.progress = state.progressPercentage
            binding.tvLockedConditionRatio.text = getString(
                R.string.cat_unlock_progress_format,
                state.currentProgress,
                state.targetProgress
            )
        }
    }

    private fun playBounceAnimation(speech: String) {
        binding.tvSpeechText.text = speech

        // Mascot Bounce Animation
        val scaleX = ObjectAnimator.ofFloat(binding.ivCatHero, View.SCALE_X, 1.0f, 1.15f, 0.95f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivCatHero, View.SCALE_Y, 1.0f, 1.15f, 0.95f, 1.0f)
        val translationY = ObjectAnimator.ofFloat(binding.ivCatHero, View.TRANSLATION_Y, 0f, -16f, 0f)

        val set = AnimatorSet().apply {
            playTogether(scaleX, scaleY, translationY)
            duration = 400
            interpolator = OvershootInterpolator(1.5f)
        }
        set.start()

        // Speech Bubble Pop
        val bubbleScaleX = ObjectAnimator.ofFloat(binding.layoutSpeechBubble, View.SCALE_X, 0.8f, 1.08f, 1.0f)
        val bubbleScaleY = ObjectAnimator.ofFloat(binding.layoutSpeechBubble, View.SCALE_Y, 0.8f, 1.08f, 1.0f)
        AnimatorSet().apply {
            playTogether(bubbleScaleX, bubbleScaleY)
            duration = 300
            interpolator = OvershootInterpolator(1.2f)
        }.start()
    }
}
