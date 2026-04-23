package se.mindi.extensions

import android.view.accessibility.AccessibilityNodeInfo
import se.mindi.model.UINode

fun AccessibilityNodeInfo.toUINode(): UINode {
    return toUINodeHelper(this)
}

fun toUINodeHelper(node: AccessibilityNodeInfo): UINode {
    val childCount = node.childCount
    var children = mutableListOf<UINode>()
    for (i in 0 until childCount) {
        val child = node.getChild(i)
        val uiNodeChild = toUINodeHelper(child)
        children.add(i, uiNodeChild)
    }

    val text = node.text?.toString()
    val isClickable = node.isClickable
    return UINode(
        isClickable = isClickable,
        text = text,
        children = children,
        accessibilityNode = node
    )
}
