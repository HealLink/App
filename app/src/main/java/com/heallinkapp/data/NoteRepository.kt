package com.heallinkapp.data


import android.util.Log
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

    suspend fun getAllNotes(): LiveData<List<Note>> {
        val notes = noteDao.getNotesBlocking()

        if (notes.isNullOrEmpty()) {
            fetchAndStoreNotesFromApi()
        }

        return noteDao.getAllNotes()
    }

    private suspend fun fetchAndStoreNotesFromApi() {
        try {
            // Fetch data dari API
            val apiResponse = apiService.getStories()
            Log.d("NoteRepository", "API Response: $apiResponse")
            val notesToInsert = apiResponse.data?.map { story ->
                Note(
                    title = story?.history?.title ?: "",
                    description = story?.history?.story ?: "",
                    date = story?.history?.date?: "",
                    result = story?.history?.result?.map { it.toFloat() }
                )
            }

            Log.d("NoteRepository", "Notes to insert: $notesToInsert")

            // Simpan data ke Room jika ada data
            notesToInsert?.let { notes ->
                noteDao.insertList(notes)
            }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error fetching data from API", e)
        }
    }



    // Other functions for insert, delete, update, and uploadStory remain the same
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun uploadStory(token: String, title: String, sentence: String, date: String): FileUploadResponse {
        val uploadRequest = UploadRequest(token, title, sentence, date)
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
