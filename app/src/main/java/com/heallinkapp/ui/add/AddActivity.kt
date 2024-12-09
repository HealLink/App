package com.heallinkapp.ui.add

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.heallinkapp.R
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.local.Note
import com.heallinkapp.databinding.ActivityAddBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.helper.DateHelper

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private val noteAddViewModel: NoteAddViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            insertNote()
        }

    }

    private fun insertNote() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        // Validate input
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new Note object
        val note = Note(
            title = title,
            description = description,
            date = DateHelper.getCurrentDate()
        )

        noteAddViewModel.insert(note)

        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show()

        finish()
    }
}