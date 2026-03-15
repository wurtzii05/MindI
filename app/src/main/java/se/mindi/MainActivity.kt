package se.mindi

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import se.mindi.ui.theme.MindITheme
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.widget.Toast
import android.util.Log
import android.widget.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.response.ResponseInput
import com.aallam.openai.api.response.ResponseRequest
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
class MainActivity : ComponentActivity() {

    val conversationHistory = mutableListOf<ChatMessage>(
        ChatMessage(
            role = ChatRole.System,
            content = "You are a helpful assistant" +
                    " with the goal of helping a person who may struggle" +
                    "to use a touchscreen phone to use an android phone. " +
                    "You will interpret and complete their instructions and " +
                    "answer their requests. You will be given information regarding" +
                    "the UI hierarchy. " +
                    "Here are relevant commands for you to use (note that the" +
                    "user cannot use them):" +
                    "\n /Say(text) to speak text aloud to the user." +
                    "\n /Select(optionName) to select the listed UI option." +
                    "\n Please reply with only commands, separated by newlines."
        )
    )

    private val voiceRecognitionLauncher = registerForActivityResult<Intent,ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""

            //Do whatever with spokenText
            Log.d("VoiceInput","The user said: $spokenText")

            // Start a coroutine
            //This could probably be made into a method
            lifecycleScope.launch {
                try {
                    //NEED TO ALSO ADD INFO ABOUT UI HIERARCHY
                    conversationHistory.add(ChatMessage(
                        role = ChatRole.System,
                        content = "Save\nLoad\nEdit\nView" //get uihierarchy
                    )
                    )
                    //add the user's spoken text
                    conversationHistory.add(ChatMessage(
                        role = ChatRole.User,
                        content = spokenText
                        )
                    )
                    //generate the request
                    val request = ChatCompletionRequest(
                        model = ModelId("gpt-5-nano"),
                        messages = conversationHistory
                        )
                    //get the response
                    val response = ai.chatCompletion(request)
                    val outputMessage = response.choices.first().message
                    //add the response to the history
                    conversationHistory.add(outputMessage)
                    val outputText = outputMessage.content

                    //just going to have it say the response as a demo
                    Log.d("AIOutput","The ai said: " + outputText)
                    speakOut(outputText)
                } catch (e: Exception) {
                    Log.d("Exception","AI Response Failure")
                    e.message?.let { Log.d("Exception",it) }
                    // Handle errors (network issues, API errors, etc.)
                }

            }



        }
    }
    private var tts: TextToSpeech? = null

    private val ai = OpenAI(
        token = BuildConfig.OPENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //TEXT TO SPEECH
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        val intent: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        setContent {
            MindITheme {
                MindIApp()
            }
        }
        setContent {
            // Your UI starts here
            VoiceScreen(onRecordClick = { startVoiceInput() })
        }
    }
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if you don't have it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }

        //(try to) Start the voice recognition
        try {
            voiceRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice Recognition not available", Toast.LENGTH_SHORT).show()
        }
    }
    private fun speakOut(text: String?) {
        if (!text.isNullOrEmpty()) {
            // The third parameter is null (params), and the fourth is a unique ID for this utterance
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ID_1")
        }
    }
}

@PreviewScreenSizes
@Composable
fun MindIApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MindITheme {
        Greeting("Android")
    }
}
@Composable
fun VoiceScreen(onRecordClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledButtonExample("Click to record", onClick = onRecordClick)
    }
}
@Composable
fun FilledButtonExample(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text)
    }
}
