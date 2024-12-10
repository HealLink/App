package com.heallinkapp.di

import android.content.Context
import com.heallinkapp.data.NoteRepository
import com.heallinkapp.data.UserRepository
import com.heallinkapp.data.local.NoteRoomDatabase
import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideNoteRepository(context: Context): NoteRepository {
        val apiService = ApiConfig.getApiService("")
        val database = NoteRoomDatabase.getDatabase(context)
        val dao = database.noteDao()
        return NoteRepository.getInstance(apiService,dao)
    }

    fun provideUserRepository(context: Context): UserRepository {
        val apiService = ApiConfig.getApiService("")
        val userPreferences = UserPreferences.newInstance(context)
        return UserRepository.getInstance(apiService, userPreferences)
    }
}
