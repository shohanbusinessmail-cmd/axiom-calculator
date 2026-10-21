package com.axiom.calculator.data.repository

import com.axiom.calculator.core.util.AxClock
import com.axiom.calculator.data.db.SavedCalculationDao
import com.axiom.calculator.data.db.SavedCalculationEntity
import kotlinx.coroutines.flow.Flow

class SavedCalculationRepository(
    private val dao: SavedCalculationDao,
    private val clock: AxClock
) {
    fun observeAll(): Flow<List<SavedCalculationEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<SavedCalculationEntity>> = dao.observeFavorites()
    fun search(query: String): Flow<List<SavedCalculationEntity>> = dao.search(query)

    suspend fun save(label: String, input: String, result: String, calculationType: String): Long {
        val now = clock.nowEpochMillis()
        return dao.insert(
            SavedCalculationEntity(
                label = label, input = input, result = result, calculationType = calculationType,
                createdAtEpochMillis = now, updatedAtEpochMillis = now
            )
        )
    }

    suspend fun rename(entity: SavedCalculationEntity, newLabel: String) =
        dao.update(entity.copy(label = newLabel, updatedAtEpochMillis = clock.nowEpochMillis()))

    suspend fun toggleFavorite(entity: SavedCalculationEntity) =
        dao.update(entity.copy(isFavorite = !entity.isFavorite, updatedAtEpochMillis = clock.nowEpochMillis()))

    suspend fun togglePinned(entity: SavedCalculationEntity) =
        dao.update(entity.copy(isPinned = !entity.isPinned, updatedAtEpochMillis = clock.nowEpochMillis()))

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteMany(ids: List<Long>) = dao.deleteByIds(ids)
}
