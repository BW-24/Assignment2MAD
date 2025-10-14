package com.example.pocketlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
fun OfflineLibraryScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).favouriteBookDao() }

    val vm: LibraryViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LibraryViewModel(dao) }
        }
    )

    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search Favourite Books")},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(state.faves) { book ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = book.cover?.let{"https://covers.openlibrary.org/b/id/${it}-M.jpg"} ,
                            contentDescription = book.title,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(book.title, style = MaterialTheme.typography.titleMedium)
                            Text(book.author.toString(), style = MaterialTheme.typography.bodyMedium)
                            Text(book.year?.toString() ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { updateWindow(book, LibraryViewModel) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Edit")
                        }
                        IconButton(onClick = { vm.removeBook(book) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun updateWindow(book: FavouriteBook, vm: LibraryViewModel)
{
    var title by remember { mutableStateOf(book.title ?: "") }
    var author by remember { mutableStateOf(book.author ?: "") }
    var year by remember { mutableStateOf(book.year?.toString() ?: "") }
    var openWindow by remember { mutableStateOf(true) }

    if(openWindow)
    {
        AlertDialog(
            onDismissRequest = {openWindow = false},
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
                    val updatedBook = book.copy(
                        title = title,
                        author = author,
                        year = year.toIntOrNull()
                    )
                    vm.updateBook(updatedBook)
                    openWindow = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { openWindow = false }) { Text("Cancel") }
            }
        )
    }
}