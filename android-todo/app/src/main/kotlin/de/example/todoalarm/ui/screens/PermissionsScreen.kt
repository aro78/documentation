package de.example.todoalarm.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import de.example.todoalarm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    var notifGranted by remember { mutableStateOf(checkNotifGranted(context)) }
    var exactGranted by remember { mutableStateOf(checkExactGranted(context)) }
    var fsiGranted by remember { mutableStateOf(checkFsiGranted(context)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = it }

    fun refresh() {
        notifGranted = checkNotifGranted(context)
        exactGranted = checkExactGranted(context)
        fsiGranted = checkFsiGranted(context)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.perm_title)) }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.perm_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
            PermissionRow(
                label = stringResource(R.string.perm_notif),
                granted = notifGranted,
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            PermissionRow(
                label = stringResource(R.string.perm_exact),
                granted = exactGranted,
            ) { openExactAlarmSettings(context) }
            PermissionRow(
                label = stringResource(R.string.perm_fsi),
                granted = fsiGranted,
            ) { openFsiSettings(context) }
            PermissionRow(
                label = stringResource(R.string.perm_battery),
                granted = false,
            ) { openBatteryOptimizationSettings(context) }
            OutlinedButton(
                onClick = { refresh() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.perm_refresh))
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.perm_continue))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    granted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "$label - " + stringResource(
                if (granted) R.string.perm_granted else R.string.perm_missing
            ),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (!granted) {
            OutlinedButton(onClick = onClick) { Text(stringResource(R.string.perm_open)) }
        }
    }
}

private fun checkNotifGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkExactGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return context.getSystemService<AlarmManager>()?.canScheduleExactAlarms() == true
}

private fun checkFsiGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
    return context.getSystemService<NotificationManager>()?.canUseFullScreenIntent() == true
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun openFsiSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
