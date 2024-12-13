package com.heallinkapp.ui.add

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.local.Note
import com.heallinkapp.databinding.ActivityAddBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.helper.DateHelper
import com.heallinkapp.ui.ResultActivity
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

        lifecycleScope.launchWhenStarted {
            noteAddViewModel.uploadResult.collect { response ->
                response?.let {
                    if (it.status == "success") {
                        val resultArray = it.data?.result?.map { value -> value.toFloat() }?.toFloatArray()
                        resultArray?.let {
                            // After upload, insert data to Room
                            val note = Note(
                                title = binding.edtTitle.text.toString().trim(),
                                description = binding.edtDescription.text.toString().trim(),
                                date = DateHelper.getCurrentDate(),
                                result = it.toList()
                            )
                            noteAddViewModel.insert(note)  // Save to Room

                            val intent = Intent(this@AddActivity, ResultActivity::class.java).apply {
                                putExtra("RESULT_ARRAY", it)
                            }
                            startActivity(intent)
                            finish()
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

        // Observe loading state
        lifecycleScope.launchWhenStarted {
            noteAddViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnSubmit.isEnabled = !isLoading
            }
        }

        binding.btnSubmit.setOnClickListener {
            // Fetch token inside coroutine
            lifecycleScope.launch {
                val token = userRepository.userToken.firstOrNull() ?: ""
                val title = binding.edtTitle.text.toString().trim()
                val sentence = binding.edtDescription.text.toString().trim()
                val date = DateHelper.getCurrentDate()

                if (title.isNotEmpty() && isValidWordCount(sentence)) {
                    noteAddViewModel.uploadStory(token, title, sentence, date)
                } else {
                    Toast.makeText(this@AddActivity, "Please fill a valid input!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidWordCount(sentence: String): Boolean {
        val wordCount = countWords(sentence)
        return wordCount in 10..200
    }

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }
}