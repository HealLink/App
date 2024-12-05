package com.rega.heallink.data


import androidx.lifecycle.LiveData
import com.rega.heallink.data.local.Note
import com.rega.heallink.data.local.NoteDao
import com.rega.heallink.data.remote.retrofit.ApiService

class NoteRepository(
    private val apiService: ApiService,
    private val noteDao: NoteDao
) {
    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    companion object {
        @Volatile
        private var instance: NoteRepository? = null

        fun getInstance(apiService: ApiService, noteDao: NoteDao): NoteRepository {
            return instance ?: synchronized(this) {
                instance ?: NoteRepository(apiService, noteDao).also { instance = it }
            }
        }
    }
}
