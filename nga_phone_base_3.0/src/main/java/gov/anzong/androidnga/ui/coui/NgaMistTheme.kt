package gov.anzong.androidnga.ui.coui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.itos.coui.theme.CouiPresets
import com.itos.coui.theme.CouiTheme

/** Shared NGA palette; retain COUI typography and material behavior. */
@Composable
internal fun NgaMistTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        CouiPresets.Dark.copy(
            accent = Color(0xFF78AADB),
            accentOnSurface = Color(0xFFACCFF0),
            background = Color(0xFF101317),
        )
    } else {
        CouiPresets.Light.copy(
            accent = Color(0xFF3976B7),
            accentOnSurface = Color(0xFF285E98),
            background = Color(0xFFF5F6F8),
            onSurface = Color(0xFF1B1D20),
            onSurfaceVariant = Color(0xFF747B84),
            card = Color.White,
        )
    }
    CouiTheme(darkTheme = dark, colorScheme = colors, content = content)
}

/** A viewport-fixed gradient; offset lets a separate backdrop match screen coordinates. */
internal fun ngaMistBrush(dark: Boolean, subtle: Boolean, heightPx: Float, topOffsetPx: Float = 0f): Brush {
    val top = if (dark) Color(0xFF18232E) else if (subtle) Color(0xFFEEF3F8) else Color(0xFFE7F0F8)
    val bottom = if (dark) Color(0xFF101317) else Color(0xFFF5F6F8)
    return Brush.verticalGradient(
        0f to top, 0.52f to bottom, 1f to bottom,
        startY = -topOffsetPx, endY = heightPx.coerceAtLeast(1f) - topOffsetPx,
    )
}

internal fun ngaSelectedChipColor(dark: Boolean): Color =
    if (dark) Color(0xFF2B4053) else Color(0xFFDCEAF8)
