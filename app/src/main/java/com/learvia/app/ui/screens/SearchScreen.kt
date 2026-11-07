package com.learvia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.learvia.app.R
import com.learvia.app.ui.Hit
import com.learvia.app.ui.LibraryViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(vm: LibraryViewModel, onBack: () -> Unit, onRoute: (String) -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<Hit>>(emptyList()) }
    LaunchedEffect(q) { delay(250); hits = vm.search(q) }

    ScreenScaffold(stringResource(R.string.search), onBack) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = spacedBy(12.dp)) {
            OutlinedTextField(q, { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_hint)) })
            when {
                q.isBlank() -> Text(stringResource(R.string.search_scope))
                hits.isEmpty() -> Text(stringResource(R.string.no_results))
                else -> LazyColumn(verticalArrangement = spacedBy(8.dp)) {
                    itemsIndexed(hits) { _, h ->
                        InfoRow(h.title, listOfNotNull(stringResource(h.kind), h.subtitle).joinToString(" · ")) { onRoute(h.route) }
                    }
                }
            }
        }
    }
}
