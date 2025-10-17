package com.example.pocketlibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val faves: List<FavouriteBook> = emptyList(),
    val query: String = "",
    val initialListIndex: Int = 0,
    val initialListOffset: Int = 0
)

class LibraryViewModel(
    private val dao: FavouriteBookDAO,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val SCROLL_INDEX_KEY = "library_scroll_index"
        private const val SCROLL_OFFSET_KEY = "library_scroll_offset"
    }

    private val _state = MutableStateFlow(
        LibraryUiState(
            initialListIndex = savedStateHandle.get<Int>(SCROLL_INDEX_KEY) ?: 0,
            initialListOffset = savedStateHandle.get<Int>(SCROLL_OFFSET_KEY) ?: 0
        )
    )
    val state: StateFlow<LibraryUiState> = _state

    init {
        load()
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        if (index != savedStateHandle.get(SCROLL_INDEX_KEY) || offset != savedStateHandle.get(SCROLL_OFFSET_KEY)) {
            savedStateHandle[SCROLL_INDEX_KEY] = index
            savedStateHandle[SCROLL_OFFSET_KEY] = offset
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(faves = dao.getAll())
        }
    }

    fun addBook(book: FavouriteBook) {
        viewModelScope.launch {
            dao.insert(book)
            load()
        }
    }

    fun removeBook(book: FavouriteBook) {
        viewModelScope.launch {
            dao.delete(book)
            load()
        }
    }

    fun updateBook(book: FavouriteBook) {
        viewModelScope.launch {
            dao.update(book)
            load()
        }
    }

    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        viewModelScope.launch {
            if (q.isEmpty()) {
                load()
            } else {
                _state.value = _state.value.copy(faves = dao.search(q))
            }
        }
    }
}