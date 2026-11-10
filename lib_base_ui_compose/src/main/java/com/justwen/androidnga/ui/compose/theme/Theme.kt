package com.justwen.androidnga.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alibaba.android.arouter.launcher.ARouter
import com.justwen.androidnga.base.service.api.IThemeManagerService

private val MistLightColors = lightColors(
    primary = Color(0xFF3976B7), onPrimary = Color.White,
    secondary = Color(0xFF3976B7), background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF1B1D20), surface = Color.White, onSurface = Color(0xFF1B1D20),
)
private val MistDarkColors = darkColors(
    primary = Color(0xFF78AADB), onPrimary = Color(0xFF101317),
    secondary = Color(0xFF78AADB), background = Color(0xFF101317),
    onBackground = Color(0xFFE8EEF3), surface = Color(0xFF252B31), onSurface = Color(0xFFE8EEF3),
)

private val DarkColorPalette = darkColors(
    primary = PrimaryNight,
    primaryVariant = PrimaryGreen,
    secondary = PrimaryNight,
    background = Color(0XFF080C10)
)

private val GreenLightColorPalette = lightColors(
    primary = PrimaryGreen,
    primaryVariant = PrimaryGreen,
    secondary = PrimaryGreen,
    background = Color(0xFFFFF8E7)
)

private val BlackLightColorPalette = lightColors(
    primary = PrimaryBlack,
    primaryVariant = PrimaryBlack,
    secondary = PrimaryBlack,
    background = Color(0xFFFFF8E7)
)

private val BrownLightColorPalette = lightColors(
    primary = PrimaryBrown,
    primaryVariant = PrimaryBrown,
    secondary = PrimaryBrown,
    background = Color(0xFFFFF8E7)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeManager = ARouter.getInstance().build(IThemeManagerService.ROUTER_PATH).navigation() as IThemeManagerService
    val colors = if (themeManager.isCouiStyle()) {
        if (themeManager.isNightMode()) MistDarkColors else MistLightColors
    } else if (darkTheme || themeManager.isNightMode()) {
        DarkColorPalette
    } else {
        val theme = themeManager.getThemeIndex()
        when (theme) {
            0 -> BrownLightColorPalette
            1 -> GreenLightColorPalette
            else -> BlackLightColorPalette
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}