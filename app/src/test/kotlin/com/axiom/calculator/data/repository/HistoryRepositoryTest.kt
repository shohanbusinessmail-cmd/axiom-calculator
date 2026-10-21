package com.axiom.calculator.data.repository

import com.axiom.calculator.core.util.FixedAxClock
import com.axiom.calculator.data.db.HistoryDao
import com.axiom.calculator.data.db.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * In-memory fake DAO so repository/retention logic is unit-testable on the
 * plain JVM, without Room/Android instrumentation. This directly exercises
 * [HistoryRepository.cleanupExpired]'s documented rule:
 * `expires when createdAt < currentTime - configuredRetentionDuration`,
 * using a [FixedAxClock] instead of real wall-clock time (SECTION 19 —
 * explicitly forbids sleep-based expiration tests).
 */
class FakeHistoryDao : HistoryDao {
    private val state = MutableStateFlow<List<HistoryEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(entity: HistoryEntity): Long {
        val withId = entity.copy(id = nextId++)
        state.value = state.value + withId
        return withId.id
    }

    override fun observeAll(): Flow<List<HistoryEntity>> = state.map { it.sortedByDescending { e -> e.createdAtEpochMillis } }
    override fun search(query: String): Flow<List<HistoryEntity>> = state.map { list -> list.filter { it.input.contains(query) || it.result.contains(query) } }
    override fun observeByType(type: String): Flow<List<HistoryEntity>> = state.map { list -> list.filter { it.calculationType == type } }

    override suspend fun deleteById(id: Long) { state.value = state.value.filterNot { it.id == id } }
    override suspend fun deleteByIds(ids: List<Long>) { state.value = state.value.filterNot { it.id in ids } }
    override suspend fun clearAll() { state.value = emptyList() }
    override suspend fun deleteOlderThan(cutoffEpochMillis: Long): Int {
        val before = state.value.size
        state.value = state.value.filterNot { it.createdAtEpochMillis < cutoffEpochMillis }
        return before - state.value.size
    }
    override suspend fun getById(id: Long): HistoryEntity? = state.value.find { it.id == id }
}

class HistoryRepositoryTest {

    @Test
    fun recordedEntryIsReadableImmediately() = runBlocking {
        val clock = FixedAxClock(1_000_000L)
        val repo = HistoryRepository(FakeHistoryDao(), clock)
        repo.record(input = "2+2", result = "4", calculationType = "STANDARD")
        val captured = repo.observeAll().first()
        assertEquals(1, captured.size)
        assertEquals("2+2", captured.first().input)
    }

    @Test
    fun cleanupExpired_removesOnlyEntriesOlderThanRetentionWindow() = runBlocking {
        val clock = FixedAxClock(0L)
        val dao = FakeHistoryDao()
        val repo = HistoryRepository(dao, clock)

        repo.record("old", "1", "STANDARD") // created at t=0
        clock.advanceBy(RetentionPolicy.ONE_DAY.durationMillis!! + 1) // move past the 1-day window
        repo.record("new", "2", "STANDARD") // created at t = 1 day + 1ms

        val removed = repo.cleanupExpired(RetentionPolicy.ONE_DAY)
        assertEquals(1, removed)

        val remaining = repo.observeAll().first()
        assertEquals(1, remaining.size)
        assertEquals("new", remaining.first().input)
    }

    @Test
    fun neverPolicyDisablesAgeBasedCleanupButNotManualDelete() = runBlocking {
        val clock = FixedAxClock(0L)
        val dao = FakeHistoryDao()
        val repo = HistoryRepository(dao, clock)
        val id = repo.record("x", "1", "STANDARD")
        clock.advanceBy(10_000L * 24 * 60 * 60 * 1000) // far in the future

        val removedByCleanup = repo.cleanupExpired(RetentionPolicy.NEVER)
        assertEquals(0, removedByCleanup)

        repo.delete(id) // manual deletion must still work under NEVER
        val remaining = repo.observeAll().first()
        assertTrue(remaining.isEmpty())
    }
}
