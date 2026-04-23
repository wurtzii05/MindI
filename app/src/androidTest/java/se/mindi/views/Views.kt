package se.mindi.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class Views {
    companion object {
        @Composable
        fun ListOfNButtons(n: Int) {
            val texts = List(n) {
                Button(
                    onClick = {}
                ) { }
            }

            LazyColumn() {
                texts.forEach {
                    it
                }
            }
        }

        @Composable
        fun ListOfNTexts(n: Int) {
            val texts = List(n) {
                Text( "$it" )
            }

            LazyColumn() {
                texts.forEach {
                    it
                }
            }
        }

        @Composable
        fun Empty() {

        }

        @Composable
        fun ButtonEmbeddedInButton() {
            Button(
                onClick = {}
            ) {
                Button(
                    onClick = {}
                ) { }
            }
        }
    }
}