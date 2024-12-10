package com.heallinkapp.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heallinkapp.data.NoteRepository
import com.heallinkapp.data.local.Note
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

}