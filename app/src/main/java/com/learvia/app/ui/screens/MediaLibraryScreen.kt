package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.ui.MediaInfo
import com.learvia.app.ui.Volume
import com.learvia.app.util.formatBytes

/**
 * Smart Media Library + Storage Intelligence. Only inspects files the user already assigned.
 * Duplicate rule (TBD-16 assumption): same file name and same size. Nothing is ever deleted or moved here.
 */
@Composable
fun MediaLibraryScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    var items by remember { mutableStateOf<List<MediaInfo>?>(null) }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(tick) { items = null; items = vm.scanMedia() }

    ScreenScaffold(stringResource(R.string.media_library), onBack) { pad ->
        val list = items
        if (list == null) {
            Column(Modifier.padding(pad).padding(32.dp).fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally)) }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(contentPadding(pad)), verticalArrangement = spacedBy(12.dp)) {
                OutlinedButton(onClick = { tick++ }) { Text(stringResource(R.string.rescan)) }
                if (list.isEmpty()) Text(stringResource(R.string.media_none))
                SectionTitle(stringResource(R.string.storage_summary))
                fun sum(v: Volume) = list.filter { it.volume == v && it.sizeBytes >= 0 }.sumOf { it.sizeBytes }
                InfoRow(stringResource(R.string.volume_internal), formatBytes(sum(Volume.INTERNAL)), enabled = false) {}
                InfoRow(stringResource(R.string.volume_sd), formatBytes(sum(Volume.SD)), enabled = false) {}
                InfoRow(stringResource(R.string.volume_other), formatBytes(sum(Volume.OTHER)), enabled = false) {}

                SectionTitle(stringResource(R.string.per_course))
                list.groupBy { it.courseId }.forEach { (_, g) ->
                    InfoRow(g.first().courseTitle, formatBytes(g.filter { it.sizeBytes >= 0 }.sumOf { it.sizeBytes }) + " · " + g.size, enabled = false) {}
                }

                val missing = list.filter { !it.available }
                SectionTitle(stringResource(R.string.missing_files, missing.size))
                missing.forEach { InfoRow(it.title, stringResource(R.string.missing_hint), enabled = false) {} }

                val dups = list.filter { it.sizeBytes >= 0 && it.fileName != null }.groupBy { it.fileName to it.sizeBytes }.filter { it.value.size > 1 }
                SectionTitle(stringResource(R.string.duplicates, dups.size))
                dups.forEach { (k, g) ->
                    InfoRow(k.first ?: "", g.joinToString { it.title }, enabled = false) {}
                }
            }
        }
    }
}
