package com.learvia.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.learvia.app.MainActivity
import com.learvia.app.PipState
import com.learvia.app.R
import com.learvia.app.data.ClassEntity
import com.learvia.app.ui.LibraryViewModel
import com.learvia.app.util.formatTime
import com.learvia.app.util.profileSpeed
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Load {
    data object Loading : Load
    data object Missing : Load
    data class Ready(val cls: ClassEntity) : Load
}

private sealed interface PDialog {
    data object Bookmark : PDialog
    data object Note : PDialog
    data object Marker : PDialog
}

private val RESIZE_MODES = intArrayOf(
    AspectRatioFrameLayout.RESIZE_MODE_FIT, AspectRatioFrameLayout.RESIZE_MODE_FILL, AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
)
private val RESIZE_LABELS = intArrayOf(R.string.aspect_fit, R.string.aspect_stretch, R.string.aspect_crop)
private val SEEK_CHOICES = intArrayOf(5, 10, 15, 30) // placeholder choices (TBD-12)

private suspend fun canOpen(ctx: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    try { ctx.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false } catch (_: Exception) { false }
}

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}

@Composable
fun PlayerScreen(vm: LibraryViewModel, classId: Long, startPos: Long, onBack: () -> Unit, onOpenClass: (Long) -> Unit) {
    val ctx = LocalContext.current
    val classes by vm.classes.collectAsStateWithLifecycle()
    val chapters by vm.chapters.collectAsStateWithLifecycle()
    val courses by vm.courses.collectAsStateWithLifecycle()
    var load by remember { mutableStateOf<Load>(Load.Loading) }

    LaunchedEffect(classId) {
        val c = vm.classById(classId)
        load = if (c?.mediaUri != null && canOpen(ctx, Uri.parse(c.mediaUri))) Load.Ready(c) else Load.Missing
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val l = load) {
            Load.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            Load.Missing -> CenterMessage(stringResource(R.string.media_missing_title), stringResource(R.string.media_missing_body))
            is Load.Ready -> {
                val c = l.cls
                val sibs = classes.filter { it.chapterId == c.chapterId && it.mediaUri != null }
                val i = sibs.indexOfFirst { it.id == c.id }
                val course = courses.firstOrNull { co -> co.id == chapters.firstOrNull { it.id == c.chapterId }?.courseId }
                PlayerContent(
                    c, startPos, profileSpeed(course?.profile ?: "STUDY"),
                    sibs.getOrNull(i - 1)?.id, sibs.getOrNull(i + 1)?.id, vm, onBack, onOpenClass,
                )
            }
        }
        if (load !is Load.Ready) {
            IconButton(onClick = onBack, Modifier.statusBarsPadding().padding(4.dp).align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
            }
        }
    }
}

