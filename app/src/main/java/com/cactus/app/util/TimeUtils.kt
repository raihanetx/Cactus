package com.cactus.app.util
object TimeUtils {
    fun formatDuration(ms: Long): String { val sec = (ms / 1000).toInt().coerceAtLeast(0); return "%02d:%02d".format(sec / 60, sec % 60) }
    fun formatDurationShort(ms: Long): String { val sec = (ms / 1000).toInt().coerceAtLeast(0); return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}" }
}
object FileSize { fun format(bytes: Long): String { val mb = bytes / (1024.0 * 1024.0); return if (mb >= 1.0) "${mb.toInt()} MB" else "${bytes / 1024} KB" } }
