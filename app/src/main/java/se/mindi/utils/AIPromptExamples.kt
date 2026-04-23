package se.mindi.utils

import kotlinx.serialization.json.Json
import se.mindi.model.AICommand
import se.mindi.model.AICommandType
import se.mindi.model.UINodeProperties
import se.mindi.model.UINodeType

class AIPromptExamples {
    companion object {
        private val json = Json { prettyPrint = true }

        val AICommandExampleJson: String = json.encodeToString(listOf(
            AICommand(AICommandType.SELECT, listOf(0), ""),
            AICommand(AICommandType.SAY, listOf(), "installing MindI app")
        ))

        val AIInputNodesExampleJson: String = json.encodeToString(listOf(
            UINodeProperties(
                null,
                UINodeType.CLICKABLE,
                "Install",
                listOf("MindI App"),
                0),
            UINodeProperties(
                null,
                UINodeType.TEXTUAL,
                "",
                listOf("5 star review", "the only app you'll ever need"),
                1),
        ))


        val AIUINodePropertiesExplanation = """
            |${UINodeType.CLICKABLE} corresponds to a ui element that may be clicked
            |${UINodeType.TEXTUAL} corresponds to a ui element that cannot be clicked
            |id is a unique digit to identify a ui element
        """.trimMargin()

        val AICommandPossibleTypes: String = """
            |command: has possible values ${AICommandType.entries}
            |of which ${AICommandType.SAY} specifies text that will be spoken to the user
            |${AICommandType.SELECT} specifies a uielement that should be clicked
            |id: a digit that corresponds to the id field in the given ui
            |custom text: is any text that you feel should be said to the user, this must be accompanied by a ${AICommandType.SAY} command
            |if you believe you will need to see the updated ui after the current command to complete the task, then have your LAST command
            |be ${AICommandType.COMMAND_INCOMPLETE}, with the customText being what you completed during this step. If you would set out to complete
            |the same thing that you have already done, then do not repeat it!
            |IMPORTANT: if you do not see any way of completing the given task, with the ui and commands provided then do not use ${AICommandType.COMMAND_INCOMPLETE}
        """.trimMargin()
    }
}