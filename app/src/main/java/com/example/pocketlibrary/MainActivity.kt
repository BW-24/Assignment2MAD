package com.example.pocketlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

import com.example.pocketlibrary.ui.theme.PocketLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLibraryTheme {


                val context = androidx.compose.ui.platform.LocalContext.current
                val dao = remember { AppDatabase.getDatabase(context).favouriteBookDao() }

                // this creates an instance of LibraryViewModel to be used
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { LibraryViewModel(dao) }
                    }
                )

                // added a navigation bar on the bottom of the screen to switch from main search and the saved library
                var tab by remember { mutableStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Search, contentDescription = "Search")},
                                label = { Text("Search")},
                                selected = tab == 0,
                                onClick = {tab = 0}
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Star, contentDescription = "My Library")},
                                label = { Text("My Library")},
                                selected = tab == 1,
                                onClick = {tab = 1}
                            )
                        }
                    }
                ) { innerPadding ->

                    val modifier = Modifier.padding(innerPadding)
                    when(tab) {
                        0 -> PocketLibrarySearchScreen(modifier = modifier, libraryViewModel = libraryViewModel)
                        1 -> OfflineLibraryScreen(modifier = modifier, vm = libraryViewModel)
                    }
                }


            }
        }
    }
}


