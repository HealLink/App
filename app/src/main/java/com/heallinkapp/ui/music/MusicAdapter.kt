package com.heallinkapp.ui.music

// MusicAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heallinkapp.R
import com.heallinkapp.databinding.ItemMusicBinding
import java.util.concurrent.TimeUnit

class MusicAdapter(
    private val onItemClick: (Track) -> Unit
) : ListAdapter<Track, MusicAdapter.ViewHolder>(MusicDiffCallback()) {

    inner class ViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            binding.apply {
                tvTitle.text = track.name
                tvArtist.text = track.artist_name
                tvDuration.text = formatDuration(track.duration)

                Glide.with(itemView)
                    .load(track.image)
                    .placeholder(R.drawable.baseline_library_music_24)
                    .into(ivArtwork)

                root.setOnClickListener { onItemClick(track) }
            }
        }

        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MusicDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}