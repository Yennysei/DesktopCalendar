package dev.yenny.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import desktopcalendar.sources.core.ui.generated.resources.Res
import desktopcalendar.sources.core.ui.generated.resources.Roboto_Bold
import desktopcalendar.sources.core.ui.generated.resources.Roboto_Medium
import desktopcalendar.sources.core.ui.generated.resources.Roboto_Regular
import org.jetbrains.compose.resources.Font
import androidx.compose.material3.Typography

@get:Composable
private val RobotoFontFamily: FontFamily
    get() = FontFamily(
        Font(
            Res.font.Roboto_Regular,
            weight = FontWeight.Normal,
        ),
        Font(
            resource = Res.font.Roboto_Medium,
            weight = FontWeight.Medium,
        ),
        Font(
            resource = Res.font.Roboto_Bold,
            weight = FontWeight.Medium,
        ),
    )

@get:Composable
internal val RobotoTypography: Typography
    get() = Typography().run {
        val fontFamily = RobotoFontFamily

        copy(
            displayLarge = displayLarge.copy(fontFamily = fontFamily),
            displayMedium = displayMedium.copy(fontFamily = fontFamily),
            displaySmall = displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = titleLarge.copy(fontFamily = fontFamily),
            titleMedium = titleMedium.copy(fontFamily = fontFamily),
            titleSmall = titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = bodySmall.copy(fontFamily = fontFamily),
            labelLarge = labelLarge.copy(fontFamily = fontFamily),
            labelMedium = labelMedium.copy(fontFamily = fontFamily),
            labelSmall = labelSmall.copy(fontFamily = fontFamily),
        )
    }
