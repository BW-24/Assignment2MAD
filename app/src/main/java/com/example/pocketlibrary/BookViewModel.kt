package com.example.pocketlibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class UiState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<Book> = emptyList()
)

class BookViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val QUERY_KEY = "search_query"
    }

    private val _state = MutableStateFlow(
        UiState(query = savedStateHandle.get<String>(QUERY_KEY) ?: "")
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        state
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300L)
            .onEach { queryText ->
                search(queryText)
            }
            .launchIn(viewModelScope)
    }

    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        savedStateHandle[QUERY_KEY] = q
    }

    private fun search(title: String) {
        val trimmedQuery = title.trim()
        if (trimmedQuery.isEmpty()) {
            _state.value = _state.value.copy(results = emptyList(), loading = false)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val searchResponse = Network.openLibraryApi.searchBooks(
                    query = trimmedQuery,
                    fields = "key,title,author_name,first_publish_year,cover_i",
                    limit = 30
                )
                val books = searchResponse.docs.map { doc ->
                    Book(
                        title = doc.title,
                        authorNames = doc.authorNames,
                        firstPublishYear = doc.firstPublishYear,
                        coverId = doc.coverId
                    )
                }
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
