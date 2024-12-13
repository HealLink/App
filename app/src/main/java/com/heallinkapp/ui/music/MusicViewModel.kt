package com.heallinkapp.ui.music

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentTrack = MutableLiveData<Track?>()
    val currentTrack: LiveData<Track?> = _currentTrack

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> = _duration

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    fun loadMusicIfNeeded() {
        // Hanya load jika tracks masih null atau kosong
        if (_tracks.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null

                    val response = ApiConfig.getJamendoApi()
                        .getRelaxationMusic(ApiConfig.getClientId())

                    _tracks.value = response.results
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }


    fun updatePlaybackState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updateCurrentTrack(track: Track?) {
        _currentTrack.value = track
    }

    fun updateProgress(position: Int, duration: Int) {
        _currentPosition.value = position
        _duration.value = duration
    }

    fun clearError() {
        _error.value = null
    }
}
