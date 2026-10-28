package co.barrantes.ponleplay.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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

class PonleplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        val bg = ColorProvider(Color(0xFF0B0B0B), Color(0xFF0B0B0B))
        val accent = ColorProvider(Color(0xFFF2B35D), Color(0xFFF2B35D))
        val primary = ColorProvider(Color(0xFFF4F0E8), Color(0xFFF4F0E8))
        val secondary = ColorProvider(Color(0xFFAAA49B), Color(0xFFAAA49B))
        val launch = actionStartActivity<co.barrantes.ponleplay.MainActivity>()

        Column(
            modifier = GlanceModifier.fillMaxSize().background(bg).padding(16.dp).clickable(launch),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text("PONLEPLAY", style = TextStyle(color = accent))
            Spacer(GlanceModifier.height(6.dp))
            Text("Reproductor multimedia", style = TextStyle(color = primary))
            Spacer(GlanceModifier.height(4.dp))
            Text("Toca para abrir y reproducir", style = TextStyle(color = secondary))
            Spacer(GlanceModifier.height(12.dp))
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("◀◀", style = TextStyle(color = primary))
                Spacer(GlanceModifier.width(18.dp))
                Text("▶", style = TextStyle(color = accent))
                Spacer(GlanceModifier.width(18.dp))
                Text("▶▶", style = TextStyle(color = primary))
                Spacer(GlanceModifier.width(18.dp))
                Text("PIP", style = TextStyle(color = accent))
            }
        }
    }
}
