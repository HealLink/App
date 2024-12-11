package com.heallinkapp.ui.list

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heallinkapp.R
import com.heallinkapp.data.local.Note
import com.heallinkapp.databinding.ItemNoteBinding

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()

    class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            val classMapping = mapOf(
                0 to "Anxiety",
                1 to "Bipolar",
                2 to "Depression",
                3 to "Normal",
                4 to "Personality disorder",
                5 to "Stress",
                6 to "Suicidal"
            )

            val colorMapping = mapOf(
                "Anxiety" to R.color.anxiety_color,
                "Bipolar" to R.color.bipolar_color,
                "Depression" to R.color.depression_color,
                "Normal" to R.color.normal_color,
                "Personality disorder" to R.color.personality_disorder_color,
                "Stress" to R.color.stress_color,
                "Suicidal" to R.color.suicidal_color
            )

            val sortedResults = note.result?.mapIndexed { index, confidence ->
                classMapping[index] to confidence
            }?.sortedByDescending { it.second }

            val highestCategory = sortedResults?.firstOrNull()?.first

            binding.tvItemTitle.text = note.title
            binding.tvItemDescription.text = note.description
            binding.tvItemDate.text = note.date

            val colorRes = colorMapping[highestCategory] ?: android.R.color.white
            binding.cvItemNote.setCardBackgroundColor(binding.root.context.getColor(colorRes))
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

