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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import co.barrantes.ponleplay.playback.PlaybackService

private val Ink = Color(0xFF090909)
private val Paper = Color(0xFFF6F2EA)
private val Muted = Color(0xFFAAA39A)
private val Amber = Color(0xFFF3B45F)
private val Panel = Color(0xFF171513)
private val Line = Color(0xFF302A24)

class MainActivity : ComponentActivity() {
    private var currentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val saved = getPreferences(MODE_PRIVATE).getString("last_uri", null)?.let(Uri::parse)
        currentUri = saved
        setContent {
            PonleplayApp(
                initialUri = saved,
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
}

@Composable
private fun PonleplayApp(
    initialUri: Uri?,
    onMediaSelected: (Uri) -> Unit,
    onEnterPip: () -> Unit
) {
    val context = LocalContext.current
    var selectedName by remember { mutableStateOf(initialUri?.lastPathSegment ?: "") }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    fun loadUri(uri: Uri) {
        val type = context.contentResolver.getType(uri).orEmpty()
        selectedName = uri.lastPathSegment ?: "Contenido seleccionado"
        isVideo = type.startsWith("video/")
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

    LaunchedEffect(initialUri) {
        initialUri?.let { loadUri(it) }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            onMediaSelected(it)
            loadUri(it)
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener); player?.release() }
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("PONLEPLAY", color = Amber, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.weight(1f))
                    Text("MEDIA PLAYER", color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp)
                }

                Text(
                    "Tu contenido.\nA un toque.",
                    color = Paper,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 42.sp
                )

                if (isVideo && player != null) {
                    Text(selectedName, color = Muted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { PlayerView(it).apply {
                                    useController = true
                                    controllerShowTimeoutMs = 3000
                                    controllerAutoShow = true
                                    keepScreenOn = true
                                    player = player
                                }},
                                update = { it.player = player }
                            )
                            Button(
                                onClick = onEnterPip,
                                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC090909), contentColor = Paper),
                                shape = RoundedCornerShape(50)
                            ) { Text("PIP", fontSize = 12.sp, letterSpacing = 1.sp) }
                        }
                    }
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Panel)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { player?.seekBack() }) { Text("−10", color = Paper, fontWeight = FontWeight.Bold) }
                            IconButton(onClick = { if (isPlaying) player?.pause() else player?.play() }, modifier = Modifier.size(58.dp)) {
                                Text(if (isPlaying) "❚❚" else "▶", color = Amber, fontSize = 27.sp)
                            }
                            IconButton(onClick = { player?.seekForward() }) { Text("+10", color = Paper, fontWeight = FontWeight.Bold) }
                        }
                    }
                } else {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Panel)
                    ) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("LISTO PARA REPRODUCIR", color = Amber, fontSize = 12.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                            Text("Tu espacio multimedia", color = Paper, style = MaterialTheme.typography.titleLarge)
                            Text("Selecciona un video o audio local. El contenido aparecerá aquí con sus controles.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { picker.launch(arrayOf("audio/*", "video/*")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink)
                ) {
                    Text(if (selectedName.isEmpty()) "Elegir contenido" else "Cambiar contenido", fontWeight = FontWeight.Bold)
                }
                Text("PONLEPLAY / BARRANTES CO", color = Line, fontSize = 11.sp, letterSpacing = 1.1.sp)
            }
        }
    }
}
