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

    suspend fun getAllNotes(token: String): LiveData<List<Note>> {
        val notes = noteDao.getNotesBlocking()

        if (notes.isNullOrEmpty()) {
            fetchAndStoreNotesFromApi(token)
        }

        return noteDao.getAllNotes()
    }

    private suspend fun fetchAndStoreNotesFromApi(token: String) {
        try {
            val apiResponse = apiService.getStories()
            Log.d("NoteRepository", "API Response: $apiResponse")

            // Filter notes based on the matching token
            val notesToInsert = apiResponse.data?.filter { it?.history?.token == token }?.map { story ->
                Note(
                    title = story?.history?.title ?: "",
                    description = story?.history?.story ?: "",
                    date = story?.history?.date ?: "",
                    result = story?.history?.result?.map { it.toFloat() }
                )
            }

            Log.d("NoteRepository", "Filtered Notes to insert: $notesToInsert")

            notesToInsert?.let { notes ->
                noteDao.insertList(notes)
            }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error fetching data from API", e)
        }
    }

    suspend fun clearAllNotes() {
        noteDao.clearAllNotes()
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