package com.codepocket.app

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.codepocket.app.databinding.ActivityAddEditBinding
import com.codepocket.app.model.Categories
import com.codepocket.app.model.Snippet
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var viewModel: SnippetViewModel
    private var editingSnippet: Snippet? = null
    private var snippetId: Long = -1L
    private val tagList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SnippetViewModel::class.java]

        snippetId = intent.getLongExtra("snippet_id", -1L)
        val isEdit = snippetId != -1L

        setupToolbar(isEdit)
        setupCategorySpinner()
        setupTagInput()

        if (isEdit) loadSnippet(snippetId)

        binding.btnSave.setOnClickListener { saveSnippet() }
    }

    private fun setupToolbar(isEdit: Boolean) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEdit) "Edit Snippet" else "Snippet Baru"
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Categories.ALL
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupTagInput() {
        binding.btnAddTag.setOnClickListener {
            val tag = binding.etTag.text?.toString()?.trim()
                ?.removePrefix("#")?.lowercase() ?: ""
            if (tag.isNotBlank() && !tagList.contains(tag)) {
                tagList.add(tag)
                addTagChip(tag)
                binding.etTag.text?.clear()
            }
        }
    }

    private fun addTagChip(tag: String) {
        val chip = Chip(this).apply {
            text = "#$tag"
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                tagList.remove(tag)
                binding.chipGroupTags.removeView(this)
            }
        }
        binding.chipGroupTags.addView(chip)
    }

    private fun loadSnippet(id: Long) {
        lifecycleScope.launch {
            // Observe via allSnippets and find by id
            viewModel.allSnippets.observe(this@AddEditActivity) { snippets ->
                val snippet = snippets.find { it.id == id } ?: return@observe
                if (editingSnippet == null) {
                    editingSnippet = snippet
                    populateFields(snippet)
                }
            }
        }
    }

    private fun populateFields(snippet: Snippet) {
        binding.etTitle.setText(snippet.title)
        binding.etCode.setText(snippet.code)
        binding.etDescription.setText(snippet.description)

        val idx = Categories.ALL.indexOf(snippet.category)
        if (idx >= 0) binding.spinnerCategory.setSelection(idx)

        tagList.clear()
        tagList.addAll(snippet.tags)
        binding.chipGroupTags.removeAllViews()
        snippet.tags.forEach { addTagChip(it) }
    }

    private fun saveSnippet() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        val code = binding.etCode.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val category = binding.spinnerCategory.selectedItem?.toString() ?: Categories.BASH

        if (title.isBlank()) {
            binding.etTitle.error = "Judul tidak boleh kosong"
            return
        }
        if (code.isBlank()) {
            binding.etCode.error = "Code tidak boleh kosong"
            return
        }

        val snippet = if (editingSnippet != null) {
            editingSnippet!!.copy(
                title = title,
                code = code,
                description = description,
                category = category,
                tags = tagList.toList(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            Snippet(
                title = title,
                code = code,
                description = description,
                category = category,
                tags = tagList.toList()
            )
        }

        if (editingSnippet != null) {
            viewModel.update(snippet)
            Toast.makeText(this, "✅ Snippet diperbarui", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(snippet)
            Toast.makeText(this, "✅ Snippet disimpan", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
