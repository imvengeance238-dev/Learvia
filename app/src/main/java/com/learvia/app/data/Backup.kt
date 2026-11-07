package com.learvia.app.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Metadata backup only: structure, progress, notes, bookmarks, markers, transcripts, history, plans,
 * focus sessions, collections and media REFERENCES. Video/image files are never copied (BK-001 scope assumed).
 * After restoring on another device the references may need re-linking.
 */
object Backup {
    private const val FORMAT = 1

    private fun JSONObject.s(k: String): String? = if (isNull(k)) null else getString(k)
    private fun JSONObject.l(k: String): Long? = if (isNull(k)) null else getLong(k)
    private fun JSONObject.i(k: String): Int? = if (isNull(k)) null else getInt(k)
    private fun <T> JSONArray.each(f: (JSONObject) -> T): List<T> = (0 until length()).map { f(getJSONObject(it)) }
    private fun <T> List<T>.arr(f: (T) -> JSONObject) = JSONArray().also { a -> forEach { a.put(f(it)) } }
    private fun obj(vararg p: Pair<String, Any?>) = JSONObject().also { o -> p.forEach { (k, v) -> o.put(k, v ?: JSONObject.NULL) } }

    suspend fun export(db: AppDatabase): String {
        val d = db.dao()
        val root = JSONObject().put("format", FORMAT)
        root.put("courses", d.courses().first().arr { obj("id" to it.id, "title" to it.title, "description" to it.description, "thumb" to it.thumbnailUri, "order" to it.sortOrder, "color" to it.colorArgb, "profile" to it.profile) })
        root.put("subjects", d.subjects().first().arr { obj("id" to it.id, "courseId" to it.courseId, "title" to it.title, "description" to it.description, "thumb" to it.thumbnailUri, "order" to it.sortOrder) })
        root.put("chapters", d.chapters().first().arr { obj("id" to it.id, "courseId" to it.courseId, "subjectId" to it.subjectId, "title" to it.title, "description" to it.description, "thumb" to it.thumbnailUri, "order" to it.sortOrder) })
        root.put("classes", d.classes().first().arr { obj("id" to it.id, "chapterId" to it.chapterId, "title" to it.title, "description" to it.description, "thumb" to it.thumbnailUri, "mediaUri" to it.mediaUri, "mediaName" to it.mediaName, "pos" to it.lastPositionMs, "dur" to it.durationMs, "order" to it.sortOrder, "watchedAt" to it.lastWatchedAt, "furthest" to it.furthestMs, "completed" to it.completed) })
        root.put("bookmarks", d.bookmarks().first().arr { obj("id" to it.id, "classId" to it.classId, "pos" to it.positionMs, "label" to it.label, "at" to it.createdAt) })
        root.put("notes", d.notes().first().arr { obj("id" to it.id, "classId" to it.classId, "pos" to it.positionMs, "text" to it.text, "at" to it.createdAt) })
        root.put("markers", d.markers().first().arr { obj("id" to it.id, "classId" to it.classId, "pos" to it.positionMs, "label" to it.label) })
        root.put("transcripts", d.allTranscriptLines().arr { obj("id" to it.id, "classId" to it.classId, "start" to it.startMs, "end" to it.endMs, "text" to it.text) })
        root.put("history", d.history().first().arr { obj("id" to it.id, "classId" to it.classId, "at" to it.startedAt, "ms" to it.watchedMs) })
        root.put("plans", d.plans().first().arr { obj("id" to it.id, "title" to it.title, "day" to it.epochDay, "done" to it.done) })
        root.put("focus", d.focus().first().arr { obj("id" to it.id, "at" to it.startedAt, "ms" to it.durationMs) })
        root.put("collections", d.collections().first().arr { obj("id" to it.id, "name" to it.name) })
        root.put("links", d.collectionLinks().first().arr { obj("c" to it.collectionId, "k" to it.courseId) })
        return root.toString()
    }

    /** Replaces all current data. Throws on an unreadable file before anything is deleted. */
    suspend fun import(db: AppDatabase, json: String) {
        val r = JSONObject(json)
        require(r.optInt("format", -1) == FORMAT) { "Unsupported backup format" }
        val courses = r.getJSONArray("courses").each { CourseEntity(it.getLong("id"), it.getString("title"), it.getString("description"), it.s("thumb"), it.getInt("order"), it.i("color"), it.getString("profile")) }
        val subjects = r.getJSONArray("subjects").each { SubjectEntity(it.getLong("id"), it.getLong("courseId"), it.getString("title"), it.getString("description"), it.s("thumb"), it.getInt("order")) }
        val chapters = r.getJSONArray("chapters").each { ChapterEntity(it.getLong("id"), it.getLong("courseId"), it.l("subjectId"), it.getString("title"), it.getString("description"), it.s("thumb"), it.getInt("order")) }
        val classes = r.getJSONArray("classes").each { ClassEntity(it.getLong("id"), it.getLong("chapterId"), it.getString("title"), it.getString("description"), it.s("thumb"), it.s("mediaUri"), it.s("mediaName"), it.getLong("pos"), it.getLong("dur"), it.getInt("order"), it.getLong("watchedAt"), it.getLong("furthest"), it.getBoolean("completed")) }
        val bookmarks = r.getJSONArray("bookmarks").each { BookmarkEntity(it.getLong("id"), it.getLong("classId"), it.getLong("pos"), it.getString("label"), it.getLong("at")) }
        val notes = r.getJSONArray("notes").each { NoteEntity(it.getLong("id"), it.getLong("classId"), it.l("pos"), it.getString("text"), it.getLong("at")) }
        val markers = r.getJSONArray("markers").each { MarkerEntity(it.getLong("id"), it.getLong("classId"), it.getLong("pos"), it.getString("label")) }
        val lines = r.getJSONArray("transcripts").each { TranscriptLineEntity(it.getLong("id"), it.getLong("classId"), it.getLong("start"), it.getLong("end"), it.getString("text")) }
        val history = r.getJSONArray("history").each { HistoryEntity(it.getLong("id"), it.getLong("classId"), it.getLong("at"), it.getLong("ms")) }
        val plans = r.getJSONArray("plans").each { PlanEntity(it.getLong("id"), it.getString("title"), it.getLong("day"), it.getBoolean("done")) }
        val focus = r.getJSONArray("focus").each { FocusEntity(it.getLong("id"), it.getLong("at"), it.getLong("ms")) }
        val collections = r.getJSONArray("collections").each { CollectionEntity(it.getLong("id"), it.getString("name")) }
        val links = r.getJSONArray("links").each { CollectionCourseEntity(it.getLong("c"), it.getLong("k")) }

        db.clearAllTables()
        val d = db.dao()
        db.withTransaction {
            courses.forEach { d.insertCourse(it) }
            subjects.forEach { d.insertSubject(it) }
            chapters.forEach { d.insertChapter(it) }
            classes.forEach { d.insertClass(it) }
            bookmarks.forEach { d.insertBookmark(it) }
            notes.forEach { d.insertNote(it) }
            markers.forEach { d.insertMarker(it) }
            d.insertTranscriptLines(lines)
            history.forEach { d.insertHistory(it) }
            plans.forEach { d.insertPlan(it) }
            focus.forEach { d.insertFocus(it) }
            collections.forEach { d.insertCollection(it) }
            links.forEach { d.insertLink(it) }
        }
    }
}
