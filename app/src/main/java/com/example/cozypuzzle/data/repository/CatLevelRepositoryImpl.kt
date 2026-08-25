package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.datasource.BundledCatLevelDataSource
import com.example.cozypuzzle.data.datasource.LocalCatLevelDataSource
import com.example.cozypuzzle.domain.model.CatLevel
import com.example.cozypuzzle.domain.repository.CatLevelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatLevelRepositoryImpl @Inject constructor(
    private val localDataSource: LocalCatLevelDataSource
) : CatLevelRepository {

    override suspend fun getCatLevels(): List<CatLevel> = localDataSource.getLevels()

    override suspend fun getCatLevel(levelId: String): CatLevel? =
        localDataSource.getLevel(levelId)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CatLevelRepositoryModule {

    @Binds
    abstract fun bindCatLevelRepository(impl: CatLevelRepositoryImpl): CatLevelRepository

    @Binds
    abstract fun bindLocalCatLevelDataSource(
        impl: BundledCatLevelDataSource
    ): LocalCatLevelDataSource
}
