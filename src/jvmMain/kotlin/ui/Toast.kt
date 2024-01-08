package ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Toast(msg: String, onRequestClose: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1000)
        onRequestClose()
    }
    Card(shape = RoundedCornerShape(10.dp), backgroundColor = Color.White) {
        Text(msg, modifier = Modifier.padding(20.dp, 10.dp), textAlign = TextAlign.Center)
    }
}