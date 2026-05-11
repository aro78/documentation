package de.example.todoalarm.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val pending: Flow<List<Task>> = dao.observePending()
    val completed: Flow<List<Task>> = dao.observeCompleted()

    suspend fun get(id: Long): Task? = dao.getById(id)
    suspend fun allPendingSnapshot(): List<Task> = dao.getAllPendingSnapshot()

    suspend fun insert(task: Task): Long = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun markCompleted(id: Long, ts: Long = System.currentTimeMillis()) = dao.markCompleted(id, ts)
    suspend fun snooze(id: Long, until: Long?) = dao.updateSnooze(id, until)
    suspend fun markFired(id: Long, ts: Long = System.currentTimeMillis()) = dao.updateLastFired(id, ts)
}
