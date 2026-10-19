package com.example.cozypuzzle.presentation.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemShopBinding
import com.example.cozypuzzle.domain.model.ItemRarity
import com.example.cozypuzzle.domain.model.ShopItem

class ShopItemAdapter(
    private val onItemClick: (itemId: String) -> Unit
) : ListAdapter<ShopItem, ShopItemAdapter.ShopItemViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopItemViewHolder =
        ShopItemViewHolder(
            ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClick
        )

    override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ShopItemViewHolder(
        private val binding: ItemShopBinding,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            val context = binding.root.context
            binding.itemEmoji.text = item.emoji
            binding.itemName.text = item.name
            binding.itemRarity.setText(item.rarity.labelRes())
            binding.itemRarity.setTextColor(
                ContextCompat.getColor(context, item.rarity.colorRes())
            )

            if (item.isOwned) {
                binding.btnBuy.isEnabled = false
                binding.btnBuy.setText(R.string.shop_owned)
            } else {
                binding.btnBuy.isEnabled = true
                binding.btnBuy.text =
                    context.getString(R.string.shop_price_format, item.priceCoins)
            }
            binding.root.setOnClickListener { onItemClick(item.id) }
        }

        private fun ItemRarity.labelRes(): Int = when (this) {
            ItemRarity.COMMON -> R.string.rarity_common
            ItemRarity.RARE -> R.string.rarity_rare
            ItemRarity.EPIC -> R.string.rarity_epic
            ItemRarity.LEGENDARY -> R.string.rarity_legendary
        }

        private fun ItemRarity.colorRes(): Int = when (this) {
            ItemRarity.COMMON -> R.color.cozy_green_dark
            ItemRarity.RARE -> R.color.cozy_sky
            ItemRarity.EPIC -> R.color.cozy_blossom
            ItemRarity.LEGENDARY -> R.color.cozy_sun
        }
    }

    private companion object {

        val DIFF = object : DiffUtil.ItemCallback<ShopItem>() {
            override fun areItemsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ShopItem, newItem: ShopItem): Boolean =
                oldItem == newItem
        }
    }
}
