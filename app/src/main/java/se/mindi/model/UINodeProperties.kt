package se.mindi.model

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
