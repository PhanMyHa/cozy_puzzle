package com.example.cozypuzzle.core.path.solver

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.PathResult

interface PathFinder {

    fun findPath(board: CatBoard): PathResult
}
