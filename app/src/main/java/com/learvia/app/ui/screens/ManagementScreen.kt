package com.learvia.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.data.NodeKind
import com.learvia.app.ui.LibraryViewModel

private enum class Act(@StringRes val label: Int) {
    ADD_SUBJECT(R.string.add_subject), ADD_CHAPTER(R.string.add_chapter), ADD_CLASS(R.string.add_class),
    ASSIGN(R.string.assign_video), RELINK(R.string.relink_video), THUMB(R.string.set_thumbnail), MOVE_UP(R.string.move_up), MOVE_DOWN(R.string.move_down), PROFILE(R.string.playback_profile), COLOR(R.string.course_color), RENAME(R.string.rename), DELETE(R.string.delete),
}

private val NodeKind.labelRes: Int
    get() = when (this) {
        NodeKind.COURSE -> R.string.kind_course
        NodeKind.SUBJECT -> R.string.kind_subject
        NodeKind.CHAPTER -> R.string.kind_chapter
        NodeKind.CLASS -> R.string.kind_class
    }

private data class Node(
    val key: String, val kind: NodeKind, val id: Long, val courseId: Long,
    val title: String, val indent: Int, val subtitle: String?, val acts: List<Act>, val thumb: String?,
)

private sealed interface Dlg {
    data class Input(@StringRes val title: Int, val initial: String, val onOk: (String) -> Unit) : Dlg
    data class Confirm(val onOk: () -> Unit) : Dlg
    data class Choice(@StringRes val title: Int, val options: List<Pair<Int, () -> Unit>>) : Dlg
}

