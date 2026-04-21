package se.mindi.parser

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.json.Json
import se.mindi.model.UINodeProperties
import se.mindi.model.UINodeType

class AccessibilityEventUIParser {
    val uiNodes: MutableList<UINodeProperties> = mutableListOf()
    private val nodesToParse: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()

    companion object {
        public fun parse(node: AccessibilityNodeInfo): AccessibilityEventUIParser {

            val parser = AccessibilityEventUIParser()
            try {
                parser.nodesToParse.addLast(node)
                while (!parser.nodesToParse.isEmpty()) {
                    parser.uiNodes.add(parser.parseNode())
                }
                return parser
            } catch (_: Exception) {}

            return parser
        }
    }

    public fun searchId(id: String): UINodeProperties? {
        runCatching {
            val idNum = id.toInt()
            return (if (idNum > uiNodes.size) {
                null
            } else {
                uiNodes[idNum]
            })
        }

        return null
    }

    public override fun toString(): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(uiNodes)

    }

    private fun parseNode(): UINodeProperties {
        val node = nodesToParse.removeFirst()
        val nodeType = getNodeType(node)
        val nodeText = node.text?.toString() ?: ""
        val childText = parseChildText(node)
        val id = uiNodes.lastIndex + 1
        val np = UINodeProperties(node, nodeType, nodeText, childText, id)
        return np
    }

    private fun parseChildText(node: AccessibilityNodeInfo, result: MutableList<String> = mutableListOf()): List<String> {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val childType = getNodeType(child)
            if (childType == UINodeType.CLICKABLE) {
                nodesToParse.add(child)
            } else {
                parseChildTextHelper(child, result)
            }
        }

        return result
    }

    private fun getNodeType(node: AccessibilityNodeInfo): UINodeType {
        if (node.isClickable) {
            return UINodeType.CLICKABLE
        }

        return UINodeType.TEXTUAL
    }

    private fun parseChildTextHelper(node: AccessibilityNodeInfo, result: MutableList<String>): MutableList<String> {
        // add current text to list, then parse children
        val text = node.text?.toString()
        if (text != null) {
            result.add(text.trim())
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val childType = getNodeType(child)
            // only want nodes with text
            if (childType == UINodeType.CLICKABLE) {
                nodesToParse.addLast(child)
            } else {
                parseChildTextHelper(child, result)
            }
        }

        return result
    }
}