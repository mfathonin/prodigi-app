package com.merahputihperkasa.prodigi.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.room.TypeConverter
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

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
        Log.d("IntListConverter", "Value: $value")
        if (value.isEmpty()) {
            return emptyList()
        }
        return try {
            var str = value
            if (value.startsWith("[") && value.endsWith("]")) {
                str = value.substring(1, value.length - 1)
            }
            str.split(",").map { it.trim().toInt() }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid integer format in the stored value", e)
        }
    }

    @TypeConverter
    fun toString(list: List<Int>): String {
        if (list.isEmpty()) {
            return ""
        }
        return "[${list.joinToString(",")}]"
    }
}

suspend fun Bitmap.saveToDisk(context: Context): Uri {
    require(!this.isRecycled) { "Cannot save recycled bitmap" }
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.png"
    )

    if (file.exists()) file.delete()

    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

/**
 * We call [MediaScannerConnection] to index the newly created image inside MediaStore to be visible
 * for other apps, as well as returning its [MediaStore] Uri
 */
suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    try {
        outputStream().use { out ->
            if (!bitmap.compress(format, quality, out)) {
                throw IOException("Failed to compress bitmap")
            }
            out.flush()
        }
    } catch (e: IOException) {
        throw IOException("Failed to write bitmap to file: ${e.message}", e)
    }
}

fun shareBitmap(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        createChooser(intent, "Share your image"),
        null
    )
}