package com.learvia.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Simple dated task list. Tasks are free text; calendar integration is not used (TBD-05 assumption). */
@Composable
fun PlannerScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    val plans by vm.plans.collectAsStateWithLifecycle()
    val today = LocalDate.now().toEpochDay()
    var adding by remember { mutableStateOf(false) }

    ScreenScaffold(
        stringResource(R.string.planner), onBack,
        fab = { FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, stringResource(R.string.add_plan)) } },
    ) { pad ->
        if (plans.isEmpty()) CenterMessage(null, stringResource(R.string.planner_empty), pad)
        else LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(8.dp)) {
            items(plans, key = { it.id }) { p ->
                val overdue = !p.done && p.epochDay < today
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(p.done, { vm.setPlanDone(p.id, it) })
                        Column(Modifier.weight(1f)) {
                            Text(p.title, style = MaterialTheme.typography.titleMedium, textDecoration = if (p.done) TextDecoration.LineThrough else null)
                            val label = when (p.epochDay) {
                                today -> stringResource(R.string.today)
                                today + 1 -> stringResource(R.string.tomorrow)
                                else -> LocalDate.ofEpochDay(p.epochDay).toString()
                            }
                            Text(
                                if (overdue) label + " · " + stringResource(R.string.overdue) else label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { vm.deletePlan(p.id) }) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }

    if (adding) {
        var title by remember { mutableStateOf("") }
        var offset by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.add_plan)) },
            text = {
                Column(verticalArrangement = spacedBy(12.dp)) {
                    OutlinedTextField(title, { title = it }, singleLine = true, label = { Text(stringResource(R.string.title_label)) })
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = spacedBy(8.dp)) {
                        (0..6).forEach { d ->
                            val date = LocalDate.now().plusDays(d.toLong())
                            val label = when (d) {
                                0 -> stringResource(R.string.today)
                                1 -> stringResource(R.string.tomorrow)
                                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            }
                            FilterChip(selected = offset == d, onClick = { offset = d }, label = { Text(label) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = title.isNotBlank(), onClick = { vm.addPlan(title, today + offset); adding = false }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
