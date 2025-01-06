package com.merahputihperkasa.prodigi.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.TypeConverter

fun isValidURi(uri: String): Boolean {
    val pattern = Regex("^https?://[^\\s$.?#].\\S*$")

    return pattern.matches(uri)
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(text, text)
    clipboard.setPrimaryClip(clip)
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

class IntListConverter {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        if (value.isEmpty()) {
            return emptyList()
        }
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun toString(list: List<Int>): String {
        if (list.isEmpty()) {
            return ""
        }
        return list.joinToString(",")
    }
}
