package com.heallinkapp.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heallinkapp.data.NoteRepository
import com.heallinkapp.data.local.Note
import com.heallinkapp.data.remote.response.FileUploadResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteAddViewModel(private val repository: NoteRepository) : ViewModel() {

    fun insert(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun uploadStory(sentence: String) {
        viewModelScope.launch {
            try {
                val response = repository.uploadStory(sentence)
                _uploadResult.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    private val _uploadResult = MutableStateFlow<FileUploadResponse?>(null)
    val uploadResult: StateFlow<FileUploadResponse?> = _uploadResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

}
