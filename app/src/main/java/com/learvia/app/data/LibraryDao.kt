package com.learvia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    // ---- streams
    @Query("SELECT * FROM courses ORDER BY sortOrder, id") fun courses(): Flow<List<CourseEntity>>
    @Query("SELECT * FROM subjects ORDER BY sortOrder, id") fun subjects(): Flow<List<SubjectEntity>>
    @Query("SELECT * FROM chapters ORDER BY sortOrder, id") fun chapters(): Flow<List<ChapterEntity>>
    @Query("SELECT * FROM classes ORDER BY sortOrder, id") fun classes(): Flow<List<ClassEntity>>
    @Query("SELECT * FROM bookmarks ORDER BY positionMs") fun bookmarks(): Flow<List<BookmarkEntity>>
    @Query("SELECT * FROM notes ORDER BY createdAt DESC") fun notes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM markers ORDER BY positionMs") fun markers(): Flow<List<MarkerEntity>>
    @Query("SELECT * FROM history ORDER BY startedAt DESC") fun history(): Flow<List<HistoryEntity>>
    @Query("SELECT * FROM plans ORDER BY epochDay, id") fun plans(): Flow<List<PlanEntity>>
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC") fun focus(): Flow<List<FocusEntity>>
    @Query("SELECT * FROM collections ORDER BY id") fun collections(): Flow<List<CollectionEntity>>
    @Query("SELECT * FROM collection_courses") fun collectionLinks(): Flow<List<CollectionCourseEntity>>
    @Query("SELECT * FROM transcript_lines WHERE classId = :classId ORDER BY startMs")
    fun transcriptFor(classId: Long): Flow<List<TranscriptLineEntity>>

    // ---- one-shot reads
    @Query("SELECT * FROM classes WHERE id = :id") suspend fun classById(id: Long): ClassEntity?
    @Query("SELECT * FROM transcript_lines WHERE text LIKE '%' || :q || '%' ORDER BY classId, startMs LIMIT 50")
    suspend fun searchTranscript(q: String): List<TranscriptLineEntity>
    @Query("SELECT * FROM transcript_lines") suspend fun allTranscriptLines(): List<TranscriptLineEntity>

    // ---- inserts (REPLACE also serves backup restore, ids preserved)
    @Insert(onConflict = REPLACE) suspend fun insertCourse(e: CourseEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertSubject(e: SubjectEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertChapter(e: ChapterEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertClass(e: ClassEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertBookmark(e: BookmarkEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertNote(e: NoteEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertMarker(e: MarkerEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertTranscriptLines(e: List<TranscriptLineEntity>)
    @Insert(onConflict = REPLACE) suspend fun insertHistory(e: HistoryEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertPlan(e: PlanEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertFocus(e: FocusEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertCollection(e: CollectionEntity): Long
    @Insert(onConflict = REPLACE) suspend fun insertLink(e: CollectionCourseEntity)

    // ---- rename / order / appearance
    @Query("UPDATE courses SET title = :t WHERE id = :id") suspend fun renameCourse(id: Long, t: String)
    @Query("UPDATE subjects SET title = :t WHERE id = :id") suspend fun renameSubject(id: Long, t: String)
    @Query("UPDATE chapters SET title = :t WHERE id = :id") suspend fun renameChapter(id: Long, t: String)
    @Query("UPDATE classes SET title = :t WHERE id = :id") suspend fun renameClass(id: Long, t: String)
    @Query("UPDATE courses SET sortOrder = :o WHERE id = :id") suspend fun orderCourse(id: Long, o: Int)
    @Query("UPDATE subjects SET sortOrder = :o WHERE id = :id") suspend fun orderSubject(id: Long, o: Int)
    @Query("UPDATE chapters SET sortOrder = :o WHERE id = :id") suspend fun orderChapter(id: Long, o: Int)
    @Query("UPDATE classes SET sortOrder = :o WHERE id = :id") suspend fun orderClass(id: Long, o: Int)
    @Query("UPDATE courses SET profile = :p WHERE id = :id") suspend fun setProfile(id: Long, p: String)
    @Query("UPDATE courses SET colorArgb = :c WHERE id = :id") suspend fun setColor(id: Long, c: Int?)
    @Query("UPDATE courses SET thumbnailUri = :u WHERE id = :id") suspend fun setCourseThumb(id: Long, u: String?)
    @Query("UPDATE subjects SET thumbnailUri = :u WHERE id = :id") suspend fun setSubjectThumb(id: Long, u: String?)
    @Query("UPDATE chapters SET thumbnailUri = :u WHERE id = :id") suspend fun setChapterThumb(id: Long, u: String?)
    @Query("UPDATE classes SET thumbnailUri = :u WHERE id = :id") suspend fun setClassThumb(id: Long, u: String?)

    // ---- deletes: Learvia metadata only (FK cascade). Original media files are never touched.
    @Query("DELETE FROM courses WHERE id = :id") suspend fun deleteCourse(id: Long)
    @Query("DELETE FROM subjects WHERE id = :id") suspend fun deleteSubject(id: Long)
    @Query("DELETE FROM chapters WHERE id = :id") suspend fun deleteChapter(id: Long)
    @Query("DELETE FROM classes WHERE id = :id") suspend fun deleteClass(id: Long)
    @Query("DELETE FROM bookmarks WHERE id = :id") suspend fun deleteBookmark(id: Long)
    @Query("DELETE FROM notes WHERE id = :id") suspend fun deleteNote(id: Long)
    @Query("DELETE FROM markers WHERE id = :id") suspend fun deleteMarker(id: Long)
    @Query("DELETE FROM transcript_lines WHERE classId = :classId") suspend fun deleteTranscript(classId: Long)
    @Query("DELETE FROM plans WHERE id = :id") suspend fun deletePlan(id: Long)
    @Query("DELETE FROM collections WHERE id = :id") suspend fun deleteCollection(id: Long)
    @Query("DELETE FROM collection_courses WHERE collectionId = :c AND courseId = :k")
    suspend fun deleteLink(c: Long, k: Long)
    @Query("UPDATE plans SET done = :d WHERE id = :id") suspend fun setPlanDone(id: Long, d: Boolean)

    // ---- media & progress
    @Query("UPDATE classes SET mediaUri = :uri, mediaName = :name WHERE id = :id")
    suspend fun setMedia(id: Long, uri: String?, name: String?)

    /**
     * Progress rule (assumed, TBD-03): furthest position reached; a class counts as completed once
     * the furthest position reaches 90% of the known duration. Completion is never reset.
     */
    @Query(
        "UPDATE classes SET lastPositionMs = :pos, durationMs = :dur, lastWatchedAt = :now, " +
            "furthestMs = MAX(furthestMs, :pos), " +
            "completed = CASE WHEN completed = 1 OR (:dur > 0 AND MAX(furthestMs, :pos) >= :dur * 0.9) THEN 1 ELSE 0 END " +
            "WHERE id = :id",
    )
    suspend fun savePosition(id: Long, pos: Long, dur: Long, now: Long)
}
