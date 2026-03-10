package com.codepocket.app.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codepocket.app.databinding.ItemSnippetBinding
import com.codepocket.app.model.Categories
import com.codepocket.app.model.Snippet

class SnippetAdapter(
    private val onItemClick: (Snippet) -> Unit,
    private val onItemLongClick: (Snippet) -> Unit,
    private val onCopyClick: (Snippet) -> Unit,
    private val onMenuClick: (Snippet, View) -> Unit,
    private val selectedIds: () -> Set<Long>
) : ListAdapter<Snippet, SnippetAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Snippet>() {
            override fun areItemsTheSame(old: Snippet, new: Snippet) = old.id == new.id
            override fun areContentsTheSame(old: Snippet, new: Snippet) = old == new
        }
    }

    inner class ViewHolder(private val binding: ItemSnippetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(snippet: Snippet) {
            val isSelected = snippet.id in selectedIds()

            binding.tvTitle.text = snippet.title
            binding.tvCode.text = snippet.code
            binding.tvCategory.text = "${Categories.getIcon(snippet.category)} ${snippet.category}"

            // Tags
            if (snippet.tags.isNotEmpty()) {
                binding.tvTags.visibility = View.VISIBLE
                binding.tvTags.text = snippet.tags.joinToString(" ") { "#$it" }
            } else {
                binding.tvTags.visibility = View.GONE
            }

            // Description
            if (snippet.description.isNotBlank()) {
                binding.tvDescription.visibility = View.VISIBLE
                binding.tvDescription.text = snippet.description
            } else {
                binding.tvDescription.visibility = View.GONE
            }

            // Pin indicator
            binding.ivPin.visibility = if (snippet.isPinned) View.VISIBLE else View.GONE

            // Selection state
            binding.root.isActivated = isSelected
            binding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Category color indicator
            try {
                binding.viewCategoryBar.setBackgroundColor(Categories.getColor(snippet.category))
            } catch (e: Exception) {}

            binding.root.setOnClickListener { onItemClick(snippet) }
            binding.root.setOnLongClickListener { onItemLongClick(snippet); true }
            binding.btnCopy.setOnClickListener { onCopyClick(snippet) }
            binding.btnMore.setOnClickListener { onMenuClick(snippet, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSnippetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun notifySelectionChanged() {
        notifyDataSetChanged()
    }
}
