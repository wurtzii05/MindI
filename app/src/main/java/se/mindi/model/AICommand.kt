package se.mindi.model

import kotlinx.serialization.Serializable

@Serializable
data class AICommands (
    val commands: List<AICommand>
)

@Serializable
data class AICommand (
    val commandType: AICommandType,
    val ids: List<Int>,
    val customText: String
)
enum class AICommandType {
    SAY, SELECT, COMMAND_COMPLETE, COMMAND_INCOMPLETE
}