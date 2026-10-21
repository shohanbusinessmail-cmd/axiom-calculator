package com.axiom.calculator.data.repository

import com.axiom.calculator.core.util.AxClock
import com.axiom.calculator.data.db.HistoryDao
import com.axiom.calculator.data.db.HistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Owns calculation history persistence and the automatic retention cleanup
 * rule (LIB-002): an entry expires when
 * `createdAt < currentTime - configuredRetentionDuration`.
 * [RetentionPolicy.NEVER] disables age-based expiry but never disables
 * manual/bulk deletion, which remains available through [delete]/[clearAll].
 */
class HistoryRepository(
    private val dao: HistoryDao,
    private val clock: AxClock
) {
    fun observeAll(): Flow<List<HistoryEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<HistoryEntity>> = dao.search(query)

    fun observeByType(type: String): Flow<List<HistoryEntity>> = dao.observeByType(type)

    /** Commits one calculation. Called ONLY on an explicit "=" / solve action, never from recomposition. */
    suspend fun record(
        input: String,
        result: String,
        calculationType: String,
        angleMode: String? = null,
        unitContext: String? = null,
        precisionMetadata: String? = null
    ): Long = dao.insert(
        HistoryEntity(
            input = input,
            result = result,
            calculationType = calculationType,
            angleMode = angleMode,
            unitContext = unitContext,
            precisionMetadata = precisionMetadata,
            createdAtEpochMillis = clock.nowEpochMillis()
        )
    )

    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteMany(ids: List<Long>) = dao.deleteByIds(ids)
    suspend fun clearAll() = dao.clearAll()
    suspend fun getById(id: Long) = dao.getById(id)

    /**
     * Deletes every entry older than the configured retention window. Returns
     * the number of rows removed. No-op (returns 0) when [policy] is
     * [RetentionPolicy.NEVER]. Call this at repository-access points (e.g.
     * app start, Library tab open) in addition to any best-effort background
     * scheduling, since Android may defer WorkManager jobs (SECTION 12).
     */
    suspend fun cleanupExpired(policy: RetentionPolicy): Int {
        val duration = policy.durationMillis ?: return 0
        val cutoff = clock.nowEpochMillis() - duration
        return dao.deleteOlderThan(cutoff)
    }
}
