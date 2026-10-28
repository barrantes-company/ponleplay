package co.barrantes.ponleplay.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.unit.dp
import androidx.glance.appwidget.provideContent
import co.barrantes.ponleplay.MainActivity

class PonleplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.color.ColorProvider(android.graphics.Color.rgb(8, 8, 8)))
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "PONLEPLAY",
                style = androidx.glance.text.TextStyle(
                    color = androidx.glance.color.ColorProvider(android.graphics.Color.rgb(242, 179, 93))
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(text = "Sin contenido activo")
            Spacer(modifier = GlanceModifier.height(12.dp))
            Text(text = "Abrir reproductor")
        }
    }
}
