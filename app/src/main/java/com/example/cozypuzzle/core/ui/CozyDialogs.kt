package com.example.cozypuzzle.core.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.DialogCozyConfirmBinding
import com.example.cozypuzzle.databinding.DialogCozyGameOverBinding
import com.example.cozypuzzle.databinding.DialogCozyLevelCompleteBinding
import com.example.cozypuzzle.databinding.DialogCozyLockedBinding
import com.example.cozypuzzle.databinding.DialogCozyPauseBinding
import com.example.cozypuzzle.databinding.DialogEditNicknameBinding
import com.example.cozypuzzle.databinding.DialogNewCatUnlockedBinding
import com.example.cozypuzzle.databinding.DialogRewardClaimedBinding
import com.example.cozypuzzle.domain.model.mycats.CatModel

object CozyDialogs {

    private fun createCozyDialog(
        context: Context,
        contentView: View,
        onCancel: (() -> Unit)? = null
    ): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(contentView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        if (onCancel != null) {
            dialog.setOnCancelListener { onCancel() }
        }

        val applyWindowDimensions = {
            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels
            val density = metrics.density
            val minWidth = (300 * density).toInt()
            val maxWidth = (390 * density).toInt()
            val calculatedWidth = (screenWidth * 0.90f).toInt()
            val targetWidth = calculatedWidth.coerceIn(minWidth.coerceAtMost(screenWidth), maxWidth)

            dialog.window?.setLayout(targetWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setGravity(android.view.Gravity.CENTER)
        }

        applyWindowDimensions()
        dialog.setOnShowListener {
            applyWindowDimensions()
        }
        return dialog
    }

    fun showPauseDialog(
        context: Context,
        title: String? = null,
        onResume: () -> Unit,
        onRestart: () -> Unit,
        onExit: () -> Unit
    ): Dialog {
        val binding = DialogCozyPauseBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onResume)

        if (!title.isNullOrBlank()) {
            binding.tvPauseTitle.text = title
        }

        binding.btnPauseResume.setOnClickListener {
            dialog.dismiss()
            onResume()
        }
        binding.btnPauseRestart.setOnClickListener {
            dialog.dismiss()
            onRestart()
        }
        binding.btnPauseExit.setOnClickListener {
            dialog.dismiss()
            onExit()
        }

        dialog.show()
        return dialog
    }

    fun showLockedDialog(
        context: Context,
        levelNumber: Int,
        thumbnailRes: Int = R.drawable.img_puzzle_cute_cat,
        message: String? = null,
        onDismiss: () -> Unit = {}
    ): Dialog {
        val binding = DialogCozyLockedBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onDismiss)

        binding.tvLockedLevelTitle.text = context.getString(R.string.cat_puzzle_level_format, levelNumber)
        binding.ivLockedThumb.setImageResource(thumbnailRes)
        if (!message.isNullOrBlank()) {
            binding.tvLockedMessage.text = message
        }

        binding.btnDismissLocked.setOnClickListener {
            dialog.dismiss()
            onDismiss()
        }

