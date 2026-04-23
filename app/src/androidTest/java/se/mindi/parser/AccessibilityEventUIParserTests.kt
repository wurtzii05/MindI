package se.mindi

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import se.mindi.helper.toUINode
import se.mindi.model.UINodeType
import se.mindi.parser.AccessibilityEventUIParser
import java.util.concurrent.TimeoutException
import se.mindi.views.Views

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityEventUIParserTests {
    // so we can use jetpack compose
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("se.mindi", appContext.packageName)
    }

        @Test
        @Throws(TimeoutException::class)
        fun parserFinds5Buttons() {
            val expectedNumButtons = 5
            composeRule.setContent { Views.ListOfNButtons(expectedNumButtons) }
            val semanticsNodeInteraction = composeRule.onRoot()
            val uiNodeRoot = semanticsNodeInteraction.toUINode()
            val parser = AccessibilityEventUIParser.parse(uiNodeRoot)
            val numButtonsFound = parser.uiNodes.count { it.nodeType == UINodeType.CLICKABLE }
            assertEquals(expectedNumButtons, numButtonsFound)
        }

        @Test
        @Throws(TimeoutException::class)
        fun parserFinds5Buttons5Texts() {
            val expectedNum = 5
            composeRule.setContent {
                Views.ListOfNButtons(expectedNum)
                Views.ListOfNTexts(expectedNum)
            }
            val semanticsNodeInteraction = composeRule.onRoot()
            val uiNodeRoot = semanticsNodeInteraction.toUINode()
            val parser = AccessibilityEventUIParser.parse(uiNodeRoot)
            val numButtonsFound = parser.uiNodes.count { it.nodeType == UINodeType.CLICKABLE }
            assertEquals(expectedNum, numButtonsFound)
            val numTextsFound = parser.uiNodes.count{ it.nodeType == UINodeType.TEXTUAL }
        }

        @Test
        @Throws(TimeoutException::class)
        fun buttonEmbeddedInTextualShouldBeTextualAndClickable() {
            val expectedText = "test"
            composeRule.setContent {
                Column() {
                    Text(expectedText)
                    Button(onClick = {}) {
                    }
                }
            }
            val semanticsNodeInteraction = composeRule.onRoot()
            val uiNodeRoot = semanticsNodeInteraction.toUINode()
            val parser = AccessibilityEventUIParser.parse(uiNodeRoot)
            println("PARSER $parser")

            val expectedNumNodes = 2
            assertEquals(expectedNumNodes, parser.uiNodes.count()) // should only have 1 element
            val textNode = parser.uiNodes[0]
            val clickNode = parser.uiNodes[1]

            assertEquals(UINodeType.TEXTUAL, textNode.nodeType)
            assertEquals(UINodeType.CLICKABLE, clickNode.nodeType)

            assertEquals(1, textNode.childrenText.count())
            assertEquals(0, clickNode.childrenText.count())

            assertEquals("[$expectedText]", textNode.childrenText[0])
        }

            @Test
            @Throws(TimeoutException::class)
            fun textEmbeddedInButtonResultsInClickable() {
                val expectedText = "test"
                composeRule.setContent {
                    Button(onClick = {}) {
                        Text(expectedText)
                    }
                }
                val semanticsNodeInteraction = composeRule.onRoot()
                val uiNodeRoot = semanticsNodeInteraction.toUINode()
                val parser = AccessibilityEventUIParser.parse(uiNodeRoot)
                println("THE PARSER SHOWUP!!! $parser")
                assertEquals(2, parser.uiNodes.count()) // should only have 1 element
                val node = parser.uiNodes[0]
                assertEquals(UINodeType.TEXTUAL, node.nodeType) // should be clickable
                assertEquals(0, node.childrenText.count()) // should only be 1 child text
            }
}
