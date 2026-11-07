package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import java.time.LocalDate
import java.time.ZoneId

/** Pomodoro-style focus timer. Presets are placeholders (TBD); the timer runs while the app process is alive. */
@Composable
fun FocusScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    val ui by vm.focusUi.collectAsStateWithLifecycle()
    val sessions by vm.focus.collectAsStateWithLifecycle()
    val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val todays = sessions.filter { it.startedAt >= startOfToday }
    val secs = ui.remainingMs / 1000

    ScreenScaffold(stringResource(R.string.focus), onBack) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(if (ui.isBreak) R.string.focus_break else R.string.focus_work), style = MaterialTheme.typography.titleMedium)
            Text("%02d:%02d".format(secs / 60, secs % 60), style = MaterialTheme.typography.displayLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (ui.running) Button(onClick = { vm.focusPause() }) { Text(stringResource(R.string.pause)) }
                else Button(onClick = { vm.focusStart() }) { Text(stringResource(R.string.start)) }
                OutlinedButton(onClick = { vm.focusReset() }) { Text(stringResource(R.string.reset)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = ui.workMin == 25, enabled = !ui.running, onClick = { vm.focusReset(25, 5) }, label = { Text("25 / 5") })
                FilterChip(selected = ui.workMin == 50, enabled = !ui.running, onClick = { vm.focusReset(50, 10) }, label = { Text("50 / 10") })
            }
            Text(stringResource(R.string.focus_today, todays.size, todays.sumOf { it.durationMs } / 60_000))
        }
    }
}
