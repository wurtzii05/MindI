package se.mindi.utils

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import se.mindi.BuildConfig
import se.mindi.model.AICommand
import se.mindi.model.AICommandType
import se.mindi.model.UINodeProperties
import se.mindi.model.UINodeType
import se.mindi.parser.AICommandParser
import se.mindi.parser.AccessibilityEventUIParser
import se.mindi.runner.AICommandRunner
import se.mindi.services.AccessibilityService
import se.mindi.utils.TTS
import kotlin.collections.drop
import kotlin.time.Duration.Companion.seconds

class AI {
    val aiPrompt = """
            You are a helpful assistant with the goal of helping a possibly impaired person 
            to use an android phone. You will be prompted with both a brief description of 
            UI elements given in the json form ${AIPromptExamples.AIInputNodesExampleJson} such that
            ${AIPromptExamples.AIUINodePropertiesExplanation}. a user request will also be given.
            please respond like the json example ${AIPromptExamples.AICommandExampleJson} of which
            ${AIPromptExamples.AICommandPossibleTypes}.
            Note that the user cannot use these commands and will not see or hear your whole
            response.
            """.trimIndent()
    val conversationHistory = mutableListOf<ChatMessage>(
        ChatMessage(
            role = ChatRole.System,
            content = aiPrompt
        )
    )
    private val ai = OpenAI(
        token = BuildConfig.OPENAI_API_KEY,
        timeout = Timeout(socket = 60.seconds),
    )


    public suspend fun getAIResponse(spokenText : String?, uiText : String?) : String?
    {
        Log.d("VoiceInput","The user said: $spokenText")
        try {
            //Give UI information (from role system)
            conversationHistory.add(
                ChatMessage(
                    role = ChatRole.System,
                    content = uiText
                )
            )
            //add the user's spoken text
            conversationHistory.add(
                ChatMessage(
                    role = ChatRole.User,
                    content = spokenText
                )
            )
            //generate the request
            val request = ChatCompletionRequest(
                model = ModelId("gpt-5"),
                messages = conversationHistory
            )
            //get the response
            val response = ai.chatCompletion(request)
            val outputMessage = response.choices.first().message
            //add the response to the history
            conversationHistory.add(outputMessage)

            val outputText = outputMessage.content
            Log.d("AIOutput","The ai said: " + outputText)
            return outputText

        } catch (e: Exception) {
            Log.d("Exception","AI Response Failure")
            e.message?.let { Log.d("Exception",it) }
        }
        //Should only happen if something goes wrong.
        return null
    }
    public suspend fun getAIResponseSequential(uiText : String?) : String?
    {
        try {
            //Give UI information (from role system)
            conversationHistory.add(
                ChatMessage(
                    role = ChatRole.System,
                    content = uiText
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
            Log.d("AIOutput","The ai said: " + outputText)
            return outputText

        } catch (e: Exception) {
            Log.d("Exception","AI Response Failure")
            e.message?.let { Log.d("Exception",it) }
        }
        //Should only happen if something goes wrong.
        return null
    }
    public fun handleAiCommand(aiInput : String, explorer: AccessibilityEventUIParser): Boolean
    {
        var commands = AICommandParser.parse(aiInput)
        var answer = true
        if (commands?.last()?.commandType == AICommandType.COMMAND_INCOMPLETE) {
            answer = false
            commands = commands?.dropLast(1);
            Log.d("AI Self-Message", commands?.last()?.customText!!)
            conversationHistory.add(
                ChatMessage(
                    role = ChatRole.Assistant,
                    content = commands?.last()?.customText
                )
            )
            //
        }

        if (commands != null) {
            try {
                AICommandRunner.run(commands, explorer)
            } catch (_ : Exception) {
                return true // moron ai probably did a command_complete in middle of commands
            }
        } else {
            Log.e("Error", "there were no commands to run")
            // TTS.speakError("")
        }

        return answer
    }
}