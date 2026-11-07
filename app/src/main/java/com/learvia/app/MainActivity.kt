package com.learvia.app

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.learvia.app.ui.LearviaNav
import com.learvia.app.ui.theme.LearviaTheme

/** Shared flags between the player screen and the activity (Picture-in-Picture). */
object PipState {
    var active by mutableStateOf(false)
    var eligible = false // true only while a video is on screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LearviaTheme { LearviaNav() } }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PipState.eligible && !PipState.active) enterPip()
    }

    fun enterPip() {
        try { enterPictureInPictureMode(PictureInPictureParams.Builder().build()) } catch (_: Exception) { }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PipState.active = isInPictureInPictureMode
    }
}
