package com.example.cozypuzzle.presentation.findpair.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.findpair.model.FindPairCard
import com.example.cozypuzzle.databinding.ItemFindPairCardBinding

class FindPairCardAdapter(
    private val onCardClicked: (FindPairCard) -> Unit
) : ListAdapter<FindPairCard, FindPairCardAdapter.CardViewHolder>(CardDiffCallback()) {

    private var recyclerView: RecyclerView? = null
    private var hintedCardIds: Set<Int> = emptySet()

    private val handler = Handler(Looper.getMainLooper())
    private val hiddenMatchedCardIds = mutableSetOf<Int>()
    private val pendingHideRunnables = mutableMapOf<Int, Runnable>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        clearPendingHideTasks()
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun submitList(list: List<FindPairCard>?) {
        val newList = list.orEmpty()
        if (newList.isNotEmpty() && newList.none { it.isMatched }) {
            clearPendingHideTasks()
            hiddenMatchedCardIds.clear()
        }
        super.submitList(list)
    }

    private fun clearPendingHideTasks() {
        for (runnable in pendingHideRunnables.values) {
            handler.removeCallbacks(runnable)
        }
        pendingHideRunnables.clear()
    }

    fun setHintedCards(hintedIds: Set<Int>) {
        val previous = hintedCardIds
        hintedCardIds = hintedIds
        val affected = (previous + hintedIds)
        for (id in affected) {
            val position = currentList.indexOfFirst { it.id == id }
            if (position != -1) {
                notifyItemChanged(position, PAYLOAD_HINT)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemFindPairCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card, hintedCardIds.contains(card.id))
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_HINT)) {
            val card = getItem(position)
            holder.bindHint(hintedCardIds.contains(card.id))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun animateFlip(cardId: Int, toFaceUp: Boolean) {
        val position = currentList.indexOfFirst { it.id == cardId }
        if (position == -1) return
        val rv = recyclerView ?: return
        val holder = rv.findViewHolderForAdapterPosition(position) as? CardViewHolder ?: return
        holder.animateFlip(toFaceUp)
    }

    fun animateMatched(cardId1: Int, cardId2: Int) {
        val rv = recyclerView ?: return
        for (id in listOf(cardId1, cardId2)) {
            val position = currentList.indexOfFirst { it.id == id }
            if (position != -1) {
                val holder = rv.findViewHolderForAdapterPosition(position) as? CardViewHolder
                holder?.animateCelebration()
            }

            // Schedule slow fade-out after MATCH_DISPLAY_DURATION_MS (3000ms)
            pendingHideRunnables[id]?.let { handler.removeCallbacks(it) }
            val hideRunnable = Runnable {
                pendingHideRunnables.remove(id)
                hiddenMatchedCardIds.add(id)
                val pos = currentList.indexOfFirst { it.id == id }
                if (pos != -1) {
                    val h = rv.findViewHolderForAdapterPosition(pos) as? CardViewHolder
                    if (h != null && h.currentCardId == id) {
                        h.animateFadeOut()
                    } else {
                        notifyItemChanged(pos)
                    }
                }
            }
            pendingHideRunnables[id] = hideRunnable
            handler.postDelayed(hideRunnable, MATCH_DISPLAY_DURATION_MS)
        }
    }

    inner class CardViewHolder(
        private val binding: ItemFindPairCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var currentCardId: Int = -1

        init {
            val scale = binding.root.resources.displayMetrics.density
            binding.cardContainer.cameraDistance = 8000 * scale
        }

        fun bind(card: FindPairCard, isHinted: Boolean) {
            currentCardId = card.id
            val context = binding.root.context

            binding.cardContainer.animate().cancel()

            if (card.isMatched) {
                if (hiddenMatchedCardIds.contains(card.id)) {
                    // Completed 3s viewing + fade-out -> remain INVISIBLE
                    binding.cardContainer.visibility = View.INVISIBLE
                    binding.cardContainer.alpha = 0f
                    binding.cardContainer.isClickable = false
                    return
                }

                // Matched, but still within the 3s viewing window!
                // Keep it fully VISIBLE and face up so user can see it!
                binding.cardContainer.visibility = View.VISIBLE
                binding.cardContainer.isClickable = false
                binding.cardContainer.alpha = 1f
                binding.cardContainer.scaleX = 1f
                binding.cardContainer.scaleY = 1f
                binding.cardContainer.rotationY = 0f

                binding.layoutCardFront.visibility = View.VISIBLE
                binding.layoutCardBack.visibility = View.GONE
                binding.ivCatIllustration.setImageResource(card.imageRes)
                binding.ivMatchedStar.visibility = View.VISIBLE
                binding.ivMatchedStar.alpha = 1f

                binding.viewHintGlow.visibility = View.GONE
                return
            }

            binding.cardContainer.visibility = View.VISIBLE
            binding.cardContainer.isClickable = true
            binding.cardContainer.alpha = 1f
            binding.cardContainer.scaleX = 1f
            binding.cardContainer.scaleY = 1f

            binding.cardContainer.setOnClickListener {
                onCardClicked(card)
            }

            // Card Back styling
            val backColorRes = if (card.isCardBackPink) {
                R.color.cozy_pastel_pink
            } else {
                R.color.cozy_pastel_blue
            }
            binding.layoutCardBack.setBackgroundColor(ContextCompat.getColor(context, backColorRes))

            // Card Front styling
            binding.ivCatIllustration.setImageResource(card.imageRes)
            binding.ivMatchedStar.visibility = View.GONE

            // Visibility
            val showFront = card.isFaceUp
            binding.layoutCardFront.visibility = if (showFront) View.VISIBLE else View.GONE
            binding.layoutCardBack.visibility = if (showFront) View.GONE else View.VISIBLE
            binding.cardContainer.rotationY = 0f

            bindHint(isHinted)
        }

        fun bindHint(isHinted: Boolean) {
            binding.viewHintGlow.visibility = if (isHinted) View.VISIBLE else View.GONE
            if (isHinted) {
                binding.viewHintGlow.alpha = 0f
                binding.viewHintGlow.animate()
                    .alpha(1f)
                    .setDuration(300L)
                    .start()
            }
        }

        fun animateFlip(toFaceUp: Boolean) {
            val cardView = binding.cardContainer
            val firstHalf = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f).apply {
                duration = 180L
                interpolator = AccelerateInterpolator()
            }
            val secondHalf = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f).apply {
                duration = 180L
                interpolator = DecelerateInterpolator()
            }

            firstHalf.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (toFaceUp) {
                        binding.layoutCardBack.visibility = View.GONE
                        binding.layoutCardFront.visibility = View.VISIBLE
                    } else {
                        binding.layoutCardFront.visibility = View.GONE
                        binding.layoutCardBack.visibility = View.VISIBLE
                    }
                    secondHalf.start()
                }
            })

            firstHalf.start()
        }

        fun animateCelebration() {
            binding.ivMatchedStar.visibility = View.VISIBLE
            binding.ivMatchedStar.alpha = 1f

            binding.cardContainer.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(220L)
                .setInterpolator(OvershootInterpolator(2f))
                .withEndAction {
                    binding.cardContainer.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(180L)
                        .start()
                }
                .start()
        }

        fun animateFadeOut() {
            binding.cardContainer.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .alpha(0f)
                .setDuration(SLOW_FADE_DURATION_MS)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    binding.cardContainer.visibility = View.INVISIBLE
                    binding.cardContainer.isClickable = false
                }
                .start()
        }
    }

    private class CardDiffCallback : DiffUtil.ItemCallback<FindPairCard>() {
        override fun areItemsTheSame(oldItem: FindPairCard, newItem: FindPairCard): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FindPairCard, newItem: FindPairCard): Boolean =
            oldItem == newItem
    }

    companion object {
        private const val PAYLOAD_HINT = "payload_hint"
        private const val MATCH_DISPLAY_DURATION_MS = 3000L
        private const val SLOW_FADE_DURATION_MS = 800L
    }
}
