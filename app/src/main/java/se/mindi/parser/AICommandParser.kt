package se.mindi.parser

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import se.mindi.model.AICommand
import se.mindi.model.AICommands

class AICommandParser {
    companion object Parser {
        public fun parse(input: String): List<AICommand>?  =
            try {
                 Json.decodeFromString<AICommands>(input).commands
            } catch (e: SerializationException)
            {
                Json.decodeFromString(input)
            } catch (e: IllegalArgumentException)
            {
                Log.e("AI_Parsing", "could not parse input $input, error $e")
                null
            }
    }
}