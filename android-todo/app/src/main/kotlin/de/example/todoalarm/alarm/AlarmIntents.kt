package de.example.todoalarm.alarm

object AlarmIntents {
    const val ACTION_FIRE = "de.example.todoalarm.action.FIRE"
    const val ACTION_SNOOZE = "de.example.todoalarm.action.SNOOZE"
    const val ACTION_ACK = "de.example.todoalarm.action.ACK"

    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_SNOOZE_MS = "snooze_ms"

    private const val REQ_FIRE_OFFSET = 0
    private const val REQ_FULLSCREEN_OFFSET = 1
    private const val REQ_ACK_OFFSET = 2
    private const val REQ_OPEN_OFFSET = 3
    private const val REQ_SNOOZE_BASE_OFFSET = 4

    private const val MULT = 16

    fun fireRequestCode(taskId: Long): Int = (taskId * MULT).toInt() + REQ_FIRE_OFFSET
    fun fullscreenRequestCode(taskId: Long): Int = (taskId * MULT).toInt() + REQ_FULLSCREEN_OFFSET
    fun ackRequestCode(taskId: Long): Int = (taskId * MULT).toInt() + REQ_ACK_OFFSET
    fun openRequestCode(taskId: Long): Int = (taskId * MULT).toInt() + REQ_OPEN_OFFSET
    fun snoozeRequestCode(taskId: Long, index: Int): Int =
        (taskId * MULT).toInt() + REQ_SNOOZE_BASE_OFFSET + index
}
