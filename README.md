All image elements (AsyncImage, Icon) have a contentDescription set.

Text size and contrast are managed uniformly by MaterialTheme, conforming to standard practices and ensuring readability. Therefore, no modifications are needed in this part.




Modification File Details：

1. BookViewModel.kt
To restore the last search query after an app restart, we use SavedStateHandle. It is a key-value map that can survive process termination.

We add SavedStateHandle to the BookViewModel's constructor.

In the init block, read the saved search query from savedStateHandle to initialize the state. If a saved query exists, automatically trigger a search.

In the updateQuery function, update the new search query to both _state and savedStateHandle.



2. PocketLibraryScreen.kt
To maintain the scroll position of the search results list after screen rotation, we use rememberLazyGridState.

Inside the PocketLibrarySearchScreen Composable function, create a gridState instance.

Pass this gridState to the state parameter of LazyVerticalGrid. Compose will automatically handle state saving and restoration during screen rotation.



3. LibraryViewModel.kt
To restore the scroll position of "My Library" after an app restart, we also use SavedStateHandle.

Modify the LibraryUiState data class by adding initialListIndex and initialListOffset fields to store the initial scroll position.

Add SavedStateHandle to the LibraryViewModel's constructor.

In the init block, read the saved scroll position from savedStateHandle and use it to initialize _state.

Add a new saveScrollPosition function to receive the current scroll position from the UI layer and save it to savedStateHandle.



4. OfflineLibraryScreen.kt
This is the core part for implementing persistent scroll position.

Use rememberLazyListState() to create the list state listState.

Use a LaunchedEffect(state.faves) that executes once after the book list is loaded, calling listState.scrollToItem to restore the last saved scroll position.

Use another LaunchedEffect(listState) that creates a snapshotFlow to listen for changes in the scroll position. Whenever the position changes, call the ViewModel's saveScrollPosition function to persist the new position.

Pass the listState to LazyColumn.



5. MainActivity.kt
Because we modified the LibraryViewModel's constructor by adding a SavedStateHandle parameter, we need to update the factory method that creates it.

In the initializer block of viewModelFactory, call createSavedStateHandle() to obtain a SavedStateHandle instance.

Pass the obtained dao and savedStateHandle to the LibraryViewModel's constructor.
