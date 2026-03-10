package com.codepocket.app.database

import androidx.lifecycle.LiveData
import com.codepocket.app.model.Snippet

class SnippetRepository(private val dao: SnippetDao) {

    val allSnippets: LiveData<List<Snippet>> = dao.getAllSnippets()

    fun getByCategory(category: String): LiveData<List<Snippet>> =
        dao.getSnippetsByCategory(category)

    fun search(query: String): LiveData<List<Snippet>> =
        dao.searchSnippets(query)

    suspend fun insert(snippet: Snippet): Long = dao.insertSnippet(snippet)

    suspend fun update(snippet: Snippet) = dao.updateSnippet(snippet)

    suspend fun delete(snippet: Snippet) = dao.deleteSnippet(snippet)

    suspend fun deleteById(id: Long) = dao.deleteSnippetById(id)

    suspend fun getAllSync(): List<Snippet> = dao.getAllSnippetsSync()

    suspend fun insertAll(snippets: List<Snippet>) = dao.insertAll(snippets)

    suspend fun togglePin(id: Long, isPinned: Boolean) = dao.updatePinned(id, isPinned)
}
