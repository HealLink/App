package com.rega.heallink.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rega.heallink.data.NoteRepository
import com.rega.heallink.data.local.Note
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getAllNotes() {
        _isLoading.value = true
        repository.getAllNotes().observeForever { notes ->
            _notes.value = notes
            _isLoading.value = false
        }

        // Handling errors with proper exception handling
        try {
            // If there's any asynchronous operation, make sure to catch errors
        } catch (e: HttpException) {
            _error.postValue("Network error: ${e.message()}")
        } catch (e: IOException) {
            _error.postValue("IO error: ${e.message}")
        } catch (e: Exception) {
            _error.postValue("Unknown error: ${e.message}")
        }
    }
}
