package com.learvia.app.util

import com.learvia.app.data.ChapterEntity
import com.learvia.app.data.ClassEntity
import com.learvia.app.data.CourseEntity
import com.learvia.app.data.SubjectEntity

/** 0..1 progress of one class (furthest position / duration; completed counts as 1). */
fun ClassEntity.progress(): Float =
    if (completed) 1f else if (durationMs > 0) (furthestMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

// Placeholder profile speeds (TBD-12): Study 1.0x, Revision 1.5x, Relaxed 0.75x, Custom 1.25x.
val PROFILES = listOf("STUDY", "REVISION", "RELAXED", "CUSTOM")
fun profileSpeed(p: String): Float = when (p) {
    "REVISION" -> 1.5f
    "RELAXED" -> 0.75f
    "CUSTOM" -> 1.25f
    else -> 1f
}

/** Learning order across the library: course, then subject (direct chapters first), chapter, class. Only classes with media. */
fun orderedPlayable(
    courses: List<CourseEntity>, subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>, classes: List<ClassEntity>,
): List<ClassEntity> {
    val subjOrder = subjects.associate { it.id to it.sortOrder }
    val chOrder = chapters.associateBy { it.id }
    val courseRank = courses.withIndex().associate { it.value.id to it.index }
    return classes.filter { it.mediaUri != null && chOrder[it.chapterId] != null }
        .sortedWith(
            compareBy<ClassEntity>(
                { courseRank[chOrder[it.chapterId]!!.courseId] ?: Int.MAX_VALUE },
                { chOrder[it.chapterId]!!.subjectId?.let { s -> subjOrder[s] ?: 0 } ?: -1 },
                { chOrder[it.chapterId]!!.sortOrder },
                { it.sortOrder },
            ),
        )
}

/**
 * Rule-based recommendation (TBD-04, assumed): the first not-yet-completed class after the most recently
 * watched one in learning order, wrapping around. No AI or network involved.
 */
fun recommend(ordered: List<ClassEntity>): ClassEntity? {
    val last = ordered.filter { it.lastWatchedAt > 0 }.maxByOrNull { it.lastWatchedAt }
    val start = if (last != null) ordered.indexOf(last) + 1 else 0
    return (ordered.drop(start) + ordered.take(start)).firstOrNull { !it.completed && it.id != last?.id }
}
