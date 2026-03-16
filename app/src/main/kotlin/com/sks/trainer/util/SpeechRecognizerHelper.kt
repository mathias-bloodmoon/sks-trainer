package com.sks.trainer.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechRecognizerHelper(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onVolumeChanged: (Float) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isContinuous = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val restartRunnable = Runnable {
        if (isContinuous) {
            startListeningInternal()
        }
    }

    private fun createRecognizer() {
        destroyInternal() // Clean up existing, aber sicher
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { Log.d("Speech", "Ready") }
                    override fun onBeginningOfSpeech() { Log.d("Speech", "Started") }
                    override fun onRmsChanged(rmsdB: Float) { onVolumeChanged(rmsdB) }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { Log.d("Speech", "End") }
                    
                    override fun onError(error: Int) {
                        Log.e("Speech", "Error: $error")
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audiofehler"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Nichts erkannt"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                            SpeechRecognizer.ERROR_CLIENT -> "Client Fehler"
                            else -> "Fehler code: $error"
                        }
                        onError(message)
                        checkContinuous()
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        }
                        checkContinuous()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onPartialResult(matches[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun checkContinuous() {
        if (isContinuous) {
            // Eine kleine Verzögerung ist wichtig, damit der vorherige Prozess
            // sauber abgebaut wird, bevor ein neuer gestartet wird.
            mainHandler.postDelayed(restartRunnable, 150)
        }
    }

    private fun startListeningInternal() {
        createRecognizer()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Längere Timeouts für kontinuierliches Sprechen
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 100000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100000L)
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }
        speechRecognizer?.startListening(intent)
    }

    fun startListening(continuous: Boolean = true) {
        this.isContinuous = continuous
        mainHandler.removeCallbacks(restartRunnable)
        startListeningInternal()
    }

    fun stopListening() {
        isContinuous = false
        mainHandler.removeCallbacks(restartRunnable)
        speechRecognizer?.stopListening()
    }

    private fun destroyInternal() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("Speech", "Error destroying speech recognizer", e)
        }
        speechRecognizer = null
    }

    fun destroy() {
        isContinuous = false
        mainHandler.removeCallbacks(restartRunnable)
        destroyInternal()
    }
}
