package com.sks.trainer.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Helper class to simplify Android's SpeechRecognizer usage.
 */
class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audiofehler"
                            SpeechRecognizer.ERROR_CLIENT -> "Clientfehler"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Berechtigung fehlt"
                            SpeechRecognizer.ERROR_NETWORK -> "Netzwerkfehler"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Netzwerk-Timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Nichts erkannt"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Dienst beschäftigt"
                            SpeechRecognizer.ERROR_SERVER -> "Serverfehler"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Keine Spracheingabe"
                            else -> "Unbekannter Fehler"
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    /**
     * Starts listening for speech in German.
     */
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        }
        speechRecognizer?.startListening(intent)
    }

    /**
     * Stops listening.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    /**
     * Releases resources.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
