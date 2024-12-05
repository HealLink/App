package com.rega.heallink.di

import android.content.Context
import com.rega.heallink.data.NoteRepository
import com.rega.heallink.data.UserRepository
import com.rega.heallink.data.local.NoteRoomDatabase
import com.rega.heallink.data.local.UserPreferences
import com.rega.heallink.data.remote.retrofit.ApiConfig

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
