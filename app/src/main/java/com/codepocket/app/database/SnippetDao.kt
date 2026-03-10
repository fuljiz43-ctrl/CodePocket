package com.codepocket.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.codepocket.app.model.Snippet

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSnippets(): LiveData<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE category = :category ORDER BY isPinned DESC, updatedAt DESC")
    fun getSnippetsByCategory(category: String): LiveData<List<Snippet>>

    @Query("""
        SELECT * FROM snippets 
        WHERE title LIKE '%' || :query || '%' 
        OR code LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR tags LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchSnippets(query: String): LiveData<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE id = :id")
    suspend fun getSnippetById(id: Long): Snippet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: Snippet): Long

    @Update
    suspend fun updateSnippet(snippet: Snippet)

    @Delete
    suspend fun deleteSnippet(snippet: Snippet)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteSnippetById(id: Long)

    @Query("SELECT * FROM snippets")
    suspend fun getAllSnippetsSync(): List<Snippet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snippets: List<Snippet>)

    @Query("UPDATE snippets SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: Long, isPinned: Boolean)
}
