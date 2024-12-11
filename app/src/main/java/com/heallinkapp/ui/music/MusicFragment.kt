package com.heallinkapp.ui.music

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.heallinkapp.databinding.FragmentMusicBinding
import android.media.AudioAttributes
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.coroutines.launch
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.*
import android.widget.*
import kotlinx.coroutines.launch


import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.heallinkapp.R

class MusicFragment : Fragment() {
    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var musicService: MusicService
    private var currentTrack: Track? = null
    private var isPlaying = false




    private val handler = Handler(Looper.getMainLooper())


    private val updateProgress = object : Runnable {
        override fun run() {
            try {
                val currentPosition = musicService.getCurrentPosition()
                binding.seekBar.progress = currentPosition
                binding.tvCurrentTime.text = formatTime(currentPosition)
                if (musicService.isPlaying()) {
                    handler.postDelayed(this, 1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()

            val track = musicService.getCurrentTrack()
            if (track != null) {
                updatePlayerUI(track)
                binding.seekBar.max = musicService.getDuration()
                binding.seekBar.progress = musicService.getCurrentPosition()
                binding.tvDuration.text = formatTime(musicService.getDuration())
                binding.tvCurrentTime.text = formatTime(musicService.getCurrentPosition())
                updatePlayPauseButton(musicService.isPlaying())
                if (musicService.isPlaying()) {
                    handler.post(updateProgress)
                }
            }
            musicService.onPlaybackStateChanged = { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPlayerControls()
        loadMusic()
        startMusicService()

        lifecycleScope.launch {
            if (::musicService.isInitialized) {
                val track = musicService.getCurrentTrack()
                if (track != null) {
                    updatePlayerUI(track)
                    binding.seekBar.max = musicService.getDuration()
                    binding.seekBar.progress = musicService.getCurrentPosition()
                    binding.tvDuration.text = formatTime(musicService.getDuration())
                    binding.tvCurrentTime.text = formatTime(musicService.getCurrentPosition())
                    updatePlayPauseButton(musicService.isPlaying())
                }
            }
        }
        handler.post(updateProgress)
    }

    private fun startMusicService() {
        Intent(requireContext(), MusicService::class.java).also { intent ->
            ContextCompat.startForegroundService(requireContext(), intent)
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }


    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter { track ->
            playMusic(track)
        }
        binding.rvMusic.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = musicAdapter
        }
    }

    private fun setupPlayerControls() {
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService.seekTo(progress)
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateProgress)
            }
        })
    }

    private fun loadMusic() {
        lifecycleScope.launch {
            try {
                binding.progressBar.isVisible = true
                val response = ApiConfig.getJamendoApi()
                    .getRelaxationMusic(ApiConfig.getClientId())
                musicAdapter.submitList(response.results)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading music: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    private fun playMusic(track: Track) {
        currentTrack = track
        musicService.play(track)
        updatePlayerUI(track)
        musicService.setOnPreparedListener { duration ->
            binding.seekBar.max = duration
            binding.tvDuration.text = formatTime(duration)
            handler.post(updateProgress)
        }
        binding.seekBar.progress = 0
        binding.tvCurrentTime.text = formatTime(0)
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            musicService.pause()
        } else {
            musicService.resume()
        }
    }

    private fun updatePlayerUI(track: Track) {
        binding.apply {
            tvCurrentTitle.text = track.name
            tvCurrentArtist.text = track.artist_name
            playerLayout.visibility = View.VISIBLE
        }
    }

    private fun updatePlayPauseButton(playing: Boolean) {
        isPlaying = playing
        binding.btnPlayPause.setImageResource(
            if (playing) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun formatTime(millis: Int): String {
        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgress)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection)
    }
}