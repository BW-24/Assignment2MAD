package com.example.pocketlibrary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pocketlibrary.ui.theme.PocketLibraryTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLibraryTheme {

                val context = LocalContext.current
                val dao = remember { AppDatabase.getDatabase(context).favouriteBookDao() }

                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { LibraryViewModel(dao, createSavedStateHandle()) }
                    }
                )

                val bookViewModel: BookViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { BookViewModel(createSavedStateHandle()) }
                    }
                )

                var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

                fun createImageUri(context: Context): Uri {
                    val file = File(
                        context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
                        "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                    )
                    return FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }

                val takePictureLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicture()
                ) { success ->
                    if (!success) {
                        capturedImageUri = null
                    }
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        val newImageUri = createImageUri(context)
                        capturedImageUri = newImageUri
                        takePictureLauncher.launch(newImageUri)
                    }
                }

                val launchCamera: () -> Unit = {
                    capturedImageUri = null
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }

                val launchShareIntent: (FavouriteBook) -> Unit = { book ->
                    val shareText = """
                        I'd like to recommend a book to you:
                        Title: ${book.title}
                        Author: ${book.author ?: "Unknown"}
                        Year: ${book.year?.toString() ?: "Unknown"}
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Book Recommendation: ${book.title}")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(
                        Intent.createChooser(intent, "Share this book via...")
                    )
                }


                var tab by remember { mutableStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                label = { Text("Search") },
                                selected = tab == 0,
                                onClick = { tab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Star, contentDescription = "My Library") },
                                label = { Text("My Library") },
                                selected = tab == 1,
                                onClick = { tab = 1 }
                            )
                        }
                    }
                ) { innerPadding ->

                    val modifier = Modifier.padding(innerPadding)
                    when (tab) {
                        0 -> PocketLibrarySearchScreen(
                            vm = bookViewModel,
                            modifier = modifier,
                            libraryViewModel = libraryViewModel
                        )
                        1 -> OfflineLibraryScreen(
                            modifier = modifier,
                            vm = libraryViewModel,
                            onLaunchCamera = launchCamera,
                            capturedImageUri = capturedImageUri,
                            onClearCapturedImageUri = { capturedImageUri = null },
                            onShareBook = launchShareIntent,
                            onTakePhotoForBook = { book ->
                            }
                        )
                    }
                }
            }
        }
    }
}