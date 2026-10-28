package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.VoicePersona
import com.example.data.model.VoiceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    data class Playing(val filePath: String, val currentMs: Long, val durationMs: Long) : PlaybackState()
    data class Paused(val filePath: String, val currentMs: Long, val durationMs: Long) : PlaybackState()
    object Completed : PlaybackState()
}

class VoiceEngineManager(private val context: Context) {

    private val TAG = "VoiceEngineManager"
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val scope = CoroutineScope(Dispatchers.Main)

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var currentPlayingFile: String? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    private val _isPreviewPlaying = MutableStateFlow<String?>(null) // voiceId currently previewing
    val isPreviewPlaying: StateFlow<String?> = _isPreviewPlaying.asStateFlow()

    // Amplitudes for the real-time waveform visualizer (24 bars)
    private val _liveAmplitudes = MutableStateFlow(List(24) { 0.15f })
    val liveAmplitudes: StateFlow<List<Float>> = _liveAmplitudes.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                Log.d(TAG, "TTS initialized successfully")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS Utterance onStart: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS Utterance onDone: $utteranceId")
                        if (utteranceId?.startsWith("preview_") == true) {
                            scope.launch { _isPreviewPlaying.value = null }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS Utterance onError: $utteranceId")
                        if (utteranceId?.startsWith("preview_") == true) {
                            scope.launch { _isPreviewPlaying.value = null }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.e(TAG, "TTS Utterance onError code $errorCode: $utteranceId")
                        if (utteranceId?.startsWith("preview_") == true) {
                            scope.launch { _isPreviewPlaying.value = null }
                        }
                    }
                })
            } else {
                Log.e(TAG, "Failed to initialize TTS. Status: $status")
            }
        }
    }

    /**
     * Preview sample speech for a given voice directly through TTS.
     */
    fun previewVoice(voice: VoicePersona) {
        val currentTts = tts ?: return
        if (!isTtsInitialized) return

        // If already previewing this voice, stop it
        if (_isPreviewPlaying.value == voice.id) {
            stopPreview()
            return
        }

        stopPlayback()
        stopPreview()

        _isPreviewPlaying.value = voice.id
        configureVoice(currentTts, voice, VoiceSettings())

        val utteranceId = "preview_${voice.id}_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        currentTts.speak(voice.sampleQuote, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopPreview() {
        tts?.stop()
        _isPreviewPlaying.value = null
    }

    /**
     * Synthesize text into a WAV file in the app cache directory.
     */
    suspend fun synthesizeSpeechToFile(
        text: String,
        voice: VoicePersona,
        settings: VoiceSettings
    ): File? = suspendCancellableCoroutine { continuation ->
        val currentTts = tts
        if (currentTts == null || !isTtsInitialized) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        _isSynthesizing.value = true
        stopPlayback()
        stopPreview()

        val outputDir = File(context.cacheDir, "audio_cache").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "eleven_${voice.id}_${System.currentTimeMillis()}.wav")

        configureVoice(currentTts, voice, settings)

        val utteranceId = "synth_${UUID.randomUUID()}"
        val listener = object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                Log.d(TAG, "Synthesizing started for $id")
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    _isSynthesizing.value = false
                    continuation.resume(outputFile)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    _isSynthesizing.value = false
                    continuation.resume(null)
                }
            }

            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) {
                    _isSynthesizing.value = false
                    continuation.resume(null)
                }
            }
        }

        currentTts.setOnUtteranceProgressListener(listener)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = currentTts.synthesizeToFile(text, params, outputFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            _isSynthesizing.value = false
            continuation.resume(null)
        }
    }

    private fun configureVoice(tts: TextToSpeech, voice: VoicePersona, settings: VoiceSettings) {
        val locale = try {
            val parts = voice.localeTag.split("-")
            if (parts.size >= 2) Locale(parts[0], parts[1]) else Locale(parts[0])
        } catch (e: Exception) {
            Locale.US
        }

        val langResult = tts.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default
            tts.setLanguage(Locale.US)
        }

        // Apply pitch and speed calculations based on persona and settings
        val effectivePitch = settings.computeEffectivePitch(voice.defaultPitch)
        val effectiveSpeed = settings.computeEffectiveSpeed(voice.defaultSpeed)

        tts.setPitch(effectivePitch)
        tts.setSpeechRate(effectiveSpeed)

        // Find best matching voice if available in system
        try {
            val systemVoices = tts.voices
            if (!systemVoices.isNullOrEmpty()) {
                val matched = systemVoices.firstOrNull { sysVoice ->
                    val nameMatch = sysVoice.name.contains(voice.gender, ignoreCase = true)
                    val langMatch = sysVoice.locale.language.equals(locale.language, ignoreCase = true)
                    nameMatch && langMatch
                } ?: systemVoices.firstOrNull { it.locale.language.equals(locale.language, ignoreCase = true) }

                matched?.let { tts.voice = it }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice selection fallback: ${e.message}")
        }
    }

    // --- Audio Playback Management via MediaPlayer ---

    fun playAudioFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Audio file does not exist: $filePath")
            return
        }

        stopPreview()

        // If pausing current file, toggle
        val currentState = _playbackState.value
        if (currentState is PlaybackState.Playing && currentPlayingFile == filePath) {
            pausePlayback()
            return
        } else if (currentState is PlaybackState.Paused && currentPlayingFile == filePath) {
            resumePlayback()
            return
        }

        stopPlayback()
        currentPlayingFile = filePath

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    val dur = mp.duration.toLong()
                    _playbackState.value = PlaybackState.Playing(filePath, 0L, dur)
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stopProgressTracker()
                    _playbackState.value = PlaybackState.Completed
                    _liveAmplitudes.value = List(24) { 0.15f }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    stopPlayback()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start media player", e)
            stopPlayback()
        }
    }

    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                val current = player.currentPosition.toLong()
                val dur = player.duration.toLong()
                currentPlayingFile?.let { path ->
                    _playbackState.value = PlaybackState.Paused(path, current, dur)
                }
                stopProgressTracker()
                _liveAmplitudes.value = List(24) { 0.15f }
            }
        }
    }

    fun resumePlayback() {
        mediaPlayer?.let { player ->
            player.start()
            val current = player.currentPosition.toLong()
            val dur = player.duration.toLong()
            currentPlayingFile?.let { path ->
                _playbackState.value = PlaybackState.Playing(path, current, dur)
            }
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            val dur = player.duration.toLong()
            currentPlayingFile?.let { path ->
                if (player.isPlaying) {
                    _playbackState.value = PlaybackState.Playing(path, positionMs, dur)
                } else {
                    _playbackState.value = PlaybackState.Paused(path, positionMs, dur)
                }
            }
        }
    }

    fun setSpeed(speed: Float) {
        try {
            mediaPlayer?.let { player ->
                player.playbackParams = player.playbackParams.setSpeed(speed)
            }
        } catch (e: Exception) {
            Log.w(TAG, "PlaybackParams setSpeed not supported on this platform: ${e.message}")
        }
    }

    fun stopPlayback() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping media player: ${e.message}")
        }
        mediaPlayer = null
        currentPlayingFile = null
        _playbackState.value = PlaybackState.Idle
        _liveAmplitudes.value = List(24) { 0.15f }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var step = 0
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val current = player.currentPosition.toLong()
                    val dur = player.duration.toLong()
                    currentPlayingFile?.let { path ->
                        _playbackState.value = PlaybackState.Playing(path, current, dur)
                    }

                    // Generate animated rhythmic waveform amplitudes
                    step++
                    val newAmps = List(24) { i ->
                        val wave = Math.sin((step * 0.45) + (i * 0.35)).toFloat()
                        val wave2 = Math.cos((step * 0.25) - (i * 0.2)).toFloat()
                        val amp = (0.25f + 0.38f * Math.abs(wave) + 0.37f * Math.abs(wave2)).coerceIn(0.12f, 1.0f)
                        amp
                    }
                    _liveAmplitudes.value = newAmps
                }
                delay(75)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    /**
     * Share generated audio file using Android Sharesheet.
     */
    fun shareAudio(context: Context, filePath: String, scriptTitle: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "AI Generated Voice - $scriptTitle")
                putExtra(Intent.EXTRA_TEXT, "Generated with ElevenVoice AI Studio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share AI Voice Audio")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing audio file: ${e.message}", e)
        }
    }

    fun release() {
        stopPlayback()
        stopPreview()
        tts?.shutdown()
        tts = null
    }
}
