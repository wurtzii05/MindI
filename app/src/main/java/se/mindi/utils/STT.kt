package se.mindi.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class STT(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(onResult: (String?) -> Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)


        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onResult(data?.get(0)) // Pass the best match back
                cleanup()
            }

            override fun onError(error: Int) {
                //If we didn't get a result
                Log.e("STT", "Error code: $error")
                onResult(null)
                cleanup()
            }
            override fun onReadyForSpeech(p0: Bundle?)
            {
                //play a sound?
            }
            //Can be left empty
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }
    public fun stopListening()
    {
        speechRecognizer!!.stopListening()
    }

    private fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}