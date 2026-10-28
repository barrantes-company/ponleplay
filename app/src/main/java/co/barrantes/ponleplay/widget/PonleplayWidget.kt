package co.barrantes.ponleplay.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionSendBroadcast
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import co.barrantes.ponleplay.playback.PlaybackActions

private val WidgetInk = ColorProvider(Color(0xFF0D0C0B), Color(0xFF0D0C0B))
private val WidgetPaper = ColorProvider(Color(0xFFF6F2EA), Color(0xFFF6F2EA))
private val WidgetMuted = ColorProvider(Color(0xFFAAA39A), Color(0xFFAAA39A))
private val WidgetAmber = ColorProvider(Color(0xFFF3B45F), Color(0xFFF3B45F))

class PonleplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        val openPlayer = actionStartActivity<co.barrantes.ponleplay.MainActivity>()
        val back = actionSendBroadcast(Intent(PlaybackActions.SEEK_BACK))
        val play = actionSendBroadcast(Intent(PlaybackActions.PLAY_PAUSE))
        val forward = actionSendBroadcast(Intent(PlaybackActions.SEEK_FORWARD))

        Column(
            modifier = GlanceModifier.fillMaxSize().background(WidgetInk).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(GlanceModifier.fillMaxWidth().clickable(openPlayer), verticalAlignment = Alignment.CenterVertically) {
                Text("PONLEPLAY", style = TextStyle(color = WidgetAmber))
                Spacer(GlanceModifier.width(8.dp))
                Text("NOW PLAYING", style = TextStyle(color = WidgetMuted))
            }
            Spacer(GlanceModifier.height(10.dp))
            Text("Reproductor multimedia", style = TextStyle(color = WidgetPaper))
            Spacer(GlanceModifier.height(4.dp))
            Text("Controla tu contenido sin abrir la app", style = TextStyle(color = WidgetMuted))
            Spacer(GlanceModifier.height(16.dp))
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("◀ 10", style = TextStyle(color = WidgetPaper), modifier = GlanceModifier.clickable(back))
                Spacer(GlanceModifier.width(20.dp))
                Text("▶", style = TextStyle(color = WidgetAmber), modifier = GlanceModifier.clickable(play))
                Spacer(GlanceModifier.width(20.dp))
                Text("10 ▶", style = TextStyle(color = WidgetPaper), modifier = GlanceModifier.clickable(forward))
                Spacer(GlanceModifier.width(20.dp))
                Text("PIP", style = TextStyle(color = WidgetAmber), modifier = GlanceModifier.clickable(openPlayer))
            }
        }
    }
}
