package com.heallinkapp.data


import androidx.lifecycle.LiveData
import com.heallinkapp.data.local.Note
import com.heallinkapp.data.local.NoteDao
import com.heallinkapp.data.remote.response.FileUploadResponse
import com.heallinkapp.data.remote.response.UploadRequest
import com.heallinkapp.data.remote.retrofit.ApiService


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

    suspend fun uploadStory(sentence: String): FileUploadResponse {
        val uploadRequest = UploadRequest(sentence)
        return apiService.addStory(uploadRequest)
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
