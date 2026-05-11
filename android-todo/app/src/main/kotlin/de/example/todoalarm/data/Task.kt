package de.example.todoalarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String? = null,
    val dueAt: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val lastFiredAt: Long? = null,
    val reAlertIntervalMs: Long = DEFAULT_REALERT_INTERVAL_MS,
    val snoozedUntil: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val effectiveTriggerAt: Long?
        get() = snoozedUntil ?: dueAt

    companion object {
        const val DEFAULT_REALERT_INTERVAL_MS: Long = 3 * 60 * 1000L
    }
}
