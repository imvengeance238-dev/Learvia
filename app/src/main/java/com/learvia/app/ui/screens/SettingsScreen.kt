package com.learvia.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.util.Reminders
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: LibraryViewModel, onBack: () -> Unit, onNav: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var reminder by remember { mutableStateOf(vm.prefs.reminderEnabled) }
    var hour by remember { mutableIntStateOf(vm.prefs.reminderHour) }
    var message by remember { mutableStateOf<Int?>(null) }
    var confirmImport by remember { mutableStateOf<Uri?>(null) }

    fun enable() { reminder = true; vm.prefs.reminderEnabled = true; Reminders.schedule(ctx, hour) }
    fun disable() { reminder = false; vm.prefs.reminderEnabled = false; Reminders.cancel(ctx) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) enable() }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch { message = if (vm.exportBackup(uri)) R.string.backup_ok else R.string.backup_failed }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) confirmImport = uri }

    ScreenScaffold(stringResource(R.string.settings), onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(contentPadding(pad)), verticalArrangement = spacedBy(12.dp)) {
            // Management stays a secondary entry inside Settings.
            InfoRow(stringResource(R.string.management), stringResource(R.string.management_hint)) { onNav("manage") }
            InfoRow(stringResource(R.string.media_library), stringResource(R.string.media_library_hint)) { onNav("media") }
            InfoRow(stringResource(R.string.collections), stringResource(R.string.collections_hint)) { onNav("collections") }

            SectionTitle(stringResource(R.string.reminders))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(12.dp)) {
                Text(stringResource(R.string.reminder_daily), Modifier.weight(1f))
                Switch(checked = reminder, onCheckedChange = { on ->
                    if (!on) disable()
                    else if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else enable()
                })
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = spacedBy(8.dp)) {
                listOf(7, 9, 12, 18, 20, 21).forEach { h ->
                    FilterChip(selected = hour == h, onClick = {
                        hour = h; vm.prefs.reminderHour = h
                        if (reminder) Reminders.schedule(ctx, h)
                    }, label = { Text("%02d:00".format(h)) })
                }
            }

            SectionTitle(stringResource(R.string.backup_restore))
            Text(stringResource(R.string.backup_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = spacedBy(8.dp)) {
                Button(onClick = { exporter.launch("learvia-backup.json") }) { Text(stringResource(R.string.backup_export)) }
                OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "*/*")) }) { Text(stringResource(R.string.backup_import)) }
            }
            message?.let { Text(stringResource(it)) }

            InfoRow(stringResource(R.string.about), stringResource(R.string.about_body), enabled = false) {}
        }
    }

    confirmImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { confirmImport = null },
            title = { Text(stringResource(R.string.restore_title)) },
            text = { Text(stringResource(R.string.restore_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = null
                    scope.launch { message = if (vm.importBackup(uri)) R.string.restore_ok else R.string.restore_failed }
                }) { Text(stringResource(R.string.backup_import)) }
            },
            dismissButton = { TextButton(onClick = { confirmImport = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
