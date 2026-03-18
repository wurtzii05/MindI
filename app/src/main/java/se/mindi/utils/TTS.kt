package se.mindi.utils

import android.speech.tts.TextToSpeech

class TTS {
    companion object {
        var tts: TextToSpeech? = null
        fun speakOut(text: String?) {
            if (!text.isNullOrEmpty()) {
                // The third parameter is null (params), and the fourth is a unique ID for this utterance
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ID_1")
            }
        }

        fun speakError(err: String) {
            val text = "an error has occurred. $err"
            speakOut(text)
        }
    }
}