package com.learvia.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.util.formatTime
import kotlinx.coroutines.launch

enum class Panel(val label: Int) {
    BOOKMARKS(R.string.panel_bookmarks), NOTES(R.string.panel_notes), MARKERS(R.string.panel_markers),
    TRANSCRIPT(R.string.panel_transcript), REVISION(R.string.panel_revision),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPanels(
    panel: Panel, onPanel: (Panel) -> Unit, classId: Long, vm: LibraryViewModel,
    positionMs: Long, onSeek: (Long) -> Unit, onDismiss: () -> Unit,
) {
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val markers by vm.markers.collectAsStateWithLifecycle()
    val myB = bookmarks.filter { it.classId == classId }
    val myN = notes.filter { it.classId == classId }
    val myM = markers.filter { it.classId == classId }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Panel.values().forEach { p ->
                    FilterChip(selected = p == panel, onClick = { onPanel(p) }, label = { Text(stringResource(p.label)) })
                }
            }
            when (panel) {
                Panel.BOOKMARKS -> if (myB.isEmpty()) Empty(R.string.empty_bookmarks) else LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(myB, key = { it.id }) { b -> Entry(formatTime(b.positionMs), b.label, { onSeek(b.positionMs) }) { vm.deleteBookmark(b.id) } }
                }
                Panel.NOTES -> if (myN.isEmpty()) Empty(R.string.empty_notes) else LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(myN, key = { it.id }) { n ->
                        Entry(n.positionMs?.let { formatTime(it) }, n.text, { n.positionMs?.let(onSeek) }) { vm.deleteNote(n.id) }
                    }
                }
                Panel.MARKERS -> if (myM.isEmpty()) Empty(R.string.empty_markers) else LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(myM, key = { it.id }) { m -> Entry(formatTime(m.positionMs), m.label, { onSeek(m.positionMs) }) { vm.deleteMarker(m.id) } }
                }
                Panel.REVISION -> {
                    // Rule-based (TBD-15 assumption): every point you marked, in video order. No generated content.
                    data class Pt(val pos: Long, val kind: Int, val text: String)
                    val pts = (myB.map { Pt(it.positionMs, R.string.kind_bookmark, it.label) } +
                        myN.filter { it.positionMs != null }.map { Pt(it.positionMs!!, R.string.kind_note, it.text) } +
                        myM.map { Pt(it.positionMs, R.string.kind_marker, it.label) }).sortedBy { it.pos }
                    if (pts.isEmpty()) Empty(R.string.empty_revision) else LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(pts) { p -> Entry(formatTime(p.pos), stringResource(p.kind) + ": " + p.text, { onSeek(p.pos) }, null) }
                    }
                }
                Panel.TRANSCRIPT -> TranscriptTab(classId, vm, positionMs, onSeek)
            }
        }
    }
}

@Composable
private fun Empty(res: Int) {
    Text(stringResource(res), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun Entry(time: String?, text: String, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (time != null) Text(time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
    }
}

@Composable
private fun TranscriptTab(classId: Long, vm: LibraryViewModel, positionMs: Long, onSeek: (Long) -> Unit) {
    val lines by vm.transcriptFor(classId).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<Int?>(null) }
    // Transcripts come only from a file the user imports (.srt / .vtt). Nothing is generated.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val n = vm.importTranscript(classId, uri)
            status = if (n > 0) R.string.transcript_imported else R.string.transcript_unreadable
        }
    }
    if (lines.isEmpty()) {
        Empty(R.string.transcript_none)
        Button(onClick = { picker.launch(arrayOf("*/*")) }) { Text(stringResource(R.string.transcript_import)) }
    } else {
        OutlinedTextField(query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search)) })
        val shown = if (query.isBlank()) lines else lines.filter { it.text.contains(query, ignoreCase = true) }
        if (shown.isEmpty()) Empty(R.string.no_results) else LazyColumn(Modifier.heightIn(max = 360.dp)) {
            items(shown, key = { it.id }) { l ->
                val current = positionMs in l.startMs until l.endMs
                Row(Modifier.fillMaxWidth().clickable { onSeek(l.startMs) }.padding(vertical = 6.dp)) {
                    Text(formatTime(l.startMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                    Text(
                        l.text, style = MaterialTheme.typography.bodyMedium,
                        color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Row {
            TextButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text(stringResource(R.string.transcript_import)) }
            TextButton(onClick = { vm.clearTranscript(classId) }) { Text(stringResource(R.string.transcript_remove)) }
        }
    }
    status?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
