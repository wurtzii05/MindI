package se.mindi.helper

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import se.mindi.model.UINode

fun SemanticsNodeInteraction.toUINode(): UINode {
    return toUINodeHelper(this.fetchSemanticsNode("could not fetch semantics node"))
}

fun toUINodeHelper(node: SemanticsNode): UINode {
    val children = mutableListOf<UINode>()
    for (child in node.children) {
        val uiNodeChild = toUINodeHelper(child)
        children.add(uiNodeChild)
    }
    val isClickable = node.config.contains(SemanticsActions.OnClick)
    val text = if (node.config.contains(SemanticsProperties.Text)) {
        node.config[SemanticsProperties.Text].toString()
    }
    else {
        null
    }

    return UINode(
        isClickable = isClickable,
        text = text,
        children = children,
        accessibilityNode = null
    )
}