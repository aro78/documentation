package de.example.todoalarm.ui

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import de.example.todoalarm.R
import de.example.todoalarm.TodoApp
import de.example.todoalarm.alarm.AlarmIntents
import de.example.todoalarm.data.Task
import de.example.todoalarm.notification.AlarmNotifications
import de.example.todoalarm.ui.theme.AlarmRed
import de.example.todoalarm.ui.theme.TodoAlarmTheme
import de.example.todoalarm.util.SnoozeDurations
import de.example.todoalarm.util.TimeFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {

    private val taskFlow = MutableStateFlow<Task?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockscreenWindow()
        handleIntent(intent)

        setContent {
            TodoAlarmTheme {
                val task by taskFlow.collectAsState()
                AlarmScreen(
                    task = task,
                    onSnooze = { ms -> snooze(ms) },
                    onAcknowledge = { acknowledge() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onBackPressed() {
        // Deliberately no-op: the only ways out are snooze or acknowledge.
    }

    private fun configureLockscreenWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService<KeyguardManager>()?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun handleIntent(intent: Intent?) {
        val taskId = intent?.getLongExtra(AlarmIntents.EXTRA_TASK_ID, -1L) ?: -1L
        if (taskId < 0) {
            finish()
            return
        }
        val app = applicationContext as TodoApp
        lifecycleScope.launch {
            taskFlow.value = app.repository.get(taskId)
        }
    }

    private fun snooze(snoozeMs: Long) {
        val task = taskFlow.value ?: return finish()
        val app = applicationContext as TodoApp
        val until = System.currentTimeMillis() + snoozeMs
        lifecycleScope.launch {
            NotificationManagerCompat.from(this@AlarmActivity)
                .cancel(AlarmNotifications.notificationId(task.id))
            app.scheduler.cancel(task.id)
            app.repository.snooze(task.id, until)
            app.scheduler.schedule(task.copy(snoozedUntil = until))
            finish()
        }
    }

    private fun acknowledge() {
        val task = taskFlow.value ?: return finish()
        val app = applicationContext as TodoApp
        lifecycleScope.launch {
            NotificationManagerCompat.from(this@AlarmActivity)
                .cancel(AlarmNotifications.notificationId(task.id))
            app.scheduler.cancel(task.id)
            app.repository.markCompleted(task.id)
            finish()
        }
    }
}

@Composable
private fun AlarmScreen(
    task: Task?,
    onSnooze: (Long) -> Unit,
    onAcknowledge: () -> Unit,
) {
    val options = remember {
        listOf(
            SnoozeDurations.ONE_MIN to R.string.snooze_1m,
            SnoozeDurations.THREE_MIN to R.string.snooze_3m,
            SnoozeDurations.TEN_MIN to R.string.snooze_10m,
            SnoozeDurations.THIRTY_MIN to R.string.snooze_30m,
            SnoozeDurations.ONE_HOUR to R.string.snooze_1h,
            SnoozeDurations.THREE_HOURS to R.string.snooze_3h,
            SnoozeDurations.ONE_DAY to R.string.snooze_1d,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlarmRed),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.alarm_title),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = task?.title ?: "...",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            val notes = task?.notes
            if (!notes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notes,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
            task?.effectiveTriggerAt?.let { ts ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = TimeFormat.format(ts),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.snooze_label),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
            ) {
                items(options) { (durationMs, labelRes) ->
                    OutlinedButton(
                        onClick = { onSnooze(durationMs) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text(stringResource(labelRes), fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAcknowledge,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AlarmRed,
                ),
            ) {
                Text(
                    stringResource(R.string.alarm_acknowledge),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
