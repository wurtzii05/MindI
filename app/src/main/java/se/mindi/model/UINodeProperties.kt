package se.mindi.model

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// intermediary class for uiNode to make testing parser possible
// classes that wish to use the parser must now create a transformer
data class UINode(
    val isClickable: Boolean,
    val text: String?,
    val children: List<UINode>,
    val accessibilityNode: AccessibilityNodeInfo? // should simply be null for alternative node types
)

@Serializable
data class UINodeProperties(
    @Transient
    val node: AccessibilityNodeInfo? = null,
    val nodeType: UINodeType,
    val nodeText: String,
    val childrenText: List<String>,
    val id: Int
)

enum class UINodeType {
    CLICKABLE, TEXTUAL
}
