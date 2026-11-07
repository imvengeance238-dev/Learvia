package com.learvia.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.util.orderedPlayable
import com.learvia.app.util.progress
import com.learvia.app.util.recommend

@Composable
fun LibraryScreen(vm: LibraryViewModel, onCourse: (Long) -> Unit, onClass: (Long) -> Unit, onNav: (String) -> Unit) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val subjects by vm.subjects.collectAsStateWithLifecycle()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val classes by vm.classes.collectAsStateWithLifecycle()
    val collections by vm.collections.collectAsStateWithLifecycle()
    val links by vm.links.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableLongStateOf(-1L) }

    val ordered = remember(courses, subjects, chapters, classes) { orderedPlayable(courses, subjects, chapters, classes) }
    val rec = remember(ordered) { recommend(ordered) }
    val resume = classes.filter { it.mediaUri != null && it.lastWatchedAt > 0 && !it.completed }.sortedByDescending { it.lastWatchedAt }.take(5)
    val shown = if (filter < 0) courses else courses.filter { c -> links.any { it.collectionId == filter && it.courseId == c.id } }

    ScreenScaffold(
        title = stringResource(R.string.app_name),
        actions = { IconButton(onClick = { onNav("settings") }) { Icon(Icons.Default.Settings, stringResource(R.string.settings)) } },
    ) { pad ->
        if (courses.isEmpty()) {
            CenterMessage(stringResource(R.string.empty_library_title), stringResource(R.string.empty_library_hint), pad) {
                Button(onClick = { onNav("manage") }) { Text(stringResource(R.string.open_management)) }
            }
        } else {
            LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(12.dp)) {
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = spacedBy(8.dp)) {
                        AssistChip(onClick = { onNav("search") }, label = { Text(stringResource(R.string.search)) })
                        AssistChip(onClick = { onNav("planner") }, label = { Text(stringResource(R.string.planner)) })
                        AssistChip(onClick = { onNav("focus") }, label = { Text(stringResource(R.string.focus)) })
                        AssistChip(onClick = { onNav("stats") }, label = { Text(stringResource(R.string.statistics)) })
                    }
                }
                if (resume.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.continue_watching)) }
                    items(resume, key = { "r${it.id}" }) { c ->
                        InfoRow(c.title, stringResource(R.string.percent_watched, (c.progress() * 100).toInt()), thumbnailUri = c.thumbnailUri) { onClass(c.id) }
                    }
                }
                rec?.let { r ->
                    item { SectionTitle(stringResource(R.string.recommended)) }
                    item(key = "rec") { InfoRow(r.title, stringResource(R.string.recommended_why), thumbnailUri = r.thumbnailUri) { onClass(r.id) } }
                }
                item { SectionTitle(stringResource(R.string.courses)) }
                if (collections.isNotEmpty()) {
                    item {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = spacedBy(8.dp)) {
                            FilterChip(selected = filter < 0, onClick = { filter = -1 }, label = { Text(stringResource(R.string.all)) })
                            collections.forEach { col ->
                                FilterChip(selected = filter == col.id, onClick = { filter = col.id }, label = { Text(col.name) })
                            }
                        }
                    }
                }
                if (shown.isEmpty()) item { Text(stringResource(R.string.empty_collection)) }
                items(shown, key = { "c${it.id}" }) { c ->
                    val mine = classes.filter { cl -> chapters.any { it.id == cl.chapterId && it.courseId == c.id } }
                    InfoRow(
                        c.title, progressText(mine) ?: c.description.ifBlank { null },
                        thumbnailUri = c.thumbnailUri, accentArgb = c.colorArgb,
                    ) { onCourse(c.id) }
                }
            }
        }
    }
}
