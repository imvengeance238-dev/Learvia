package com.learvia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Everything here is derived from recorded events: watch sessions (foreground playback only) and finished focus sessions. */
@Composable
fun StatsScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    val history by vm.history.collectAsStateWithLifecycle()
    val focus by vm.focus.collectAsStateWithLifecycle()
    val classes by vm.classes.collectAsStateWithLifecycle()
    val zone = ZoneId.systemDefault()
    fun dayOf(ms: Long) = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate().toEpochDay()

    val minutesByDay = remember(history, focus) {
        (history.map { dayOf(it.startedAt) to it.watchedMs } + focus.map { dayOf(it.startedAt) to it.durationMs })
            .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() / 60_000 }
    }
    val today = LocalDate.now().toEpochDay()
    val streak = remember(minutesByDay) {
        var d = if ((minutesByDay[today] ?: 0) > 0) today else today - 1
        var n = 0
        while ((minutesByDay[d] ?: 0) > 0) { n++; d-- }
        n
    }
    val watchMin = history.sumOf { it.watchedMs } / 60_000
    val focusMin = focus.sumOf { it.durationMs } / 60_000

    ScreenScaffold(stringResource(R.string.statistics), onBack) { pad ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(contentPadding(pad)), verticalArrangement = spacedBy(12.dp)) {
            InfoRow(stringResource(R.string.stat_watch_time), stringResource(R.string.minutes_fmt, watchMin), enabled = false) {}
            InfoRow(stringResource(R.string.stat_focus_time), stringResource(R.string.minutes_fmt, focusMin), enabled = false) {}
            InfoRow(stringResource(R.string.stat_sessions), history.size.toString(), enabled = false) {}
            InfoRow(stringResource(R.string.stat_completed), classes.count { it.completed }.toString(), enabled = false) {}
            InfoRow(stringResource(R.string.stat_streak), stringResource(R.string.days_fmt, streak), enabled = false) {}

            SectionTitle(stringResource(R.string.heatmap))
            Heatmap(minutesByDay, today)
            Text(stringResource(R.string.heatmap_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 17 weeks x 7 days, Monday on top, newest week on the right. Level thresholds are placeholders (TBD). */
@Composable
private fun Heatmap(minutesByDay: Map<Long, Long>, today: Long) {
    val weeks = 17
    val dow = LocalDate.ofEpochDay(today).dayOfWeek.value // 1 = Monday
    val start = today - ((weeks - 1) * 7 + (dow - 1))
    val base = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = spacedBy(3.dp)) {
        for (w in 0 until weeks) {
            Column(verticalArrangement = spacedBy(3.dp)) {
                for (d in 0 until 7) {
                    val day = start + w * 7 + d
                    val m = minutesByDay[day] ?: 0
                    val color = when {
                        day > today -> androidx.compose.ui.graphics.Color.Transparent
                        m <= 0 -> empty
                        m < 15 -> base.copy(alpha = 0.30f)
                        m < 30 -> base.copy(alpha = 0.55f)
                        m < 60 -> base.copy(alpha = 0.80f)
                        else -> base
                    }
                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(color))
                }
            }
        }
    }
}
