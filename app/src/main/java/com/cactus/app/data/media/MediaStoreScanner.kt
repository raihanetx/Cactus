package com.cactus.app.data.media
import android.content.ContentUris; import android.content.Context; import android.net.Uri; import android.provider.MediaStore; import android.util.Log
data class MediaStoreVideo(val mediaStoreId: Long, val displayName: String, val filePath: String, val sizeBytes: Long, val durationMs: Long, val dateAddedMs: Long, val uri: Uri)
object MediaStoreScanner {
    private const val TAG = "MediaStoreScanner"
    fun scanVideos(context: Context): List<MediaStoreVideo> {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED)
        val selection = "${MediaStore.Video.Media.SIZE} > 10240"
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        return try { context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { c ->
            val out = mutableListOf<MediaStoreVideo>()
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID); val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME); val pathCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA); val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE); val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION); val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            while (c.moveToNext()) { val id = c.getLong(idCol); val name = c.getString(nameCol) ?: "Unknown"; val dot = name.lastIndexOf('.'); val displayName = if (dot > 0) name.substring(0, dot) else name; out.add(MediaStoreVideo(id, displayName, c.getString(pathCol) ?: "", c.getLong(sizeCol), c.getLong(durCol).takeIf { it > 0 } ?: 0L, c.getLong(dateCol) * 1000L, ContentUris.withAppendedId(collection, id))) }
            Log.d(TAG, "Scanned ${out.size} videos"); out
        } ?: emptyList() } catch (e: Exception) { Log.w(TAG, "Scan failed", e); emptyList() }
    }
}
