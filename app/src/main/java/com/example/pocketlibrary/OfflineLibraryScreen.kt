package com.example.pocketlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

// my library screen that can be seen offline
@Composable
fun OfflineLibraryScreen(modifier: Modifier, vm: LibraryViewModel) {
    val state by vm.state.collectAsState()

    var updateWindow by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<FavouriteBook?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(state.faves) {
        if (state.faves.isNotEmpty()) {
            listState.scrollToItem(
                index = state.initialListIndex,
                scrollOffset = state.initialListOffset
            )
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                vm.saveScrollPosition(index, offset)
            }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search Favourite Books")},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.faves) { book ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = book.cover,
                            contentDescription = book.title,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(book.title, style = MaterialTheme.typography.titleMedium)
                            Text(book.author.toString(), style = MaterialTheme.typography.bodyMedium)
                            Text(book.year?.toString() ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = {
                            selectedBook = book
                            updateWindow = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { vm.removeBook(book) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
    if(updateWindow && selectedBook != null) {
        var title by remember { mutableStateOf(selectedBook!!.title ?: "") }
        var author by remember { mutableStateOf(selectedBook!!.author ?: "") }
        var year by remember { mutableStateOf(selectedBook!!.year?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = {updateWindow = false},
            title = { Text("Edit Book") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author") }
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedBook = selectedBook!!.copy(
                        title = title,
                        author = author,
                        year = year.toIntOrNull()
                    )
                    vm.updateBook(updatedBook)
                    updateWindow = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { updateWindow = false }) { Text("Cancel") }
            }
        )
    }
}