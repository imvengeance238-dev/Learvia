package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.learvia.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = { actions() },
            )
        },
        floatingActionButton = fab,
        content = content,
    )
}

fun listPadding(pad: PaddingValues, dir: androidx.compose.ui.unit.LayoutDirection) = PaddingValues(
    start = 16.dp + pad.calculateStartPadding(dir),
    end = 16.dp + pad.calculateEndPadding(dir),
    top = pad.calculateTopPadding() + 8.dp,
    bottom = pad.calculateBottomPadding() + 88.dp,
)

@Composable
fun contentPadding(pad: PaddingValues) = listPadding(pad, LocalLayoutDirection.current)

@Composable
fun Thumb(uri: String?, size: androidx.compose.ui.unit.Dp) {
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
fun InfoRow(
    title: String,
    subtitle: String?,
    enabled: Boolean = true,
    thumbnailUri: String? = null,
    accentArgb: Int? = null,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (accentArgb != null) {
                androidx.compose.foundation.layout.Box(
                    Modifier.width(6.dp).height(40.dp).clip(RoundedCornerShape(3.dp))
                        .background(androidx.compose.ui.graphics.Color(accentArgb)),
                )
                Spacer(Modifier.width(12.dp))
            }
            if (thumbnailUri != null) {
                Thumb(thumbnailUri, 56.dp)
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CenterMessage(title: String?, body: String, pad: PaddingValues = PaddingValues(), action: @Composable () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(pad).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (title != null) Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        action()
    }
}

@Composable
fun InputDialog(
    @androidx.annotation.StringRes title: Int,
    initial: String,
    singleLine: Boolean = true,
    onDismiss: () -> Unit,
    onOk: (String) -> Unit,
) {
    var text by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(initial) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                text, { text = it }, singleLine = singleLine,
                label = { Text(stringResource(R.string.title_label)) },
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(enabled = text.isNotBlank(), onClick = { onOk(text); onDismiss() }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun progressText(classes: List<com.learvia.app.data.ClassEntity>): String? {
    val playable = classes.filter { it.mediaUri != null }
    if (playable.isEmpty()) return null
    return stringResource(R.string.progress_fmt, playable.count { it.completed }, playable.size)
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
}
