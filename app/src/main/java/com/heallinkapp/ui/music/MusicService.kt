package com.heallinkapp.ui.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.heallinkapp.R
import android.app.*
import android.graphics.Bitmap
import android.graphics.Canvas

import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.navigation.NavDeepLinkBuilder
import com.heallinkapp.MainActivity

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
        set (value) {
            field = value
            value?.invoke(isPlaying())
        }
    private lateinit var mediaSession: MediaSessionCompat
    private var onPreparedCallback: ((Int) -> Unit)? = null

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun getCurrentTrack(): Track? = currentTrack

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }


    override fun onCreate() {
        super.onCreate()
        initMediaSession()
    }

    override fun onBind(intent: Intent): IBinder {
        return MusicBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                if (isPlaying()) pause() else resume()
                updateNotification()
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }


    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    resume()
                }

                override fun onPause() {
                    pause()
                }

                override fun onStop() {
                    stopSelf()
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos.toInt())
                }
            })
            isActive = true
        }
    }

    fun setOnPreparedListener(callback: (Int) -> Unit) {
        onPreparedCallback = callback
    }


    fun play(track: Track) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(track.audio)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    onPlaybackStateChanged?.invoke(true)
                    showNotification(track)
                    onPreparedCallback?.invoke(duration)
                }
                setOnErrorListener { _, _, _ ->
                    onPlaybackStateChanged?.invoke(false)
                    true
                }
            }
            currentTrack = track
        } catch (e: Exception) {
            onPlaybackStateChanged?.invoke(false)
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        onPlaybackStateChanged?.invoke(false)
        updateNotification()
    }

    fun resume() {
        mediaPlayer?.start()
        onPlaybackStateChanged?.invoke(true)
        updateNotification()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    private fun showNotification(track: Track) {
        val channelId = "music_channel"
        createNotificationChannel(channelId)

        val intent = Intent(this, MusicFragment::class.java)

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_music)
            .createPendingIntent()

        val playPauseAction = NotificationCompat.Action(
            if (isPlaying()) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying()) "Pause" else "Play",
            getPendingIntent(ACTION_PAUSE)
        )

        val stopAction = NotificationCompat.Action(
            R.drawable.baseline_stop_24,
            "Stop",
            getPendingIntent(ACTION_STOP)
        )

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1)
            .setMediaSession(mediaSession.sessionToken)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_library_music_24)
            .setLargeIcon(getBitmapFromVectorDrawable(R.drawable.baseline_library_music_24))
            .setContentTitle(track.name)
            .setContentText(track.artist_name)
            .setSubText(if (isPlaying()) "Playing" else "Paused")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(mediaStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        currentTrack?.let { track ->
            showNotification(track)
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music player controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        onPlaybackStateChanged = null
        onPreparedCallback = null
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val ACTION_PAUSE = "pause"
        const val ACTION_STOP = "stop"
    }
}