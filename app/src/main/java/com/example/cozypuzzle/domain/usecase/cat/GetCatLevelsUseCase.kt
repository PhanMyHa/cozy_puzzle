package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.domain.model.CatLevel
import com.example.cozypuzzle.domain.repository.CatLevelRepository
import javax.inject.Inject

class GetCatLevelsUseCase @Inject constructor(
    private val catLevelRepository: CatLevelRepository
) {

    suspend operator fun invoke(): List<CatLevel> = catLevelRepository.getCatLevels()
}
