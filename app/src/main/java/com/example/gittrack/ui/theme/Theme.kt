package com.example.gittrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.view.WindowCompat

/**
 * GitTrack “Clean Geek” 色彩令牌。
 * 使用低饱和深蓝作为底色，以电光蓝、青绿和紫色作为功能强调，
 * 避免大面积霓虹和渐变，保证 Material 3 的清晰层级。
 */
object GeekPalette {
    val ElectricBlue = Color(0xFF4F7CFF)
    val Cyan = Color(0xFF00A7B5)
    val Emerald = Color(0xFF188A64)
    val Violet = Color(0xFF7C5CFC)
    val Amber = Color(0xFFB76E00)
    val Rose = Color(0xFFC84464)

    val ElectricBlueSoft = Color(0xFFDCE5FF)
    val CyanSoft = Color(0xFFC8F3F6)
    val EmeraldSoft = Color(0xFFC8F1E2)
    val VioletSoft = Color(0xFFE8E0FF)
    val AmberSoft = Color(0xFFFFE5BD)
    val RoseSoft = Color(0xFFFFD9E1)

    val DarkBlue = Color(0xFF9DB7FF)
    val DarkCyan = Color(0xFF65D6DF)
    val DarkEmerald = Color(0xFF69D7AC)
    val DarkViolet = Color(0xFFC7B7FF)
    val DarkAmber = Color(0xFFFFC56B)
    val DarkRose = Color(0xFFFFAFC0)
}

private val LightColors = lightColorScheme(
    primary = GeekPalette.ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = GeekPalette.ElectricBlueSoft,
    onPrimaryContainer = Color(0xFF0A2056),
    secondary = GeekPalette.Emerald,
    onSecondary = Color.White,
    secondaryContainer = GeekPalette.EmeraldSoft,
    onSecondaryContainer = Color(0xFF003829),
    tertiary = GeekPalette.Violet,
    onTertiary = Color.White,
    tertiaryContainer = GeekPalette.VioletSoft,
    onTertiaryContainer = Color(0xFF2B175F),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF171A22),
    surface = Color(0xFFFCFCFF),
    onSurface = Color(0xFF171A22),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFF444955),
    surfaceTint = GeekPalette.ElectricBlue,
    outline = Color(0xFF747986),
    outlineVariant = Color(0xFFC7CBD5),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = GeekPalette.DarkBlue,
    onPrimary = Color(0xFF05245D),
    primaryContainer = Color(0xFF223E78),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = GeekPalette.DarkEmerald,
    onSecondary = Color(0xFF003827),
    secondaryContainer = Color(0xFF07513C),
    onSecondaryContainer = Color(0xFFC8F1E2),
    tertiary = GeekPalette.DarkViolet,
    onTertiary = Color(0xFF35216D),
    tertiaryContainer = Color(0xFF4D3887),
    onTertiaryContainer = Color(0xFFE8E0FF),
    background = Color(0xFF0C1018),
    onBackground = Color(0xFFE5E8F0),
    surface = Color(0xFF111722),
    onSurface = Color(0xFFE5E8F0),
    surfaceVariant = Color(0xFF252C38),
    onSurfaceVariant = Color(0xFFC5C9D3),
    surfaceTint = GeekPalette.DarkBlue,
    outline = Color(0xFF8E94A1),
    outlineVariant = Color(0xFF3D4552),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val GitTrackTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.7).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.35).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

private val GitTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun GitTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 为保证课程展示和 GitTrack 品牌一致性，默认关闭动态壁纸配色。
    // 参数保留，后续需要时可扩展为用户设置。
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = GitTrackTypography,
        shapes = GitTrackShapes,
        content = content
    )
}
