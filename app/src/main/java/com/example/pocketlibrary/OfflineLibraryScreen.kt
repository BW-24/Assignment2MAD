package com.example.pocketlibrary

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun OfflineLibraryScreen(
    modifier: Modifier,
    vm: LibraryViewModel,
    onLaunchCamera: () -> Unit,
    capturedImageUri: Uri?,
    onClearCapturedImageUri: () -> Unit,
    onShareBook: (FavouriteBook) -> Unit,
    onTakePhotoForBook: (FavouriteBook) -> Unit
) {
    val state by vm.state.collectAsState()

    var updateWindow by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<FavouriteBook?>(null) }

    var showCameraBookPicker by remember { mutableStateOf(false) }
    var showShareBookPicker by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            showCameraBookPicker = true
        }
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::updateQuery,
                label = { Text("Search Favourite Books") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.faves) { book ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = book.cover ?: R.drawable.ic_launcher_background,
                                contentDescription = book.title,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(book.title, style = MaterialTheme.typography.titleMedium)
                                Text(book.author.toString(), style = MaterialTheme.typography.bodyMedium)
                                Text(book.year?.toString() ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onTakePhotoForBook(book) }) {
                                Icon(Icons.Default.Add, contentDescription = "Take Photo")
                            }
                            IconButton(onClick = { onShareBook(book) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(onClick = onLaunchCamera) {
                Icon(Icons.Default.Add, contentDescription = "Take Photo")
            }
            FloatingActionButton(onClick = { showShareBookPicker = true }) {
                Icon(Icons.Default.Share, contentDescription = "Share Book")
            }
        }
    }

    if (updateWindow && selectedBook != null) {
        var title by remember { mutableStateOf(selectedBook!!.title) }
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

    if (showCameraBookPicker) {
        BookPickerDialog(
            title = "Select a book to link photo",
            books = state.faves,
            onDismiss = {
                showCameraBookPicker = false
                onClearCapturedImageUri()
            },
            onBookSelected = { book ->
                capturedImageUri?.let { uri ->
                    vm.updateBook(book.copy(cover = uri.toString()))
                }
                showCameraBookPicker = false
                onClearCapturedImageUri()
            }
        )
    }

    if (showShareBookPicker) {
        BookPickerDialog(
            title = "Select a book to share",
            books = state.faves,
            onDismiss = { showShareBookPicker = false },
            onBookSelected = { book ->
                onShareBook(book)
                showShareBookPicker = false
            }
        )
    }
}

private fun LibraryViewModel.saveScrollPosition(
    index: Int,
    offset: Int
) {
}

@Composable
private fun BookPickerDialog(
    title: String,
    books: List<FavouriteBook>,
    onDismiss: () -> Unit,
    onBookSelected: (FavouriteBook) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No books in library.", textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(books) { book ->
                        Text(
                            text = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookSelected(book) }
                                .padding(vertical = 12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}