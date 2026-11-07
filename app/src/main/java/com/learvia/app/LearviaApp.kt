package com.learvia.app

import android.app.Application
import com.learvia.app.data.AppDatabase
import com.learvia.app.data.LibraryRepository
import com.learvia.app.util.Prefs

class LearviaApp : Application() {
    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { LibraryRepository(database.dao()) }
    val prefs by lazy { Prefs(this) }
}
