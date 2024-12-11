package com.heallinkapp.ui.add

import android.annotation.SuppressLint
import android.content.Intent
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
import com.heallinkapp.data.remote.response.UploadRequest
import com.heallinkapp.data.remote.retrofit.ApiConfig
import com.heallinkapp.databinding.ActivityAddBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.helper.DateHelper
import com.heallinkapp.ui.ResultActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

        lifecycleScope.launchWhenStarted {
            noteAddViewModel.uploadResult.collect { response ->
                response?.let {
                    if (it.status == "success") {
                        val resultArray = it.data?.result?.map { value -> value.toFloat() }?.toFloatArray()

                        if (resultArray != null) {
                            val intent = Intent(this@AddActivity, ResultActivity::class.java).apply {
                                putExtra("RESULT_ARRAY", resultArray)
                            }
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@AddActivity, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        // Observe error
        lifecycleScope.launchWhenStarted {
            noteAddViewModel.error.collect { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(this@AddActivity, "Error: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            val sentence = binding.edtDescription.text.toString().trim()
            if (sentence.isNotEmpty()) {
                noteAddViewModel.uploadStory(sentence)
            } else {
                Toast.makeText(this, "Please enter a sentence!", Toast.LENGTH_SHORT).show()
            }
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

        noteAddViewModel.insert(note)

        // Tampilkan Toast bahwa catatan berhasil ditambahkan
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show()

        // Tutup activity
        finish()
    }

    suspend fun uploadStory() {
        try {
            val sentence = binding.edtDescription.text.toString().trim()

            if (sentence.isEmpty()) {
                Toast.makeText(this, "Please enter a sentence!", Toast.LENGTH_SHORT).show()
                return
            }

            val apiService = ApiConfig.getApiService("")
            val uploadRequest = UploadRequest(sentence = sentence)

            val response = apiService.addStory(uploadRequest)

            if (response.status == "success") {
                Log.d("UploadStory", "Story uploaded successfully with ID: ${response.data?.id}")
            } else {
                Log.e("UploadStory", "Failed to upload story: ${response.message}")
            }
        } catch (e: HttpException) {
            Log.e("UploadStory", "HTTP Exception: ${e.code()} - ${e.message()}")
        } catch (e: Exception) {
            Log.e("UploadStory", "Unexpected Error: ${e.message}")
        }
    }



}