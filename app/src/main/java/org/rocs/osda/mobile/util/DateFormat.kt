package org.rocs.osda.mobile.util

import java.text.SimpleDateFormat
import java.util.Locale

private val inputDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
private val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val outputDate = SimpleDateFormat("MMM d, yyyy", Locale.US)
private val outputDateTime = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US)

fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    return try {
        if (value.length >= 19 && value[10] == 'T') {
            val date = inputDateTime.parse(value.substring(0, 19)) ?: return value
            if (value.substring(11, 19) == "00:00:00") {
                outputDate.format(date)
            } else {
                outputDateTime.format(date)
            }
        } else {
            val date = inputDate.parse(value.take(10)) ?: return value
            outputDate.format(date)
        }
    } catch (e: Exception) {
        value
    }
}
