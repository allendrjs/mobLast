package org.rocs.osda.mobile.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class PickedFile(val bytes: ByteArray, val fileName: String, val contentType: String)

/** Reads a picked content:// Uri's bytes, display name and MIME type. */
fun resolvePickedFile(context: Context, uri: Uri): PickedFile {
    val resolver = context.contentResolver

    var displayName = "appeal_letter"
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)?.let { displayName = it }
        }
    }

    val contentType = resolver.getType(uri) ?: "application/octet-stream"

    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException("Couldn't read the selected file.")

    return PickedFile(bytes, displayName, contentType)
}
