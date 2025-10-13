package com.example.pocketlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<Book> = emptyList()
)

class BookViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var searchJob: Job? = null

    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            search()
        }
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isEmpty()) {
            _state.value = _state.value.copy(results = emptyList(), error = null, loading = false)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val searchResponse = Network.openLibraryApi.searchBooks(
                    query = q,
                    fields = "key,title,author_name,first_publish_year,cover_i",
                    limit = 30
                )
                val books = searchResponse.docs.map { doc ->
                    Book(
                        title = doc.title,
                        authorNames = doc.authorNames,
                        firstPublishYear = doc.firstPublishYear,
                        coverId = doc.coverId
                    )}
                _state.value = _state.value.copy(results = books, loading = false)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = t.message ?: "Something went wrong"
                )
            }
        }
    }
}
