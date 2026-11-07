package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.ui.LibraryViewModel

/** Chapters may sit under a Subject or directly under the course (Subject is optional). */
@Composable
fun CourseScreen(vm: LibraryViewModel, courseId: Long, onBack: () -> Unit, onChapter: (Long) -> Unit) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val subjects by vm.subjects.collectAsStateWithLifecycle()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val classes by vm.classes.collectAsStateWithLifecycle()
    val course = courses.firstOrNull { it.id == courseId }
    val mySubjects = subjects.filter { it.courseId == courseId }
    val myChapters = chapters.filter { it.courseId == courseId }
    val loose = myChapters.filter { it.subjectId == null }

    ScreenScaffold(course?.title ?: "", onBack) { pad ->
        if (myChapters.isEmpty()) {
            CenterMessage(null, stringResource(R.string.no_chapters), pad)
        } else {
            LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(12.dp)) {
                mySubjects.forEach { s ->
                    val inSubject = myChapters.filter { it.subjectId == s.id }
                    if (inSubject.isNotEmpty()) {
                        item(key = "s${s.id}") { Text(s.title, style = MaterialTheme.typography.titleSmall) }
                        items(inSubject, key = { "c${it.id}" }) { ch ->
                            InfoRow(ch.title, progressText(classes.filter { it.chapterId == ch.id }) ?: ch.description.ifBlank { null }, thumbnailUri = ch.thumbnailUri) { onChapter(ch.id) }
                        }
                    }
                }
                if (loose.isNotEmpty()) {
                    if (mySubjects.isNotEmpty()) {
                        item(key = "loose") { Text(stringResource(R.string.chapters), style = MaterialTheme.typography.titleSmall) }
                    }
                    items(loose, key = { "c${it.id}" }) { ch ->
                        InfoRow(ch.title, progressText(classes.filter { it.chapterId == ch.id }) ?: ch.description.ifBlank { null }, thumbnailUri = ch.thumbnailUri) { onChapter(ch.id) }
                    }
                }
            }
        }
    }
}
