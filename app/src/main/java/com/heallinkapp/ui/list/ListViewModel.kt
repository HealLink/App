package com.heallinkapp.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heallinkapp.data.NoteRepository
import com.heallinkapp.data.local.Note
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

    private val _isAlarmSet = MutableLiveData(false)
    val isAlarmSet: LiveData<Boolean> = _isAlarmSet
    

    fun toggleAlarmStatus(isSet: Boolean) {
        _isAlarmSet.value = isSet
    }

    fun getAllNotes(token : String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAllNotes(token).observeForever { notes ->
                    _notes.value = notes
                }
            } catch (e: HttpException) {
                _error.postValue("Network error: ${e.message()}")
                _isLoading.value = false
            } catch (e: IOException) {
                _error.postValue("IO error: ${e.message}")
                _isLoading.value = false
            } catch (e: Exception) {
                _error.postValue("Unknown error: ${e.message}")
                _isLoading.value = false
            }finally {
                _isLoading.value = false
            }
        }
    }
}
