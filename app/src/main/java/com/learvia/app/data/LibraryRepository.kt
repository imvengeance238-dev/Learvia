package com.learvia.app.data

/** Thin layer over the DAO. Reads are exposed as flows; writes keep ordering values consistent. */
class LibraryRepository(val dao: LibraryDao) {
    val courses = dao.courses()
    val subjects = dao.subjects()
    val chapters = dao.chapters()
    val classes = dao.classes()
    val bookmarks = dao.bookmarks()
    val notes = dao.notes()
    val markers = dao.markers()
    val history = dao.history()
    val plans = dao.plans()
    val focus = dao.focus()
    val collections = dao.collections()
    val links = dao.collectionLinks()

    suspend fun classById(id: Long) = dao.classById(id)

    // New items go to the end of their siblings (sortOrder = id keeps it unique and increasing).
    suspend fun addCourse(title: String) { val id = dao.insertCourse(CourseEntity(title = title)); dao.orderCourse(id, id.toInt()) }
    suspend fun addSubject(courseId: Long, title: String) {
        val id = dao.insertSubject(SubjectEntity(courseId = courseId, title = title)); dao.orderSubject(id, id.toInt())
    }
    suspend fun addChapter(courseId: Long, subjectId: Long?, title: String) {
        val id = dao.insertChapter(ChapterEntity(courseId = courseId, subjectId = subjectId, title = title)); dao.orderChapter(id, id.toInt())
    }
    suspend fun addClass(chapterId: Long, title: String) {
        val id = dao.insertClass(ClassEntity(chapterId = chapterId, title = title)); dao.orderClass(id, id.toInt())
    }

    suspend fun rename(kind: NodeKind, id: Long, title: String) = when (kind) {
        NodeKind.COURSE -> dao.renameCourse(id, title)
        NodeKind.SUBJECT -> dao.renameSubject(id, title)
        NodeKind.CHAPTER -> dao.renameChapter(id, title)
        NodeKind.CLASS -> dao.renameClass(id, title)
    }

    suspend fun delete(kind: NodeKind, id: Long) = when (kind) {
        NodeKind.COURSE -> dao.deleteCourse(id)
        NodeKind.SUBJECT -> dao.deleteSubject(id)
        NodeKind.CHAPTER -> dao.deleteChapter(id)
        NodeKind.CLASS -> dao.deleteClass(id)
    }

    suspend fun setOrder(kind: NodeKind, id: Long, order: Int) = when (kind) {
        NodeKind.COURSE -> dao.orderCourse(id, order)
        NodeKind.SUBJECT -> dao.orderSubject(id, order)
        NodeKind.CHAPTER -> dao.orderChapter(id, order)
        NodeKind.CLASS -> dao.orderClass(id, order)
    }

    suspend fun setThumbnail(kind: NodeKind, id: Long, uri: String?) = when (kind) {
        NodeKind.COURSE -> dao.setCourseThumb(id, uri)
        NodeKind.SUBJECT -> dao.setSubjectThumb(id, uri)
        NodeKind.CHAPTER -> dao.setChapterThumb(id, uri)
        NodeKind.CLASS -> dao.setClassThumb(id, uri)
    }
}
