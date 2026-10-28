package co.barrantes.ponleplay.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

object PlaybackActions {
    const val PLAY_PAUSE = "co.barrantes.ponleplay.PLAY_PAUSE"
    const val SEEK_BACK = "co.barrantes.ponleplay.SEEK_BACK"
    const val SEEK_FORWARD = "co.barrantes.ponleplay.SEEK_FORWARD"
}

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PlaybackActions.PLAY_PAUSE -> if (player.isPlaying) player.pause() else player.play()
            PlaybackActions.SEEK_BACK -> player.seekBack()
            PlaybackActions.SEEK_FORWARD -> player.seekForward()
            else -> intent?.data?.let { uri ->
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.playWhenReady = true
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        player.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
