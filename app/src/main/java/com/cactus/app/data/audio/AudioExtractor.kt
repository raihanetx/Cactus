package com.cactus.app.data.audio
import android.content.Context; import android.media.MediaCodec; import android.media.MediaExtractor; import android.media.MediaFormat; import android.media.MediaMuxer; import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext; import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.withContext
import java.io.File; import java.nio.ByteBuffer; import java.security.MessageDigest
import javax.inject.Inject; import javax.inject.Singleton
@Singleton
class AudioExtractor @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun extract(sourcePath: String): String = withContext(Dispatchers.IO) {
        val outputFile = File(context.filesDir, "audio_cache/${sha256(sourcePath)}.m4a")
        if (outputFile.exists() && outputFile.length() > 0) return@withContext outputFile.absolutePath
        val extractor = MediaExtractor(); var muxer: MediaMuxer? = null
        try { extractor.setDataSource(sourcePath); var audioTrackIndex = -1; for (i in 0 until extractor.trackCount) { val format = extractor.getTrackFormat(i); val mime = format.getString(MediaFormat.KEY_MIME) ?: continue; if (mime.startsWith("audio/")) { audioTrackIndex = i; extractor.selectTrack(i); break } }
            if (audioTrackIndex < 0) throw IllegalStateException("No audio track")
            outputFile.parentFile?.mkdirs(); muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4); val muxerTrack = muxer.addTrack(extractor.getTrackFormat(audioTrackIndex)); muxer.start()
            val buffer = ByteBuffer.allocateDirect(256 * 1024); val bufferInfo = MediaCodec.BufferInfo()
            while (true) { val sampleSize = extractor.readSampleData(buffer, 0); if (sampleSize < 0) break; bufferInfo.offset = 0; bufferInfo.size = sampleSize; bufferInfo.flags = extractor.sampleFlags; bufferInfo.presentationTimeUs = extractor.sampleTime; muxer.writeSampleData(muxerTrack, buffer, bufferInfo); extractor.advance() }
            muxer.stop(); outputFile.absolutePath
        } catch (e: Exception) { Log.e("AudioExtractor", "Failed", e); outputFile.delete(); throw e }
        finally { try { extractor.release() } catch (_: Exception) {}; try { muxer?.release() } catch (_: Exception) {} }
    }
    private fun sha256(input: String): String = MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
}
