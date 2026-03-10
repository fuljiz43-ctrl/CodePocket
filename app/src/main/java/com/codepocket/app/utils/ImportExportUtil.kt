package com.codepocket.app.utils

import android.content.Context
import android.net.Uri
import com.codepocket.app.model.Snippet
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

object ImportExportUtil {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    data class ExportData(
        val version: String = "1.0",
        val exportedAt: Long = System.currentTimeMillis(),
        val snippets: List<Snippet>
    )

    fun exportToJson(snippets: List<Snippet>): String {
        val data = ExportData(snippets = snippets)
        return gson.toJson(data)
    }

    fun importFromJson(json: String): List<Snippet> {
        return try {
            // Try new format first
            val type = object : TypeToken<ExportData>() {}.type
            val data = gson.fromJson<ExportData>(json, type)
            data.snippets
        } catch (e: Exception) {
            // Try legacy list format
            try {
                val type = object : TypeToken<List<Snippet>>() {}.type
                gson.fromJson(json, type)
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            reader.close()
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getExportFileName(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        return "codepocket_snippets_$timestamp.json"
    }
}
