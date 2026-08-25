package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.engine.CatBoardEngine
import com.example.cozypuzzle.core.path.model.CatBoard
import javax.inject.Inject

class SwapCatTilesUseCase @Inject constructor(
    private val engine: CatBoardEngine
) {

    operator fun invoke(board: CatBoard, firstCell: Int, secondCell: Int): CatBoard =
        engine.swap(board, firstCell, secondCell)
}
