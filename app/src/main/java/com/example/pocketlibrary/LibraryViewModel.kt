package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// this is for the offline local storage and search
data class LibraryUiState(
    val faves: List<FavouriteBook> = emptyList(),
    val query: String = ""
)

class LibraryViewModel(private val dao: FavouriteBookDAO): ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state

    init {
        load()
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
            if(q.isEmpty()) {
                load()
            }
            else {
                _state.value= _state.value.copy(faves = dao.search(q))
            }
        }
    }
}

