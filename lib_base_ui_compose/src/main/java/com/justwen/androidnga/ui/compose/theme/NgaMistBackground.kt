package com.justwen.androidnga.ui.compose.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.justwen.androidnga.ui.compose.miuix.NgaUiStyle

/** The remaining Compose screens use the same viewport-fixed mist palette as the COUI pages. */
@Composable
internal fun NgaComposeBackground(content: @Composable BoxScope.() -> Unit) {
    val dark = NgaUiStyle.isNightMode()
    val top = if (dark) Color(0xFF18232E) else Color(0xFFEEF3F8)
    val bottom = if (dark) Color(0xFF101317) else Color(0xFFF5F6F8)
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(0f to top, 0.52f to bottom, 1f to bottom),
        ),
        content = content,
    )
}