@Composable
fun ManagementScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    val courses by vm.courses.collectAsStateWithLifecycle()
    val subjects by vm.subjects.collectAsStateWithLifecycle()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val classes by vm.classes.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<Dlg?>(null) }
    var pendingClass by rememberSaveable { mutableLongStateOf(-1L) }

    // Storage Access Framework picker: the user explicitly chooses each video (CM-004).
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && pendingClass >= 0) vm.assignVideo(pendingClass, uri)
    }

    var pendingKind by rememberSaveable { mutableIntStateOf(0) }
    var pendingThumbId by rememberSaveable { mutableLongStateOf(-1L) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && pendingThumbId >= 0) vm.setThumbnail(NodeKind.values()[pendingKind], pendingThumbId, uri)
    }

    val nodes = buildList {
        fun chapterNodes(courseId: Long, ch: com.learvia.app.data.ChapterEntity, indent: Int) {
            add(Node("h${ch.id}", NodeKind.CHAPTER, ch.id, courseId, ch.title, indent, null,
                listOf(Act.ADD_CLASS, Act.THUMB, Act.MOVE_UP, Act.MOVE_DOWN, Act.RENAME, Act.DELETE), ch.thumbnailUri))
            classes.filter { it.chapterId == ch.id }.forEach { cl ->
                add(Node("l${cl.id}", NodeKind.CLASS, cl.id, courseId, cl.title, indent + 1, cl.mediaName,
                    listOf(if (cl.mediaUri == null) Act.ASSIGN else Act.RELINK, Act.THUMB, Act.MOVE_UP, Act.MOVE_DOWN, Act.RENAME, Act.DELETE), cl.thumbnailUri))
            }
        }
        courses.forEach { c ->
            add(Node("c${c.id}", NodeKind.COURSE, c.id, c.id, c.title, 0, null,
                listOf(Act.ADD_SUBJECT, Act.ADD_CHAPTER, Act.THUMB, Act.PROFILE, Act.COLOR, Act.MOVE_UP, Act.MOVE_DOWN, Act.RENAME, Act.DELETE), c.thumbnailUri))
            subjects.filter { it.courseId == c.id }.forEach { s ->
                add(Node("s${s.id}", NodeKind.SUBJECT, s.id, c.id, s.title, 1, null,
                    listOf(Act.ADD_CHAPTER, Act.THUMB, Act.MOVE_UP, Act.MOVE_DOWN, Act.RENAME, Act.DELETE), s.thumbnailUri))
                chapters.filter { it.subjectId == s.id }.forEach { chapterNodes(c.id, it, 2) }
            }
            chapters.filter { it.courseId == c.id && it.subjectId == null }.forEach { chapterNodes(c.id, it, 1) }
        }
    }

    fun onAct(n: Node, a: Act) {
        when (a) {
            Act.ADD_SUBJECT -> dialog = Dlg.Input(R.string.add_subject, "") { vm.addSubject(n.id, it) }
            Act.ADD_CHAPTER -> dialog = Dlg.Input(R.string.add_chapter, "") {
                if (n.kind == NodeKind.COURSE) vm.addChapter(n.id, null, it) else vm.addChapter(n.courseId, n.id, it)
            }
            Act.ADD_CLASS -> dialog = Dlg.Input(R.string.add_class, "") { vm.addClass(n.id, it) }
            Act.RENAME -> dialog = Dlg.Input(R.string.rename, n.title) { vm.rename(n.kind, n.id, it) }
            Act.DELETE -> dialog = Dlg.Confirm { vm.delete(n.kind, n.id) }
            Act.MOVE_UP -> vm.move(n.kind, n.id, true)
            Act.MOVE_DOWN -> vm.move(n.kind, n.id, false)
            Act.PROFILE -> dialog = Dlg.Choice(R.string.playback_profile, listOf(
                R.string.profile_study to { vm.setProfile(n.id, "STUDY") },
                R.string.profile_revision to { vm.setProfile(n.id, "REVISION") },
                R.string.profile_relaxed to { vm.setProfile(n.id, "RELAXED") },
                R.string.profile_custom to { vm.setProfile(n.id, "CUSTOM") },
            ))
            Act.COLOR -> dialog = Dlg.Choice(R.string.course_color, listOf(
                R.string.color_red to { vm.setColor(n.id, 0xFFE53935.toInt()) },
                R.string.color_orange to { vm.setColor(n.id, 0xFFFB8C00.toInt()) },
                R.string.color_green to { vm.setColor(n.id, 0xFF43A047.toInt()) },
                R.string.color_teal to { vm.setColor(n.id, 0xFF00897B.toInt()) },
                R.string.color_blue to { vm.setColor(n.id, 0xFF1E5BEF.toInt()) },
                R.string.color_purple to { vm.setColor(n.id, 0xFF8E24AA.toInt()) },
                R.string.color_none to { vm.setColor(n.id, null) },
            ))
            Act.THUMB -> {
                pendingKind = n.kind.ordinal
                pendingThumbId = n.id
                imagePicker.launch(arrayOf("image/*"))
            }
            Act.ASSIGN, Act.RELINK -> {
                pendingClass = n.id
                picker.launch(arrayOf("video/*"))
            }
        }
    }

    ScreenScaffold(
        title = stringResource(R.string.management),
        onBack = onBack,
        fab = {
            FloatingActionButton(onClick = {
                dialog = Dlg.Input(R.string.add_course, "") { vm.addCourse(it) }
            }) { Icon(Icons.Default.Add, stringResource(R.string.add_course)) }
        },
    ) { pad ->
        if (nodes.isEmpty()) {
            CenterMessage(null, stringResource(R.string.management_empty), pad)
        } else {
            LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(4.dp)) {
                items(nodes, key = { it.key }) { n -> NodeRow(n) { onAct(n, it) } }
            }
        }
    }

    when (val d = dialog) {
        is Dlg.Input -> {
            var text by rememberSaveable(d) { mutableStateOf(d.initial) }
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(stringResource(d.title)) },
                text = {
                    OutlinedTextField(text, { text = it }, singleLine = true,
                        label = { Text(stringResource(R.string.title_label)) })
                },
                confirmButton = {
                    TextButton(enabled = text.isNotBlank(), onClick = { d.onOk(text); dialog = null }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        is Dlg.Confirm -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_body)) },
            confirmButton = {
                TextButton(onClick = { d.onOk(); dialog = null }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) } },
        )
        is Dlg.Choice -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(d.title)) },
            text = {
                androidx.compose.foundation.layout.Column(Modifier.androidxVerticalScroll()) {
                    d.options.forEach { (label, action) ->
                        TextButton(onClick = { action(); dialog = null }) { Text(stringResource(label)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) } },
        )
        null -> Unit
    }
}

@Composable
private fun Modifier.androidxVerticalScroll(): Modifier =
    this.then(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()))

@Composable
private fun NodeRow(n: Node, onAct: (Act) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(start = (n.indent * 16).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (n.thumb != null) {
            Thumb(n.thumb, 40.dp)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 12.dp))
        }
        Box(Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Column(Modifier.padding(vertical = 8.dp)) {
                Text(stringResource(n.kind.labelRes), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
                Text(n.title, style = MaterialTheme.typography.titleMedium)
                if (n.subtitle != null) Text(n.subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Box {
            IconButton(onClick = { open = true }) { Icon(Icons.Default.MoreVert, stringResource(R.string.more_actions)) }
            DropdownMenu(open, { open = false }) {
                n.acts.forEach { a ->
                    DropdownMenuItem(text = { Text(stringResource(a.label)) }, onClick = { open = false; onAct(a) })
                }
            }
        }
    }
}
