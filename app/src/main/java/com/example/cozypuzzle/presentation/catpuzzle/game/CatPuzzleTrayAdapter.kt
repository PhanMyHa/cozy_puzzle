package com.example.cozypuzzle.presentation.catpuzzle.game

import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.catpuzzle.model.CatPiece
import com.example.cozypuzzle.databinding.ItemCatPuzzlePieceBinding

class CatPuzzleTrayAdapter(
    private val onPieceClicked: (pieceId: Int) -> Unit
) : ListAdapter<CatPiece, CatPuzzleTrayAdapter.PieceViewHolder>(DIFF_CALLBACK) {

    private var boardBitmap: Bitmap? = null
    private var selectedPieceId: Int? = null

    fun setBoardBitmap(bitmap: Bitmap?) {
        this.boardBitmap = bitmap
        notifyDataSetChanged()
    }

    fun setSelectedPieceId(id: Int?) {
        this.selectedPieceId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PieceViewHolder {
        val binding = ItemCatPuzzlePieceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PieceViewHolder(binding, onPieceClicked)
    }

    override fun onBindViewHolder(holder: PieceViewHolder, position: Int) {
        holder.bind(getItem(position), boardBitmap, selectedPieceId)
    }

    class PieceViewHolder(
        private val binding: ItemCatPuzzlePieceBinding,
        private val onPieceClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var downX = 0f
        private var downY = 0f
        private var isDragging = false
        private val touchSlop = ViewConfiguration.get(binding.root.context).scaledTouchSlop.toFloat()

        @SuppressLint("ClickableViewAccessibility")
        fun bind(piece: CatPiece, fullBitmap: Bitmap?, selectedPieceId: Int?) {
            val context = binding.root.context
            val isSelected = piece.id == selectedPieceId

            val strokeColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.cozy_pastel_pink_dark)
            } else {
                ContextCompat.getColor(context, R.color.cozy_card_stroke)
            }
            binding.cardPiece.strokeColor = strokeColor
            binding.cardPiece.strokeWidth = if (isSelected) 6 else 3

            var currentPieceBitmap: Bitmap? = null
            if (fullBitmap != null) {
                val pieceW = fullBitmap.width / 3
                val pieceH = fullBitmap.height / 3
                val pieceCol = piece.id % 3
                val pieceRow = piece.id / 3
                val srcRect = Rect(
                    pieceCol * pieceW,
                    pieceRow * pieceH,
                    (pieceCol + 1) * pieceW,
                    (pieceRow + 1) * pieceH
                )
                val pieceBitmap = Bitmap.createBitmap(pieceW, pieceH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(pieceBitmap)
                val destRect = Rect(0, 0, pieceW, pieceH)
                canvas.drawBitmap(fullBitmap, srcRect, destRect, null)
                currentPieceBitmap = pieceBitmap
                binding.ivPieceImage.setImageBitmap(pieceBitmap)
            }

            fun startPieceDrag(view: View): Boolean {
                val clipData = ClipData.newPlainText("cat_puzzle_piece", "TRAY:${piece.id}")
                val sizePx = view.width.takeIf { it > 0 } ?: (80 * context.resources.displayMetrics.density).toInt()
                val shadow = com.example.cozypuzzle.core.customview.PieceDragShadowBuilder(view, currentPieceBitmap, sizePx)
                return view.startDragAndDrop(clipData, shadow, null, 0)
            }

            binding.root.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        isDragging = false
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dist = kotlin.math.hypot(event.x - downX, event.y - downY)
                        if (!isDragging && dist > touchSlop) {
                            isDragging = true
                            startPieceDrag(view)
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                        if (!isDragging) {
                            view.performClick()
                        }
                        false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                        isDragging = false
                        false
                    }
                    else -> false
                }
            }

            binding.root.setOnClickListener {
                onPieceClicked(piece.id)
            }

            binding.root.setOnLongClickListener { view ->
                startPieceDrag(view)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CatPiece>() {
            override fun areItemsTheSame(oldItem: CatPiece, newItem: CatPiece): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CatPiece, newItem: CatPiece): Boolean =
                oldItem == newItem
        }
    }
}
