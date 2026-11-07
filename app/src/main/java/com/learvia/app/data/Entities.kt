package com.learvia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NodeKind { COURSE, SUBJECT, CHAPTER, CLASS }

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val thumbnailUri: String? = null,
    val sortOrder: Int = 0,
    val colorArgb: Int? = null,
    val profile: String = "STUDY",
)

@Entity(
    tableName = "subjects",
    foreignKeys = [ForeignKey(CourseEntity::class, ["id"], ["courseId"], onDelete = CASCADE)],
    indices = [Index("courseId")],
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String = "",
    val thumbnailUri: String? = null,
    val sortOrder: Int = 0,
)

/** subjectId == null means the chapter sits directly under the course (Subject is optional). */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(CourseEntity::class, ["id"], ["courseId"], onDelete = CASCADE),
        ForeignKey(SubjectEntity::class, ["id"], ["subjectId"], onDelete = CASCADE),
    ],
    indices = [Index("courseId"), Index("subjectId")],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val subjectId: Long? = null,
    val title: String,
    val description: String = "",
    val thumbnailUri: String? = null,
    val sortOrder: Int = 0,
)

/** The video is stored only as a SAF URI reference; the file itself is never copied or modified. */
@Entity(
    tableName = "classes",
    foreignKeys = [ForeignKey(ChapterEntity::class, ["id"], ["chapterId"], onDelete = CASCADE)],
    indices = [Index("chapterId")],
)
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val title: String,
    val description: String = "",
    val thumbnailUri: String? = null,
    val mediaUri: String? = null,
    val mediaName: String? = null,
    val lastPositionMs: Long = 0,
    val durationMs: Long = 0,
    val sortOrder: Int = 0,
    val lastWatchedAt: Long = 0,
    val furthestMs: Long = 0,
    val completed: Boolean = false,
)

@Entity(tableName = "bookmarks", foreignKeys = [ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE)], indices = [Index("classId")])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long, val positionMs: Long, val label: String, val createdAt: Long,
)

/** positionMs == null means a general note for the class. */
@Entity(tableName = "notes", foreignKeys = [ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE)], indices = [Index("classId")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long, val positionMs: Long?, val text: String, val createdAt: Long,
)

/** User-created section markers inside a video (not the Course -> Chapter hierarchy). */
@Entity(tableName = "markers", foreignKeys = [ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE)], indices = [Index("classId")])
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long, val positionMs: Long, val label: String,
)

@Entity(tableName = "transcript_lines", foreignKeys = [ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE)], indices = [Index("classId")])
data class TranscriptLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long, val startMs: Long, val endMs: Long, val text: String,
)

/** One row per viewing session; watchedMs counts only time the video was actually playing in the foreground. */
@Entity(tableName = "history", foreignKeys = [ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE)], indices = [Index("classId")])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long, val startedAt: Long, val watchedMs: Long,
)

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val epochDay: Long, val done: Boolean = false,
)

@Entity(tableName = "focus_sessions")
data class FocusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long, val durationMs: Long,
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

/** A course may belong to several collections. */
@Entity(
    tableName = "collection_courses",
    primaryKeys = ["collectionId", "courseId"],
    foreignKeys = [
        ForeignKey(CollectionEntity::class, ["id"], ["collectionId"], onDelete = CASCADE),
        ForeignKey(CourseEntity::class, ["id"], ["courseId"], onDelete = CASCADE),
    ],
    indices = [Index("courseId")],
)
data class CollectionCourseEntity(val collectionId: Long, val courseId: Long)
