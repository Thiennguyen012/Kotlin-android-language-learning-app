package com.vu.englishlearningapp.ui.screens.speaking

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import kotlin.math.sqrt

/** Uses the platform recorder that previously captured microphone input on this app/device. */
class AudioRecorder(context: Context) {
    private val appContext = context.applicationContext
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun amplitude(): Float {
        val active = recorder ?: return 0f
        return try {
            val peak = active.maxAmplitude.toFloat()
            if (peak <= 0f) 0f else {
                // MediaRecorder maxAmplitude ranges from 0 to 32767; expand speech-level input.
                sqrt((peak / 32_767f).coerceIn(0f, 1f))
            }
        } catch (_: IllegalStateException) {
            0f
        }
    }

    fun start() {
        val file = File(appContext.cacheDir, "pronunciation_${System.currentTimeMillis()}.m4a")
        val active = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            active.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = active
            audioFile = file
        } catch (exception: Exception) {
            active.release()
            file.delete()
            throw exception
        }
    }

    fun stop(): File? {
        val active = recorder ?: return null
        recorder = null
        return try {
            active.stop()
            audioFile?.takeIf { it.exists() && it.length() > 1_000L }
        } catch (_: RuntimeException) {
            null
        } finally {
            active.release()
            audioFile = null
        }
    }

    fun cancel() {
        recorder?.let { active ->
            runCatching { active.stop() }
            active.release()
        }
        recorder = null
        audioFile?.delete()
        audioFile = null
    }
}
