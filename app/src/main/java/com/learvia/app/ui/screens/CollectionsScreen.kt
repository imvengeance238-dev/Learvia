package com.learvia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learvia.app.R
import com.learvia.app.data.CollectionEntity
import com.learvia.app.ui.LibraryViewModel

/** Collections are only an organizing view; a course may be in several collections. */
@Composable
fun CollectionsScreen(vm: LibraryViewModel, onBack: () -> Unit) {
    val collections by vm.collections.collectAsStateWithLifecycle()
    val courses by vm.courses.collectAsStateWithLifecycle()
    val links by vm.links.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CollectionEntity?>(null) }
    var deleting by remember { mutableStateOf<CollectionEntity?>(null) }

    ScreenScaffold(
        stringResource(R.string.collections), onBack,
        fab = { FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, stringResource(R.string.add_collection)) } },
    ) { pad ->
        if (collections.isEmpty()) CenterMessage(null, stringResource(R.string.collections_empty), pad)
        else LazyColumn(contentPadding = contentPadding(pad), verticalArrangement = spacedBy(8.dp)) {
            items(collections, key = { it.id }) { col ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        InfoRow(col.name, stringResource(R.string.collection_count, links.count { it.collectionId == col.id })) { editing = col }
                    }
                    IconButton(onClick = { deleting = col }) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
                }
            }
        }
    }

    if (adding) InputDialog(R.string.add_collection, "", onDismiss = { adding = false }) { vm.addCollection(it) }

    editing?.let { col ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(col.name) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (courses.isEmpty()) Text(stringResource(R.string.empty_library_title))
                    courses.forEach { c ->
                        val on = links.any { it.collectionId == col.id && it.courseId == c.id }
                        Row(Modifier.fillMaxWidth().clickable { vm.setLink(col.id, c.id, !on) }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(on, { vm.setLink(col.id, c.id, it) })
                            Text(c.title)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.ok)) } },
        )
    }
    deleting?.let { col ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.collection_delete_body)) },
            confirmButton = { TextButton(onClick = { vm.deleteCollection(col.id); deleting = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
