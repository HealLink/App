package com.rega.heallink.ui.list

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rega.heallink.data.local.Note
import com.rega.heallink.databinding.ItemNoteBinding

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()

    class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.tvItemTitle.text = note.title
            binding.tvItemDescription.text = note.description
            binding.tvItemDate.text = note.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    override fun getItemCount() = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(newNotes: List<Note>) {
        if (newNotes.isNullOrEmpty()) {
            Log.d("NoteAdapter", "No notes available")
        } else {
            notes.clear()
            notes.addAll(newNotes)
            notifyDataSetChanged()
        }
    }

}
