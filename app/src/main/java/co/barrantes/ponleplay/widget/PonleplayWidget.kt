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

private val WidgetInk = ColorProvider(Color(0xFF0D0C0B), Color(0xFF0D0C0B))
private val WidgetPaper = ColorProvider(Color(0xFFF6F2EA), Color(0xFFF6F2EA))
private val WidgetMuted = ColorProvider(Color(0xFFAAA39A), Color(0xFFAAA39A))
private val WidgetAmber = ColorProvider(Color(0xFFF3B45F), Color(0xFFF3B45F))

class PonleplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) { provideContent { WidgetContent() } }
    @Composable private fun WidgetContent() {
        val open = actionStartActivity<co.barrantes.ponleplay.MainActivity>()
        Column(GlanceModifier.fillMaxSize().background(WidgetInk).padding(18.dp).clickable(open),verticalAlignment=Alignment.CenterVertically) {
            Row(GlanceModifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("PONLEPLAY",style=TextStyle(color=WidgetAmber));Spacer(GlanceModifier.width(8.dp));Text("NOW PLAYING",style=TextStyle(color=WidgetMuted))}
            Spacer(GlanceModifier.height(10.dp));Text("Reproductor multimedia",style=TextStyle(color=WidgetPaper));Spacer(GlanceModifier.height(4.dp));Text("Toca para abrir y controlar",style=TextStyle(color=WidgetMuted));Spacer(GlanceModifier.height(16.dp))
            Row(GlanceModifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("◀ 10",style=TextStyle(color=WidgetPaper));Spacer(GlanceModifier.width(20.dp));Text("▶",style=TextStyle(color=WidgetAmber));Spacer(GlanceModifier.width(20.dp));Text("10 ▶",style=TextStyle(color=WidgetPaper));Spacer(GlanceModifier.width(20.dp));Text("PIP",style=TextStyle(color=WidgetAmber))}
        }
    }
}