        dialog.show()
        return dialog
    }

    data class StatSlot(
        val value: String,
        val label: String
    )

    fun showLevelCompleteDialog(
        context: Context,
        stars: Int,
        stat1: StatSlot,
        stat2: StatSlot,
        stat3: StatSlot,
        speechText: String? = null,
        nextLevelId: String? = null,
        onNextLevel: () -> Unit = {},
        onBackToLevels: () -> Unit
    ): Dialog {
        val binding = DialogCozyLevelCompleteBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root)

        binding.ivCompleteStar1.setImageResource(
            if (stars >= 1) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
        )
        binding.ivCompleteStar2.setImageResource(
            if (stars >= 2) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
        )
        binding.ivCompleteStar3.setImageResource(
            if (stars >= 3) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
        )

        binding.tvStat1Val.text = stat1.value
        binding.tvStat1Label.text = stat1.label

        binding.tvStat2Val.text = stat2.value
        binding.tvStat2Label.text = stat2.label

        binding.tvStat3Val.text = stat3.value
        binding.tvStat3Label.text = stat3.label

        if (!speechText.isNullOrBlank()) {
            binding.tvCompleteSpeech.text = speechText
        }

        if (nextLevelId != null) {
            binding.btnCompleteNext.visibility = View.VISIBLE
            binding.btnCompleteNext.setOnClickListener {
                dialog.dismiss()
                onNextLevel()
            }
        } else {
            binding.btnCompleteNext.visibility = View.GONE
        }

        binding.btnCompleteBackLevels.setOnClickListener {
            dialog.dismiss()
            onBackToLevels()
        }

        dialog.show()
        return dialog
    }

    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String = context.getString(android.R.string.cancel),
        onPositive: () -> Unit,
        onNegative: () -> Unit = {}
    ): Dialog {
        val binding = DialogCozyConfirmBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onNegative)

        binding.tvConfirmTitle.text = title
        binding.tvConfirmMessage.text = message
        binding.btnConfirmPositive.text = positiveButtonText
        binding.btnConfirmNegative.text = negativeButtonText

        binding.btnConfirmPositive.setOnClickListener {
            dialog.dismiss()
            onPositive()
        }
        binding.btnConfirmNegative.setOnClickListener {
            dialog.dismiss()
            onNegative()
        }

        dialog.show()
        return dialog
    }

    fun showGameOverDialog(
        context: Context,
        title: String? = null,
        speechText: String? = null,
        onRetry: () -> Unit,
        onQuit: () -> Unit
    ): Dialog {
        val binding = DialogCozyGameOverBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onQuit)

        if (!title.isNullOrBlank()) {
            binding.tvGameOverTitle.text = title
        }
        if (!speechText.isNullOrBlank()) {
            binding.tvGameOverSpeech.text = speechText
        }

        binding.btnGameOverRetry.setOnClickListener {
            dialog.dismiss()
            onRetry()
        }
        binding.btnGameOverQuit.setOnClickListener {
            dialog.dismiss()
            onQuit()
        }

        dialog.show()
        return dialog
    }

    fun showNewCatUnlockedDialog(
        context: Context,
        cat: CatModel,
        onDismiss: () -> Unit = {}
    ): Dialog {
        val binding = DialogNewCatUnlockedBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onDismiss)

        binding.ivUnlockedCat.setImageResource(cat.imageRes)
        binding.tvUnlockedName.text = cat.name
        binding.tvUnlockedDesc.text = cat.description
        binding.tvUnlockedCondition.text = cat.unlockHint

        binding.tvUnlockedRarity.text = context.getString(cat.rarity.titleRes)
        binding.tvUnlockedRarity.setTextColor(ContextCompat.getColor(context, cat.rarity.colorRes))
        binding.tvUnlockedRarity.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, cat.rarity.bgRes)
        )

        binding.btnUnlockGotIt.setOnClickListener {
            dialog.dismiss()
            onDismiss()
        }

        dialog.show()
        return dialog
    }

    fun showRewardClaimedDialog(
        context: Context,
        amount: Int,
        title: String? = null,
        onDismiss: () -> Unit = {}
    ): Dialog {
        val binding = DialogRewardClaimedBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root, onCancel = onDismiss)

        if (!title.isNullOrBlank()) {
            binding.tvRewardTitle.text = title
        }
        binding.tvRewardAmount.text = context.getString(R.string.daily_reward_dialog_coins_format, amount)

        binding.btnRewardCollect.setOnClickListener {
            dialog.dismiss()
            onDismiss()
        }

        dialog.show()
        return dialog
    }

    fun showEditNicknameDialog(
        context: Context,
        currentName: String,
        onSave: (String) -> Unit
    ): Dialog {
        val binding = DialogEditNicknameBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root)

        binding.etNickname.setText(currentName)
        binding.etNickname.setSelection(currentName.length)

        binding.btnNicknameCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnNicknameSave.setOnClickListener {
            val name = binding.etNickname.text?.toString() ?: ""
            if (name.trim().isNotEmpty()) {
                dialog.dismiss()
                onSave(name)
            } else {
                binding.tilNickname.error = context.getString(R.string.profile_name_invalid)
            }
        }

        dialog.show()
        return dialog
    }

    fun showInfoDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = context.getString(android.R.string.ok)
    ): Dialog {
        val binding = DialogCozyConfirmBinding.inflate(LayoutInflater.from(context))
        val dialog = createCozyDialog(context, binding.root)

        binding.tvConfirmTitle.text = title
        binding.tvConfirmMessage.text = message
        binding.btnConfirmPositive.text = buttonText
        binding.btnConfirmNegative.visibility = View.GONE

        binding.btnConfirmPositive.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        return dialog
    }
}
