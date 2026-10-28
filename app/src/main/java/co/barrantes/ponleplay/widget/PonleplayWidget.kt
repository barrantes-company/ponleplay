package co.barrantes.ponleplay.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class PonleplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        val background = ColorProvider(
            day = Color(0xFF0B0B0B),
            night = Color(0xFF0B0B0B)
        )
        val accent = ColorProvider(
            day = Color(0xFFF2B35D),
            night = Color(0xFFF2B35D)
        )
        val primary = ColorProvider(
            day = Color(0xFFF4F0E8),
            night = Color(0xFFF4F0E8)
        )
        val secondary = ColorProvider(
            day = Color(0xFFAAA49B),
            night = Color(0xFFAAA49B)
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "PONLEPLAY",
                style = TextStyle(color = accent)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Sin contenido activo",
                style = TextStyle(color = primary)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Toca para abrir el reproductor",
                style = TextStyle(color = secondary)
            )
        }
    }
}
