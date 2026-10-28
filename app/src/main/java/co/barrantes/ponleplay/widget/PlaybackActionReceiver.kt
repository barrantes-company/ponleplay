package co.barrantes.ponleplay.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import co.barrantes.ponleplay.playback.PlaybackService

class PlaybackActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, PlaybackService::class.java).setAction(intent.action)
        )
    }
}
