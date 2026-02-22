package com.merahputihperkasa.prodigi.ui.theme

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import com.merahputihperkasa.prodigi.BuildConfig

private fun fromHex(value: String, fallback: String): Color = runCatching {
    Color(parseColor(value))
}.getOrElse {
    Color(parseColor(fallback))
}


val Primary800 = fromHex(BuildConfig.BRAND_PRIMARY_DARK, "#454F9B")
val Primary400 = fromHex(BuildConfig.BRAND_PRIMARY_LIGHT, "#818DE4")
val Secondary800 = fromHex(BuildConfig.BRAND_SECONDARY_DARK, "#5E6676")
val Secondary400 = fromHex(BuildConfig.BRAND_SECONDARY_LIGHT, "#9EA8BA")
val Tertiary800 = fromHex(BuildConfig.BRAND_TERTIARY_DARK, "#428DA4")
val Tertiary400 = fromHex(BuildConfig.BRAND_TERTIARY_LIGHT, "#7ED5EF")
val Surface800 = fromHex(BuildConfig.BRAND_SURFACE_DARK, "#141D35")
val Surface400 = fromHex(BuildConfig.BRAND_SURFACE_LIGHT, "#FAFAFA")
val OnPrimary = Color.White
