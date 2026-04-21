package se.mindi.runner

import android.view.accessibility.AccessibilityNodeInfo
import se.mindi.model.AICommand
import se.mindi.model.AICommandType
import se.mindi.utils.TTS
import se.mindi.parser.AccessibilityEventUIParser

class AICommandRunner {
    companion object Runner {
        public fun run(commands: List<AICommand>, uiNodeParser: AccessibilityEventUIParser) {
            for (command in commands) {
                runCommand(command, uiNodeParser)
            }
        }

        private fun runCommand(command: AICommand, uiNodeParser: AccessibilityEventUIParser) {
            when (command.commandType) {
                AICommandType.SAY -> runSayCommand(command, uiNodeParser)
                AICommandType.SELECT -> runSelectCommand(command, uiNodeParser)
            }
        }


        private fun runSelectCommand(command: AICommand, uiNodeParser: AccessibilityEventUIParser) {
            for (id in command.ids) {
                val node = uiNodeParser.searchId("$id")
                node?.node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }

        private fun runSayCommand(command: AICommand, uiNodeParser: AccessibilityEventUIParser) {
            val text = command.ids.joinToString (
                separator = ". "
            ){
                val node = uiNodeParser.searchId(it.toString())
                "${node?.nodeText} ${node?.childrenText}"
            } + " ${command.customText}"

            TTS.speakOut(text)
        }
    }
}