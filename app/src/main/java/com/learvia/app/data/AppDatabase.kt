package com.learvia.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CourseEntity::class, SubjectEntity::class, ChapterEntity::class, ClassEntity::class,
        BookmarkEntity::class, NoteEntity::class, MarkerEntity::class, TranscriptLineEntity::class,
        HistoryEntity::class, PlanEntity::class, FocusEntity::class,
        CollectionEntity::class, CollectionCourseEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): LibraryDao

    companion object {
        // Pre-release: version 1 held only a handful of test rows, so schema 1 -> 2 is rebuilt.
        // From the first real release on, replace this with explicit migrations and exportSchema = true.
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "learvia.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
