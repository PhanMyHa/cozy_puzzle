package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.engine.CatBoardEngine
import com.example.cozypuzzle.core.path.model.CatBoard
import javax.inject.Inject

class RotateCatTileUseCase @Inject constructor(
    private val engine: CatBoardEngine
) {

    operator fun invoke(board: CatBoard, cell: Int): CatBoard =
        engine.rotate(board, cell)
}
