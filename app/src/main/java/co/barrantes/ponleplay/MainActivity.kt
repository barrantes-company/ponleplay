package co.barrantes.ponleplay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import co.barrantes.ponleplay.playback.PlaybackService

class MainActivity : ComponentActivity() {
    private var videoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PonleplayApp(
                onMediaSelected = ::startPlayback
            )
        }
    }

    private fun startPlayback(uri: Uri) {
        val type = contentResolver.getType(uri).orEmpty()
        if (type.startsWith("video/")) {
            videoPlayer?.release()
            videoPlayer = ExoPlayer.Builder(this).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
            }
        } else {
            val intent = Intent(this, PlaybackService::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ContextCompat.startForegroundService(this, intent)
        }
    }

    override fun onDestroy() {
        videoPlayer?.release()
        videoPlayer = null
        super.onDestroy()
    }
}

@Composable
private fun PonleplayApp(
    onMediaSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFile = it.lastPathSegment ?: "Contenido seleccionado"
            val type = context.contentResolver.getType(it).orEmpty()
            selectedVideo = if (type.startsWith("video/")) it else null
            onMediaSelected(it)
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080808)),
            color = Color(0xFF080808)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                selectedVideo?.let { uri ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        factory = { viewContext ->
                            PlayerView(viewContext).apply {
                                useController = true
                                player = ExoPlayer.Builder(viewContext).build().apply {
                                    setMediaItem(MediaItem.fromUri(uri))
                                    prepare()
                                    playWhenReady = true
                                }
                            }
                        },
                        update = { playerView ->
                            playerView.player?.let { player ->
                                if (player.mediaItemCount == 0) {
                                    player.setMediaItem(MediaItem.fromUri(uri))
                                    player.prepare()
                                    player.play()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "PONLEPLAY",
                    color = Color(0xFFF2B35D),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tu contenido. A un toque.",
                    color = Color(0xFFF4F0E8),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedFile ?: "Selecciona un archivo local para comenzar.",
                    color = Color(0xFFAAA49B),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        picker.launch(arrayOf("audio/*", "video/*"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF2B35D),
                        contentColor = Color(0xFF080808)
                    )
                ) {
                    Text("Elegir contenido")
                }
            }
        }
    }
}
