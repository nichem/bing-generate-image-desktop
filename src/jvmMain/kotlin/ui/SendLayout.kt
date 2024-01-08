package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue

data class BtnState(
    var enable: Boolean = true,
    var text: String
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SendLayout(btnState: BtnState, modifier: Modifier = Modifier.fillMaxWidth(), onSend: (String) -> Unit) {
    var prompt by remember { mutableStateOf(TextFieldValue("")) }
    Column(modifier) {
        OutlinedTextField(
            value = prompt,
            onValueChange = {
                prompt = it
            },
            modifier = Modifier.fillMaxWidth().onPreviewKeyEvent {
                if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                    onSend(prompt.text)
                    true
                } else {
                    false
                }
            }, label = {
                Text(btnState.text)
            }, trailingIcon = {
                IconButton(onClick = { onSend(prompt.text) }, enabled = btnState.enable) {
                    Icon(Icons.Default.Send, "")
//                    Text(btnState.text)
                }
            },
            enabled = btnState.enable,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
        )
    }
}