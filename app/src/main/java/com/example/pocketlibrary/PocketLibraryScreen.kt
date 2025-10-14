package com.example.pocketlibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


@Composable
fun PocketLibrarySearchScreen(
    vm: BookViewModel = viewModel(),
    modifier: Modifier,
    libraryViewModel: LibraryViewModel
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = { Text("Search Books") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { vm.search() }
            )
        )

        Spacer(Modifier.height(12.dp))

        Box(Modifier.fillMaxSize()) {
            when {
                state.loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.results.isEmpty() && state.query.isNotEmpty() -> {
                    Text("No results", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(140.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.results) { book ->
                            Card(
                                modifier = Modifier
                                    .aspectRatio(0.7f)

                                    // when you click the card it saves it to my library
                                    .clickable {
                                        val fave = FavouriteBook(
                                            title = book.title ?:"",
                                            author = book.authorNames?.joinToString(",") ?: "",
                                            year = book.firstPublishYear,
                                            cover =  book.coverImageUrl
                                        )
                                        libraryViewModel.addBook(fave)
                                        Toast.makeText(context,"${book.title} has been saved to library",Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                Column {
                                    AsyncImage(
                                        model = book.coverImageUrl,
                                        contentDescription = book.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = book.title ?: "Unknown title",
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = book.authorNames?.joinToString(", ") ?: "Unknown author",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = book.firstPublishYear?.toString() ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


