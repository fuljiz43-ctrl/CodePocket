package com.codepocket.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codepocket.app.adapter.SnippetAdapter
import com.codepocket.app.databinding.ActivityMainBinding
import com.codepocket.app.model.Categories
import com.codepocket.app.model.Snippet
import com.codepocket.app.utils.ImportExportUtil
import com.codepocket.app.utils.Prefs
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SnippetViewModel
    private lateinit var adapter: SnippetAdapter

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportToUri(it) }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SnippetViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        setupFab()
        setupMultiSelectBar()
        observeData()
    }

    private fun applyTheme() {
        val isDark = Prefs.isDarkMode(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.btnDarkMode.setOnClickListener {
            val isDark = Prefs.isDarkMode(this)
            Prefs.setDarkMode(this, !isDark)
            AppCompatDelegate.setDefaultNightMode(
                if (!isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        updateDarkModeIcon()

        binding.btnExport.setOnClickListener { showExportImportMenu(it) }
    }

    private fun updateDarkModeIcon() {
        val isDark = Prefs.isDarkMode(this)
        binding.btnDarkMode.setImageResource(
            if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        )
    }

    private fun showExportImportMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_export_import, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export -> {
                    exportLauncher.launch(ImportExportUtil.getExportFileName())
                    true
                }
                R.id.action_import -> {
                    importLauncher.launch("application/json")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        adapter = SnippetAdapter(
            onItemClick = { snippet ->
                if (viewModel.isMultiSelectMode.value == true) {
                    viewModel.toggleSelection(snippet.id)
                    adapter.notifySelectionChanged()
                } else {
                    openAddEditActivity(snippet)
                }
            },
            onItemLongClick = { snippet ->
                viewModel.toggleSelection(snippet.id)
                adapter.notifySelectionChanged()
            },
            onCopyClick = { snippet -> copyToClipboard(snippet.code, snippet.title) },
            onMenuClick = { snippet, view -> showSnippetPopupMenu(snippet, view) },
            selectedIds = { viewModel.selectedSnippets.value ?: emptySet() }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(false)
        }
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setCategory(null)
        }

        val categoryChips = mapOf(
            binding.chipBash to Categories.BASH,
            binding.chipPython to Categories.PYTHON,
            binding.chipJs to Categories.JAVASCRIPT,
            binding.chipHtml to Categories.HTML,
            binding.chipGit to Categories.GIT,
            binding.chipCustom to Categories.CUSTOM
        )

        categoryChips.forEach { (chip, category) ->
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) viewModel.setCategory(category)
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.isBlank()) {
                    viewModel.setCategory(null)
                    binding.chipAll.isChecked = true
                } else {
                    viewModel.setSearch(query)
                }
                binding.btnClearSearch.visibility =
                    if (query.isNotBlank()) View.VISIBLE else View.GONE
            }
        })
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { openAddEditActivity(null) }
    }

    private fun setupMultiSelectBar() {
        binding.btnCancelSelect.setOnClickListener {
            viewModel.clearSelection()
            adapter.notifySelectionChanged()
        }

        binding.btnSelectAll.setOnClickListener {
            viewModel.selectAll()
            adapter.notifySelectionChanged()
        }

        binding.btnDeleteSelected.setOnClickListener {
            val count = viewModel.selectedSnippets.value?.size ?: 0
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete $count Snippet")
                .setMessage("Yakin ingin menghapus $count snippet?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.deleteSelected()
                    adapter.notifySelectionChanged()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnCopySelected.setOnClickListener {
            val snippets = viewModel.getSelectedSnippets()
            if (snippets.isEmpty()) return@setOnClickListener
            val combined = snippets.joinToString("\n\n# ---\n\n") { snippet ->
                "# ${snippet.title}\n${snippet.code}"
            }
            copyToClipboard(combined, "${snippets.size} Snippets")
        }
    }

    private fun observeData() {
        viewModel.displaySnippets.observe(this) { snippets ->
            adapter.submitList(snippets)
            binding.tvEmpty.visibility = if (snippets.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isMultiSelectMode.observe(this) { isMulti ->
            binding.multiSelectBar.visibility = if (isMulti) View.VISIBLE else View.GONE
            binding.fab.visibility = if (isMulti) View.GONE else View.VISIBLE
        }

        viewModel.selectedSnippets.observe(this) { selected ->
            binding.tvSelectCount.text = "${selected.size} dipilih"
        }
    }

    private fun showSnippetPopupMenu(snippet: Snippet, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_snippet_item, popup.menu)
        popup.menu.findItem(R.id.action_pin).title =
            if (snippet.isPinned) "Unpin" else "Pin"

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> { openAddEditActivity(snippet); true }
                R.id.action_copy -> { copyToClipboard(snippet.code, snippet.title); true }
                R.id.action_pin -> { viewModel.togglePin(snippet); true }
                R.id.action_delete -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Hapus Snippet")
                        .setMessage("Hapus \"${snippet.title}\"?")
                        .setPositiveButton("Hapus") { _, _ -> viewModel.delete(snippet) }
                        .setNegativeButton("Batal", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun openAddEditActivity(snippet: Snippet?) {
        val intent = Intent(this, AddEditActivity::class.java)
        snippet?.let { intent.putExtra("snippet_id", it.id) }
        startActivity(intent)
    }

    private fun copyToClipboard(text: String, label: String = "Code") {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "✅ Copied: $label", Toast.LENGTH_SHORT).show()
    }

    private fun exportToUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snippets = viewModel.getAllForExport()
                val json = ImportExportUtil.exportToJson(snippets)
                contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "✅ Exported ${snippets.size} snippets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "❌ Export gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun importFromUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = ImportExportUtil.readFromUri(this@MainActivity, uri)
                    ?: throw Exception("Tidak bisa membaca file")
                val snippets = ImportExportUtil.importFromJson(json)
                if (snippets.isEmpty()) throw Exception("Tidak ada snippet ditemukan")
                viewModel.importSnippets(snippets)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "✅ Imported ${snippets.size} snippets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "❌ Import gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.isMultiSelectMode.value == true) {
            viewModel.clearSelection()
            adapter.notifySelectionChanged()
        } else {
            super.onBackPressed()
        }
    }
}
