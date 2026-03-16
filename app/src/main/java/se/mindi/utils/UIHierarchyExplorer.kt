package se.mindi.utils

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

class UIHierarchyExplorer {
    val hierarchyList: MutableList<UINodeProperties> = mutableListOf()
    private val nodesToParse: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()

    companion object Factory {
        public fun parse(node: AccessibilityNodeInfo): UIHierarchyExplorer {

            val explorer = UIHierarchyExplorer()
            try {
                explorer.nodesToParse.addLast(node)
                while (!explorer.nodesToParse.isEmpty()) {
                    explorer.hierarchyList.add(explorer.parseNode())
                }
                return explorer
            } catch (_: Exception) {}

            return explorer
        }
    }

    public fun searchId(id: String): UINodeProperties? {
        runCatching {
            val idNum = id.toInt()
            return (if (idNum > hierarchyList.size) {
                null
            } else {
                hierarchyList[idNum]
            })
        }

        return null
    }

    public override fun toString(): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(hierarchyList)

    }

    private fun parseNode(): UINodeProperties {
        val node = nodesToParse.removeFirst()
        val nodeType = getNodeType(node)
        val nodeText = node.text?.toString() ?: ""
        val childText = parseChildText(node)
        val id = hierarchyList.lastIndex + 1
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

// these could be their own classes, but not necessary currently
enum class UINodeType {
    CLICKABLE, TEXTUAL
}

@Serializable
data class UINodeProperties(
    @Transient
    val node: AccessibilityNodeInfo? = null,
    val nodeType: UINodeType,
    val nodeText: String,
    val childrenText: List<String>,
    val id: Int
)