@Composable
private fun PlayerContent(
    c: ClassEntity, startPos: Long, speed: Float, prevId: Long?, nextId: Long?,
    vm: LibraryViewModel, onBack: () -> Unit, onOpen: (Long) -> Unit,
) {
    val ctx = LocalContext.current
    val activity = remember { ctx.findActivity() }
    val scope = rememberCoroutineScope()
    val audio = remember { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var failed by remember { mutableStateOf(false) }
    var clamped by remember { mutableStateOf(false) }
    var seekSec by remember { mutableIntStateOf(vm.prefs.seekSeconds) }
    val startedAt = remember { System.currentTimeMillis() }
    var watchedMs by remember { mutableLongStateOf(0L) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var distractionFree by rememberSaveable { mutableStateOf(false) }
    var panel by remember { mutableStateOf<Panel?>(null) }
    var dialog by remember { mutableStateOf<PDialog?>(null) }
    var menu by remember { mutableStateOf(false) }
    var pointA by remember { mutableStateOf<Long?>(null) }
    var pointB by remember { mutableStateOf<Long?>(null) }
    var loop by remember { mutableStateOf(false) }
    var autoNext by remember { mutableStateOf(true) }
    var resize by remember { mutableIntStateOf(0) }
    var sleepEndAt by remember { mutableLongStateOf(0L) }
    var viewRef by remember { mutableStateOf<PlayerView?>(null) }
    var brightness by remember { mutableFloatStateOf(activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0 } ?: 0.5f) }
    var volAcc by remember { mutableFloatStateOf(0f) }
    var holding by remember { mutableStateOf(false) }
    var prevSpeed by remember { mutableFloatStateOf(1f) }

    val player = remember(c.id) {
        ExoPlayer.Builder(ctx)
            .setSeekBackIncrementMs(10_000).setSeekForwardIncrementMs(10_000)
            .setHandleAudioBecomingNoisy(true) // headphone / Bluetooth disconnect auto-pause
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
            .build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(c.mediaUri)), if (startPos >= 0) startPos else c.lastPositionMs)
                setPlaybackSpeed(speed)
                prepare()
                playWhenReady = true
            }
    }

    fun dur() = player.duration.let { if (it == C.TIME_UNSET) 0L else it }
    fun save() = vm.savePosition(c.id, player.currentPosition, dur())
    fun say(s: String) { feedback = s }
    fun seekBy(delta: Long) {
        val d = player.duration
        val t = player.currentPosition + delta
        player.seekTo(if (d == C.TIME_UNSET) t.coerceAtLeast(0) else t.coerceIn(0, d))
        say((if (delta < 0) "−" else "+") + (kotlin.math.abs(delta) / 1000) + "s")
    }
    fun toggleControls() { viewRef?.let { if (it.isControllerFullyVisible) it.hideController() else it.showController() } }
    fun setBrightness(v: Float) {
        activity?.let { a -> a.window.attributes = a.window.attributes.also { it.screenBrightness = v } }
    }

    DisposableEffect(Unit) { PipState.eligible = true; onDispose { PipState.eligible = false } }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) { failed = true }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && !clamped) {
                    clamped = true
                    val d = player.duration
                    // Never resume beyond the file's real duration (LP-001 edge case).
                    if (d != C.TIME_UNSET && player.currentPosition >= d - 1_000) player.seekTo(0)
                }
                if (state == Player.STATE_ENDED) {
                    vm.savePosition(c.id, dur(), dur())
                    if (autoNext && nextId != null && !loop) onOpen(nextId)
                }
            }
        }
        player.addListener(listener)
        onDispose {
            save()
            vm.recordSession(c.id, startedAt, watchedMs)
            player.removeListener(listener)
            player.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Pause when the app leaves the foreground. Background audio / lock-screen controls are not implemented yet (VP-006).
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, player) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_STOP) { player.pause(); save() } }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    // Active study time = time the video is really playing in the foreground (TBD-20 assumption).
    LaunchedEffect(player) {
        var tick = 0
        while (true) {
            delay(1_000)
            positionMs = player.currentPosition
            if (player.isPlaying) watchedMs += 1_000
            if (++tick % 5 == 0 && player.isPlaying) save() // placeholder save interval (LP-001)
        }
    }
    LaunchedEffect(feedback) { if (feedback != null) { delay(900); feedback = null } }
    LaunchedEffect(loop) { player.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF }
    LaunchedEffect(pointA, pointB) {
        val a = pointA; val b = pointB
        if (a != null && b != null && b > a) while (true) { delay(150); if (player.currentPosition >= b) player.seekTo(a) }
    }
    LaunchedEffect(sleepEndAt) {
        if (sleepEndAt > 0) {
            delay((sleepEndAt - System.currentTimeMillis()).coerceAtLeast(0))
            player.pause(); sleepEndAt = 0; say(ctx.getString(R.string.sleep_paused))
        }
    }

    fun capture() {
        val pos = player.currentPosition
        scope.launch(Dispatchers.IO) {
            val r = MediaMetadataRetriever()
            val ok = try {
                r.setDataSource(ctx, Uri.parse(c.mediaUri))
                val bmp = r.getFrameAtTime(pos * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                if (bmp != null) {
                    val f = File(ctx.getExternalFilesDir("captures"), "learvia_${c.id}_$pos.jpg")
                    f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
                    true
                } else false
            } catch (_: Exception) { false } finally { r.release() }
            withContext(Dispatchers.Main) { say(ctx.getString(if (ok) R.string.frame_saved else R.string.frame_failed)) }
        }
    }

    if (failed) {
        CenterMessage(null, stringResource(R.string.playback_error))
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PlayerView(it).apply { this.player = player; useController = true }.also { v -> viewRef = v } },
            update = { it.resizeMode = RESIZE_MODES[resize] },
        )

        if (!PipState.active) {
            // Gesture zones: left = brightness (player-only), right = volume, horizontal swipe = seek,
            // double-tap = seek, press-and-hold = 2x. The centre/bottom stay free for the built-in controls.
            Row(Modifier.align(Alignment.Center).fillMaxWidth().fillMaxHeight(0.6f)) {
                GestureZone(
                    Modifier.weight(1f),
                    onDouble = { seekBy(-seekSec * 1000L) }, onTap = ::toggleControls,
                    onHoldStart = { prevSpeed = player.playbackParameters.speed; player.setPlaybackSpeed(2f); holding = true; say("2×") },
                    onHoldEnd = { if (holding) { player.setPlaybackSpeed(prevSpeed); holding = false } },
                    onVertical = { dy -> brightness = (brightness - dy / 900f).coerceIn(0.02f, 1f); setBrightness(brightness); say("☀ ${(brightness * 100).toInt()}%") },
                    onHorizontalEnd = { acc -> if (kotlin.math.abs(acc) > 20) seekBy((acc * 50).toLong()) },
                )
                Spacer(Modifier.weight(1.2f))
                GestureZone(
                    Modifier.weight(1f),
                    onDouble = { seekBy(seekSec * 1000L) }, onTap = ::toggleControls,
                    onHoldStart = { prevSpeed = player.playbackParameters.speed; player.setPlaybackSpeed(2f); holding = true; say("2×") },
                    onHoldEnd = { if (holding) { player.setPlaybackSpeed(prevSpeed); holding = false } },
                    onVertical = { dy ->
                        volAcc += -dy
                        if (kotlin.math.abs(volAcc) > 35f) {
                            val steps = (volAcc / 35f).toInt()
                            volAcc -= steps * 35f
                            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val v = (audio.getStreamVolume(AudioManager.STREAM_MUSIC) + steps).coerceIn(0, max)
                            audio.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0)
                            say("🔊 $v/$max")
                        }
                    },
                    onHorizontalEnd = { acc -> if (kotlin.math.abs(acc) > 20) seekBy((acc * 50).toLong()) },
                )
            }

            if (distractionFree) {
                // Distraction-free: everything except the video and the built-in seekbar is hidden; one dim exit control stays.
                IconButton(onClick = { distractionFree = false }, Modifier.statusBarsPadding().align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(Icons.Default.Close, stringResource(R.string.exit_distraction_free), tint = Color.White.copy(alpha = 0.5f))
                }
            } else {
                IconButton(onClick = onBack, Modifier.statusBarsPadding().align(Alignment.TopStart).padding(4.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
                }
                Box(Modifier.statusBarsPadding().align(Alignment.TopEnd).padding(4.dp)) {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, stringResource(R.string.more_actions), tint = Color.White) }
                    DropdownMenu(menu, { menu = false }) {
                        @Composable
                        fun Item(res: Int, extra: String = "", onClick: () -> Unit) =
                            DropdownMenuItem(text = { Text(stringResource(res) + extra) }, onClick = { menu = false; onClick() })

                        Item(R.string.add_bookmark) { dialog = PDialog.Bookmark }
                        Item(R.string.add_note) { dialog = PDialog.Note }
                        Item(R.string.add_marker) { dialog = PDialog.Marker }
                        Item(R.string.panel_bookmarks) { panel = Panel.BOOKMARKS }
                        Item(R.string.panel_notes) { panel = Panel.NOTES }
                        Item(R.string.panel_markers) { panel = Panel.MARKERS }
                        Item(R.string.panel_transcript) { panel = Panel.TRANSCRIPT }
                        Item(R.string.panel_revision) { panel = Panel.REVISION }
                        Item(R.string.ab_set_a, pointA?.let { " (${formatTime(it)})" } ?: "") { pointA = player.currentPosition; if (pointB != null && pointB!! <= pointA!!) pointB = null }
                        Item(R.string.ab_set_b, pointB?.let { " (${formatTime(it)})" } ?: "") {
                            val a = pointA
                            if (a == null || player.currentPosition <= a) say(ctx.getString(R.string.ab_invalid)) else pointB = player.currentPosition
                        }
                        Item(R.string.ab_clear) { pointA = null; pointB = null }
                        Item(R.string.loop, if (loop) " ✓" else "") { loop = !loop }
                        Item(R.string.autoplay_next, if (autoNext) " ✓" else "") { autoNext = !autoNext }
                        Item(R.string.sleep_15) { sleepEndAt = System.currentTimeMillis() + 15 * 60_000L }
                        Item(R.string.sleep_30) { sleepEndAt = System.currentTimeMillis() + 30 * 60_000L }
                        Item(R.string.sleep_60) { sleepEndAt = System.currentTimeMillis() + 60 * 60_000L }
                        Item(R.string.sleep_off) { sleepEndAt = 0 }
                        Item(R.string.aspect_mode, ": " + stringResource(RESIZE_LABELS[resize])) { resize = (resize + 1) % RESIZE_MODES.size }
                        Item(R.string.rotate) {
                            val land = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                            activity?.requestedOrientation = if (land) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                        Item(R.string.auto_rotate) { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
                        // Frame stepping is approximate (~30 fps) until exact frame timing is verified per file.
                        Item(R.string.frame_back) { player.pause(); player.seekTo((player.currentPosition - 33).coerceAtLeast(0)) }
                        Item(R.string.frame_forward) { player.pause(); player.seekTo(player.currentPosition + 33) }
                        Item(R.string.seek_interval, ": ${seekSec}s") {
                            seekSec = SEEK_CHOICES[(SEEK_CHOICES.indexOf(seekSec) + 1) % SEEK_CHOICES.size]
                            vm.prefs.seekSeconds = seekSec
                        }
                        Item(R.string.capture_frame) { capture() }
                        Item(R.string.distraction_free) { distractionFree = true }
                        Item(R.string.pip) { (activity as? MainActivity)?.enterPip() }
                        if (prevId != null) Item(R.string.prev_class) { onOpen(prevId) }
                        if (nextId != null) Item(R.string.next_class) { onOpen(nextId) }
                    }
                }
            }
        }

        feedback?.let {
            Text(
                it,
                Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White, style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    when (dialog) {
        PDialog.Bookmark -> InputDialog(R.string.add_bookmark, formatTime(player.currentPosition), onDismiss = { dialog = null }) { vm.addBookmark(c.id, player.currentPosition, it) }
        PDialog.Note -> InputDialog(R.string.add_note, "", singleLine = false, onDismiss = { dialog = null }) { vm.addNote(c.id, player.currentPosition, it) }
        PDialog.Marker -> InputDialog(R.string.add_marker, "", onDismiss = { dialog = null }) { vm.addMarker(c.id, player.currentPosition, it) }
        null -> Unit
    }
    panel?.let {
        PlayerPanels(it, { p -> panel = p }, c.id, vm, positionMs, { ms -> player.seekTo(ms); panel = null }, { panel = null })
    }
}

@Composable
private fun GestureZone(
    modifier: Modifier,
    onDouble: () -> Unit, onTap: () -> Unit, onHoldStart: () -> Unit, onHoldEnd: () -> Unit,
    onVertical: (Float) -> Unit, onHorizontalEnd: (Float) -> Unit,
) {
    Box(
        modifier.fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDouble() },
                    onTap = { onTap() },
                    onPress = {
                        coroutineScope {
                            val job = launch { delay(450); onHoldStart() }
                            tryAwaitRelease()
                            job.cancel()
                            onHoldEnd()
                        }
                    },
                )
            }
            .pointerInput(Unit) { detectVerticalDragGestures { change, dy -> change.consume(); onVertical(dy) } }
            .pointerInput(Unit) {
                var acc = 0f
                detectHorizontalDragGestures(onDragStart = { acc = 0f }, onDragEnd = { onHorizontalEnd(acc) }, onDragCancel = { }) { change, dx ->
                    change.consume(); acc += dx
                }
            },
    )
}
