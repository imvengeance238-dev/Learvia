package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.util.progress

@Composable
fun ChapterScreen(vm: LibraryViewModel, chapterId: Long, onBack: () -> Unit, onPlay: (Long) -> Unit) {
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val classes by vm.classes.collectAsStateWithLifecycle()
    val chapter = chapters.firstOrNull { it.id == chapterId }
    val mine = classes.filter { it.chapterId == chapterId }

    ScreenScaffold(chapter?.title ?: "", onBack) { pad ->
        if (mine.isEmpty()) {
            CenterMessage(null, stringResource(R.string.no_classes), pad)
        } else {
            LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(12.dp)) {
                items(mine, key = { it.id }) { c ->
                    val status = when {
                        c.mediaUri == null -> stringResource(R.string.status_unassigned)
                        c.completed -> stringResource(R.string.status_completed)
                        c.lastPositionMs > 0 -> stringResource(R.string.status_in_progress) + " · " + (c.progress() * 100).toInt() + "%"
                        else -> stringResource(R.string.status_ready)
                    }
                    // Unassigned classes stay visible but cannot be opened (AT-004).
                    InfoRow(c.title, status, enabled = c.mediaUri != null, thumbnailUri = c.thumbnailUri) { onPlay(c.id) }
                }
            }
        }
    }
}
