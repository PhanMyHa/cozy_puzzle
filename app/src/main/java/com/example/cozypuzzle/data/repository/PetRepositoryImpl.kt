package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.local.dao.PetDao
import com.example.cozypuzzle.data.local.entity.PetEntity
import com.example.cozypuzzle.domain.model.Pet
import com.example.cozypuzzle.domain.model.PetSpecies
import com.example.cozypuzzle.domain.repository.PetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {

    override fun observePets(): Flow<List<Pet>> =
        petDao.observeAll().map { pets -> pets.map { it.toDomain() } }
}

private fun PetEntity.toDomain(): Pet = Pet(
    id = id,
    name = name,
    level = level,
    experience = experience,
    species = PetSpecies.entries.firstOrNull { it.name == species } ?: PetSpecies.CAT
)

@Module
@InstallIn(SingletonComponent::class)
abstract class PetRepositoryModule {

    @Binds
    abstract fun bindPetRepository(impl: PetRepositoryImpl): PetRepository
}
