package com.learvia.app.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learvia.app.LearviaApp
import com.learvia.app.R
import com.learvia.app.data.*
import com.learvia.app.util.Notifications
import com.learvia.app.util.Subtitles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Hit(@StringRes val kind: Int, val title: String, val subtitle: String?, val route: String)
enum class Volume { INTERNAL, SD, OTHER }
data class MediaInfo(
    val classId: Long, val title: String, val courseId: Long, val courseTitle: String,
    val fileName: String?, val sizeBytes: Long, val available: Boolean, val volume: Volume,
)
data class FocusUi(
    val running: Boolean = false, val isBreak: Boolean = false,
    val workMin: Int = 25, val breakMin: Int = 5, val remainingMs: Long = 25 * 60_000L,
)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val la = app as LearviaApp
    private val repo = la.repository
    private val dao get() = repo.dao
    private val cr get() = getApplication<Application>().contentResolver
    private fun <T> Flow<List<T>>.state() = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val courses = repo.courses.state()
    val subjects = repo.subjects.state()
    val chapters = repo.chapters.state()
    val classes = repo.classes.state()
    val bookmarks = repo.bookmarks.state()
    val notes = repo.notes.state()
    val markers = repo.markers.state()
    val history = repo.history.state()
    val plans = repo.plans.state()
    val focus = repo.focus.state()
    val collections = repo.collections.state()
    val links = repo.links.state()
    val prefs get() = la.prefs

    fun transcriptFor(classId: Long) = dao.transcriptFor(classId)
    suspend fun classById(id: Long) = repo.classById(id)

    // ---------- structure
    fun addCourse(t: String) = viewModelScope.launch { repo.addCourse(t.trim()) }
    fun addSubject(courseId: Long, t: String) = viewModelScope.launch { repo.addSubject(courseId, t.trim()) }
    fun addChapter(courseId: Long, subjectId: Long?, t: String) = viewModelScope.launch { repo.addChapter(courseId, subjectId, t.trim()) }
    fun addClass(chapterId: Long, t: String) = viewModelScope.launch { repo.addClass(chapterId, t.trim()) }
    fun rename(kind: NodeKind, id: Long, t: String) = viewModelScope.launch { repo.rename(kind, id, t.trim()) }
    fun delete(kind: NodeKind, id: Long) = viewModelScope.launch { repo.delete(kind, id) }
    fun setProfile(courseId: Long, p: String) = viewModelScope.launch { dao.setProfile(courseId, p) }
    fun setColor(courseId: Long, argb: Int?) = viewModelScope.launch { dao.setColor(courseId, argb) }

    /** Move up/down among siblings (accessible alternative to drag-and-drop). */
    fun move(kind: NodeKind, id: Long, up: Boolean) = viewModelScope.launch {
        val sibs: List<Pair<Long, Int>> = when (kind) {
            NodeKind.COURSE -> courses.value.map { it.id to it.sortOrder }
            NodeKind.SUBJECT -> { val s = subjects.value.first { it.id == id }; subjects.value.filter { it.courseId == s.courseId }.map { it.id to it.sortOrder } }
            NodeKind.CHAPTER -> { val c = chapters.value.first { it.id == id }; chapters.value.filter { it.courseId == c.courseId && it.subjectId == c.subjectId }.map { it.id to it.sortOrder } }
            NodeKind.CLASS -> { val c = classes.value.first { it.id == id }; classes.value.filter { it.chapterId == c.chapterId }.map { it.id to it.sortOrder } }
        }
        val i = sibs.indexOfFirst { it.first == id }
        val j = if (up) i - 1 else i + 1
        if (i < 0 || j < 0 || j >= sibs.size) return@launch
        repo.setOrder(kind, sibs[i].first, sibs[j].second)
        repo.setOrder(kind, sibs[j].first, sibs[i].second)
    }

    private fun persist(uri: Uri) {
        try { cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) { }
    }

    /** Stores a persisted SAF reference only. The video file is never copied, moved or modified. */
    fun assignVideo(classId: Long, uri: Uri) = viewModelScope.launch {
        val name = withContext(Dispatchers.IO) {
            persist(uri)
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
        }
        dao.setMedia(classId, uri.toString(), name)
    }

    fun setThumbnail(kind: NodeKind, id: Long, uri: Uri) = viewModelScope.launch {
        withContext(Dispatchers.IO) { persist(uri) }
        repo.setThumbnail(kind, id, uri.toString())
    }

    // ---------- playback records
    fun savePosition(classId: Long, pos: Long, dur: Long) = viewModelScope.launch {
        dao.savePosition(classId, pos.coerceAtLeast(0), dur.coerceAtLeast(0), System.currentTimeMillis())
    }
    fun recordSession(classId: Long, startedAt: Long, watchedMs: Long) = viewModelScope.launch {
        if (watchedMs >= 2_000) dao.insertHistory(HistoryEntity(classId = classId, startedAt = startedAt, watchedMs = watchedMs))
    }
    fun addBookmark(classId: Long, pos: Long, label: String) = viewModelScope.launch {
        dao.insertBookmark(BookmarkEntity(classId = classId, positionMs = pos, label = label.trim(), createdAt = System.currentTimeMillis()))
    }
    fun addNote(classId: Long, pos: Long?, text: String) = viewModelScope.launch {
        dao.insertNote(NoteEntity(classId = classId, positionMs = pos, text = text.trim(), createdAt = System.currentTimeMillis()))
    }
    fun addMarker(classId: Long, pos: Long, label: String) = viewModelScope.launch {
        dao.insertMarker(MarkerEntity(classId = classId, positionMs = pos, label = label.trim()))
    }
    fun deleteBookmark(id: Long) = viewModelScope.launch { dao.deleteBookmark(id) }
    fun deleteNote(id: Long) = viewModelScope.launch { dao.deleteNote(id) }
    fun deleteMarker(id: Long) = viewModelScope.launch { dao.deleteMarker(id) }

    /** Transcript source: a user-chosen .srt/.vtt file (TBD-07 assumption). Returns lines imported, 0 = unreadable, -1 = error. */
    suspend fun importTranscript(classId: Long, uri: Uri): Int = withContext(Dispatchers.IO) {
        val text = try { cr.openInputStream(uri)?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { null } ?: return@withContext -1
        val parsed = Subtitles.parse(text)
        if (parsed.isEmpty()) return@withContext 0
        dao.deleteTranscript(classId)
        dao.insertTranscriptLines(parsed.map { TranscriptLineEntity(classId = classId, startMs = it.first, endMs = it.second, text = it.third) })
        parsed.size
    }
    fun clearTranscript(classId: Long) = viewModelScope.launch { dao.deleteTranscript(classId) }

    // ---------- planner
    fun addPlan(title: String, epochDay: Long) = viewModelScope.launch { dao.insertPlan(PlanEntity(title = title.trim(), epochDay = epochDay)) }
    fun setPlanDone(id: Long, done: Boolean) = viewModelScope.launch { dao.setPlanDone(id, done) }
    fun deletePlan(id: Long) = viewModelScope.launch { dao.deletePlan(id) }

    // ---------- collections
    fun addCollection(name: String) = viewModelScope.launch { dao.insertCollection(CollectionEntity(name = name.trim())) }
    fun deleteCollection(id: Long) = viewModelScope.launch { dao.deleteCollection(id) }
    fun setLink(collectionId: Long, courseId: Long, on: Boolean) = viewModelScope.launch {
        if (on) dao.insertLink(CollectionCourseEntity(collectionId, courseId)) else dao.deleteLink(collectionId, courseId)
    }

    // ---------- focus / pomodoro (timer lives while the app process lives; presets are placeholders, TBD)
    private val _focusUi = MutableStateFlow(FocusUi())
    val focusUi = _focusUi.asStateFlow()
    private var focusJob: Job? = null
    private var focusStartedAt = 0L

    fun focusStart() {
        if (_focusUi.value.running) return
        if (!_focusUi.value.isBreak && _focusUi.value.remainingMs == _focusUi.value.workMin * 60_000L) focusStartedAt = System.currentTimeMillis()
        _focusUi.update { it.copy(running = true) }
        focusJob = viewModelScope.launch {
            while (_focusUi.value.remainingMs > 0) {
                delay(1_000)
                _focusUi.update { it.copy(remainingMs = (it.remainingMs - 1_000).coerceAtLeast(0)) }
            }
            onFocusPhaseDone()
        }
    }
    fun focusPause() { focusJob?.cancel(); _focusUi.update { it.copy(running = false) } }
    fun focusReset(work: Int = _focusUi.value.workMin, brk: Int = _focusUi.value.breakMin) {
        focusJob?.cancel()
        _focusUi.value = FocusUi(workMin = work, breakMin = brk, remainingMs = work * 60_000L)
    }
    private suspend fun onFocusPhaseDone() {
        val s = _focusUi.value
        val ctx = getApplication<Application>()
        if (!s.isBreak) {
            dao.insertFocus(FocusEntity(startedAt = focusStartedAt, durationMs = s.workMin * 60_000L))
            Notifications.show(ctx, 2, ctx.getString(R.string.focus_done_title), ctx.getString(R.string.focus_done_body))
        }
        _focusUi.value = s.copy(running = false, isBreak = !s.isBreak, remainingMs = (if (s.isBreak) s.workMin else s.breakMin) * 60_000L)
    }

    // ---------- global search (courses, subjects, chapters, classes, notes, bookmarks, transcripts)
    suspend fun search(q: String): List<Hit> {
        val t = q.trim()
        if (t.isEmpty()) return emptyList()
        fun String.m() = contains(t, ignoreCase = true)
        val cls = classes.value.associateBy { it.id }
        fun routeFor(classId: Long, pos: Long?): String? {
            val c = cls[classId] ?: return null
            return if (c.mediaUri != null) "player/$classId?pos=${pos ?: -1}" else "chapter/${c.chapterId}"
        }
        val hits = mutableListOf<Hit>()
        courses.value.filter { it.title.m() || it.description.m() }.forEach { hits += Hit(R.string.kind_course, it.title, null, "course/${it.id}") }
        subjects.value.filter { it.title.m() || it.description.m() }.forEach { hits += Hit(R.string.kind_subject, it.title, null, "course/${it.courseId}") }
        chapters.value.filter { it.title.m() || it.description.m() }.forEach { hits += Hit(R.string.kind_chapter, it.title, null, "chapter/${it.id}") }
        classes.value.filter { it.title.m() || it.description.m() }.forEach { c -> routeFor(c.id, null)?.let { hits += Hit(R.string.kind_class, c.title, null, it) } }
        notes.value.filter { it.text.m() }.forEach { n -> routeFor(n.classId, n.positionMs)?.let { hits += Hit(R.string.kind_note, n.text, cls[n.classId]?.title, it) } }
        bookmarks.value.filter { it.label.m() }.forEach { b -> routeFor(b.classId, b.positionMs)?.let { hits += Hit(R.string.kind_bookmark, b.label, cls[b.classId]?.title, it) } }
        // Transcript results exist only for classes that have an imported transcript; otherwise this adds nothing and raises no error.
        dao.searchTranscript(t).forEach { l -> routeFor(l.classId, l.startMs)?.let { hits += Hit(R.string.kind_transcript, l.text, cls[l.classId]?.title, it) } }
        return hits
    }

    // ---------- media library / storage intelligence (never deletes anything)
    suspend fun scanMedia(): List<MediaInfo> = withContext(Dispatchers.IO) {
        val chs = chapters.value.associateBy { it.id }
        val crs = courses.value.associateBy { it.id }
        classes.value.filter { it.mediaUri != null }.map { c ->
            val uri = Uri.parse(c.mediaUri)
            var size = -1L
            var ok = false
            try {
                cr.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { if (it.moveToFirst() && !it.isNull(0)) size = it.getLong(0) }
                cr.openFileDescriptor(uri, "r")?.use { ok = true }
            } catch (_: Exception) { }
            val course = crs[chs[c.chapterId]?.courseId]
            MediaInfo(c.id, c.title, course?.id ?: -1, course?.title ?: "", c.mediaName, size, ok, volumeOf(uri))
        }
    }

    private fun volumeOf(uri: Uri): Volume = try {
        if (uri.authority != "com.android.externalstorage.documents") Volume.OTHER
        else if (DocumentsContract.getDocumentId(uri).substringBefore(':') == "primary") Volume.INTERNAL else Volume.SD
    } catch (_: Exception) { Volume.OTHER }

    // ---------- backup / restore (metadata only)
    suspend fun exportBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val json = Backup.export(la.database)
            cr.openOutputStream(uri, "wt")!!.use { it.write(json.toByteArray()) }
        }.isSuccess
    }
    suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val text = cr.openInputStream(uri)!!.bufferedReader().use { it.readText() }
            Backup.import(la.database, text)
        }.isSuccess
    }
}
