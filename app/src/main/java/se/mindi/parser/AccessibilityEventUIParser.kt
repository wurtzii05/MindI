package se.mindi.parser

import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.json.Json
import se.mindi.model.UINodeProperties
import se.mindi.model.UINodeType

class AccessibilityEventUIParser {
    val uiNodes: MutableList<UINodeProperties> = mutableListOf()
    private val nodesToParse: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()

    companion object {
        public fun parse(node: AccessibilityNodeInfo): AccessibilityEventUIParser {

//            val focus = node.parent
//            Log.d("window", """
//                ${focus}
//            """.trimIndent())
            val parser = AccessibilityEventUIParser()
            parser.printNodes(node)

//            try {
//                parser.nodesToParse.addLast(focus)
//                while (!parser.nodesToParse.isEmpty()) {
//                    parser.uiNodes.add(parser.parseNode())
//                }
//                Log.d("parser", "$parser: Count: ${focus.childCount}");
//                return parser
//            } catch (_: Exception) {}
//
//            Log.d("parser", "$parser: Count: ${focus?.childCount}");
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

    private fun printNodes(node: AccessibilityNodeInfo) {
        Log.d("child", "TEXT: ${node.text} ${node.className} $node")
        val childrenCount = node.childCount
        for (i in 0 until childrenCount) {
            val child = node.getChild(i)
            printNodes(child)
        }
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
                continue
            }

            parseChildTextHelper(child, result)
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
                continue
            }

            return parseChildTextHelper(child, result)
        }

        return result
    }
}