package com.codepocket.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.codepocket.app.database.AppDatabase
import com.codepocket.app.database.SnippetRepository
import com.codepocket.app.model.Snippet
import kotlinx.coroutines.launch

class SnippetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SnippetRepository
    val allSnippets: LiveData<List<Snippet>>

    private val _currentCategory = MutableLiveData<String?>(null)
    private val _searchQuery = MutableLiveData<String>("")
    private val _selectedSnippets = MutableLiveData<Set<Long>>(emptySet())

    val selectedSnippets: LiveData<Set<Long>> = _selectedSnippets
    val isMultiSelectMode: LiveData<Boolean> = _selectedSnippets.map { it.isNotEmpty() }

    val displaySnippets: MediatorLiveData<List<Snippet>> = MediatorLiveData()

    private var categorySource: LiveData<List<Snippet>>? = null
    private var searchSource: LiveData<List<Snippet>>? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SnippetRepository(db.snippetDao())
        allSnippets = repository.allSnippets

        displaySnippets.addSource(allSnippets) { updateDisplay() }
        displaySnippets.addSource(_currentCategory) { updateDisplay() }
        displaySnippets.addSource(_searchQuery) { updateDisplay() }
    }

    private fun updateDisplay() {
        val query = _searchQuery.value ?: ""
        val category = _currentCategory.value

        categorySource?.let { displaySnippets.removeSource(it) }
        searchSource?.let { displaySnippets.removeSource(it) }

        when {
            query.isNotBlank() -> {
                val src = repository.search(query)
                searchSource = src
                displaySnippets.addSource(src) { displaySnippets.value = it }
            }
            category != null -> {
                val src = repository.getByCategory(category)
                categorySource = src
                displaySnippets.addSource(src) { displaySnippets.value = it }
            }
            else -> {
                displaySnippets.value = allSnippets.value ?: emptyList()
            }
        }
    }

    fun setCategory(category: String?) {
        _currentCategory.value = category
        _searchQuery.value = ""
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        _currentCategory.value = null
    }

    fun insert(snippet: Snippet) = viewModelScope.launch { repository.insert(snippet) }

    fun update(snippet: Snippet) = viewModelScope.launch { repository.update(snippet) }

    fun delete(snippet: Snippet) = viewModelScope.launch { repository.delete(snippet) }

    fun deleteById(id: Long) = viewModelScope.launch { repository.deleteById(id) }

    fun togglePin(snippet: Snippet) = viewModelScope.launch {
        repository.togglePin(snippet.id, !snippet.isPinned)
    }

    suspend fun getAllForExport(): List<Snippet> = repository.getAllSync()

    fun importSnippets(snippets: List<Snippet>) = viewModelScope.launch {
        repository.insertAll(snippets.map { it.copy(id = 0) })
    }

    // Multi-select
    fun toggleSelection(id: Long) {
        val current = _selectedSnippets.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedSnippets.value = current
    }

    fun clearSelection() { _selectedSnippets.value = emptySet() }

    fun selectAll() {
        _selectedSnippets.value = displaySnippets.value?.map { it.id }?.toSet() ?: emptySet()
    }

    fun deleteSelected() = viewModelScope.launch {
        _selectedSnippets.value?.forEach { repository.deleteById(it) }
        clearSelection()
    }

    fun getSelectedSnippets(): List<Snippet> {
        val ids = _selectedSnippets.value ?: return emptyList()
        return displaySnippets.value?.filter { it.id in ids } ?: emptyList()
    }
}
