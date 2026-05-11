package de.example.todoalarm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY COALESCE(snoozedUntil, dueAt, createdAt) ASC")
    fun observePending(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun observeCompleted(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (dueAt IS NOT NULL OR snoozedUntil IS NOT NULL)")
    suspend fun getAllPendingSnapshot(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :ts, snoozedUntil = NULL WHERE id = :id")
    suspend fun markCompleted(id: Long, ts: Long)

    @Query("UPDATE tasks SET snoozedUntil = :until WHERE id = :id")
    suspend fun updateSnooze(id: Long, until: Long?)

    @Query("UPDATE tasks SET lastFiredAt = :ts WHERE id = :id")
    suspend fun updateLastFired(id: Long, ts: Long)
}
