package de.example.todoalarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import de.example.todoalarm.ui.nav.AppNav
import de.example.todoalarm.ui.nav.Routes
import de.example.todoalarm.ui.theme.TodoAlarmTheme
import de.example.todoalarm.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as TodoApp
        val factory = ViewModelFactory(app.repository, app.scheduler)
        val start = if (hasAllPermissions(this)) Routes.LIST else Routes.PERMISSIONS

        setContent {
            TodoAlarmTheme {
                AppNav(factory = factory, startDestination = start)
            }
        }
    }

    private fun hasAllPermissions(context: Context): Boolean {
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val exact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<AlarmManager>()?.canScheduleExactAlarms() == true
        } else true

        val fsi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getSystemService<NotificationManager>()?.canUseFullScreenIntent() == true
        } else true

        return notif && exact && fsi
    }
}
