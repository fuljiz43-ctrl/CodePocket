package com.codepocket.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "snippets")
@TypeConverters(TagConverter::class)
data class Snippet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val code: String,
    val category: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

class TagConverter {
    @TypeConverter
    fun fromList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isBlank()) emptyList() else data.split(",").map { it.trim() }
}

object Categories {
    const val BASH = "Bash"
    const val PYTHON = "Python"
    const val JAVASCRIPT = "JavaScript"
    const val HTML = "HTML"
    const val GIT = "Git"
    const val CUSTOM = "Custom"

    val ALL = listOf(BASH, PYTHON, JAVASCRIPT, HTML, GIT, CUSTOM)

    fun getIcon(category: String): String = when (category) {
        BASH -> "🖥️"
        PYTHON -> "🐍"
        JAVASCRIPT -> "⚡"
        HTML -> "🌐"
        GIT -> "🔀"
        CUSTOM -> "📝"
        else -> "📝"
    }

    fun getColor(category: String): Int = when (category) {
        BASH -> 0xFF4CAF50.toInt()
        PYTHON -> 0xFF2196F3.toInt()
        JAVASCRIPT -> 0xFFFFC107.toInt()
        HTML -> 0xFFFF5722.toInt()
        GIT -> 0xFFE91E63.toInt()
        CUSTOM -> 0xFF9C27B0.toInt()
        else -> 0xFF607D8B.toInt()
    }
}
