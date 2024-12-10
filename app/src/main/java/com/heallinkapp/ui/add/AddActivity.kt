package com.heallinkapp.ui.add

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.heallinkapp.R
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.local.Note
import com.heallinkapp.databinding.ActivityAddBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.helper.DateHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private val noteAddViewModel: NoteAddViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(this))
    }
    val userRepository = Injection.provideUserRepository(this)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val username = userRepository.userName.firstOrNull() ?: "User"
            binding.textGreet.text = "Hi $username\nTell me your story!"
        }

        binding.btnSubmit.setOnClickListener {
            insertNote()
        }

    }

    private fun insertNote() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        // Cek apakah title atau description kosong
        if (title.isEmpty() || description.isEmpty()) {
            // Tambahkan log untuk debugging
            Log.d("AddActivity", "Title or description is empty")

            // Tampilkan Toast jika ada field yang kosong
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        // Jika tidak kosong, lanjutkan untuk membuat note
        val note = Note(
            title = title,
            description = description,
            date = DateHelper.getCurrentDate()
        )

        // Insert note ke dalam view model
        noteAddViewModel.insert(note)

        // Tampilkan Toast bahwa catatan berhasil ditambahkan
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show()

        // Tutup activity
        finish()
    }

}