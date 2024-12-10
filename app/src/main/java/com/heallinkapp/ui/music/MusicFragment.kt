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


import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.heallinkapp.R

class MusicFragment : Fragment() {
    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var isPlaying: Boolean = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgress = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                try {
                    binding.seekBar.progress = player.currentPosition
                    binding.tvCurrentTime.text = formatTime(player.currentPosition)
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter { track ->
            playMusic(track)
        }
        binding.rvMusic.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = musicAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadMusic() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
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
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupPlayerControls()
        loadMusic()
    }

    private fun setupPlayerControls() {
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playMusic(track: Track) {
        currentTrack = track
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            try {
                setDataSource(track.audio)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    this@MusicFragment.isPlaying = true
                    updatePlayerUI(track)
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

                    binding.seekBar.max = duration
                    binding.tvDuration.text = formatTime(duration)
                    handler.post(updateProgress)
                }
                setOnCompletionListener {
                    this@MusicFragment.isPlaying = false
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                    handler.removeCallbacks(updateProgress)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error playing music: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                handler.removeCallbacks(updateProgress)
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            } else {
                player.start()
                handler.post(updateProgress)
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            }
            isPlaying = !isPlaying
        }
    }

    private fun updatePlayerUI(track: Track) {
        binding.apply {
            tvCurrentTitle.text = track.name
            tvCurrentArtist.text = track.artist_name
            playerLayout.visibility = View.VISIBLE
        }
    }

    private fun formatTime(millis: Int): String {
        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateProgress)
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        isPlaying = false
        handler.removeCallbacks(updateProgress)
        binding.btnPlayPause.setImageResource(R.drawable.ic_play)
    }
}