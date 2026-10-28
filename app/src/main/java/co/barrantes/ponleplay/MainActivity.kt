package co.barrantes.ponleplay

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import co.barrantes.ponleplay.playback.PlaybackService

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var currentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val saved = getPreferences(MODE_PRIVATE).getString("last_uri", null)
        setContent {
            PonleplayApp(
                initialUri = saved?.let(Uri::parse),
                onMediaSelected = ::selectMedia,
                onEnterPip = ::enterPip
            )
        }
    }

    private fun selectMedia(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) { }
        getPreferences(MODE_PRIVATE).edit().putString("last_uri", uri.toString()).apply()
        currentUri = uri
    }

    private fun enterPip() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            )
        }
    }

    override fun onUserLeaveHint() {
        if (currentUri != null) enterPip()
        super.onUserLeaveHint()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}

@Composable
private fun PonleplayApp(
    initialUri: Uri?,
    onMediaSelected: (Uri) -> Unit,
    onEnterPip: () -> Unit
) {
    val context = LocalContext.current
    var selectedName by remember { mutableStateOf(initialUri?.lastPathSegment ?: "Elige un archivo local") }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val type = context.contentResolver.getType(uri).orEmpty()
        selectedName = uri.lastPathSegment ?: "Contenido seleccionado"
        isVideo = type.startsWith("video/")
        onMediaSelected(uri)
        if (isVideo) {
            player?.release()
            player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
        } else {
            context.startForegroundService(Intent(context, PlaybackService::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF080808)) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("PONLEPLAY", color = Color(0xFFF2B35D), style = MaterialTheme.typography.labelLarge)
                Text("Tu contenido. A un toque.", color = Color(0xFFF4F0E8), style = MaterialTheme.typography.headlineMedium)
                Text(selectedName, color = Color(0xFFAAA49B), style = MaterialTheme.typography.bodyLarge)

                if (isVideo && player != null) {
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { PlayerView(it).apply { useController = true; this.player = player } },
                            update = { it.player = player }
                        )
                        IconButton(onClick = onEnterPip, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                            Text("PIP", color = Color.White)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton(onClick = { player?.seekBack() }) { Text("−10", color = Color.White) }
                        IconButton(onClick = { if (isPlaying) player?.pause() else player?.play() }) {
                            Text(if (isPlaying) "❚❚" else "▶", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        }
                        IconButton(onClick = { player?.seekForward() }) { Text("+10", color = Color.White) }
                    }
                } else {
                    Text("Selecciona un video para verlo aquí o usa el widget para abrirlo rápidamente.", color = Color(0xFFAAA49B))
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { picker.launch(arrayOf("audio/*", "video/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B35D), contentColor = Color(0xFF080808))
                ) { Text("Elegir contenido") }
            }
        }
    }
}
