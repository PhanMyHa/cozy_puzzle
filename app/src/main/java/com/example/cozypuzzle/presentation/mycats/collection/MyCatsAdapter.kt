package com.example.cozypuzzle.presentation.mycats.collection

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemMyCatBinding

class MyCatsAdapter(
    private val onCatClicked: (catId: String) -> Unit,
    private val onLockedCatClicked: (item: CatItemUiModel) -> Unit
) : ListAdapter<CatItemUiModel, MyCatsAdapter.CatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val binding = ItemMyCatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CatViewHolder(
        private val binding: ItemMyCatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatItemUiModel) {
            val context = binding.root.context
            binding.ivCatThumb.setImageResource(item.cat.imageRes)

            // Rarity Badge styling
            binding.tvCatRarity.text = context.getString(item.cat.rarity.titleRes)
            binding.tvCatRarity.setTextColor(ContextCompat.getColor(context, item.cat.rarity.colorRes))
            binding.tvCatRarity.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, item.cat.rarity.bgRes)
            )

            if (item.isOwned) {
                binding.cardCat.alpha = 1.0f
                binding.layoutLockedOverlay.visibility = View.GONE
                binding.tvCatName.text = item.cat.name
                binding.layoutOwnedInfo.visibility = View.VISIBLE
                binding.tvCatUnlockHint.visibility = View.GONE

                binding.cardCat.setOnClickListener {
                    onCatClicked(item.cat.id)
                }
            } else {
                binding.cardCat.alpha = 0.85f
                binding.layoutLockedOverlay.visibility = View.VISIBLE
                binding.tvCatName.text = context.getString(R.string.my_cats_locked_name)
                binding.layoutOwnedInfo.visibility = View.GONE
                binding.tvCatUnlockHint.visibility = View.VISIBLE
                binding.tvCatUnlockHint.text = item.cat.unlockHint

                binding.cardCat.setOnClickListener {
                    onLockedCatClicked(item)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CatItemUiModel>() {
        override fun areItemsTheSame(oldItem: CatItemUiModel, newItem: CatItemUiModel): Boolean =
            oldItem.cat.id == newItem.cat.id

        override fun areContentsTheSame(oldItem: CatItemUiModel, newItem: CatItemUiModel): Boolean =
            oldItem == newItem
    }
}
