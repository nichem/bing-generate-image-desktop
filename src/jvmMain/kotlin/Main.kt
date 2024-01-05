import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import utils.LocalStore


@Composable
@Preview
fun App() {
    var settingIsVisible by remember {
        mutableStateOf(false)
    }

    MaterialTheme {
        Button(
            onClick = { settingIsVisible = true },
        ) {
            Text("to Setting")
        }

        SettingDialog(settingIsVisible) {
            settingIsVisible = false
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
fun SettingDialog(visible: Boolean, onClose: () -> Unit) {
    var cookieU by remember {
        mutableStateOf("")
    }
    var cookieS by remember {
        mutableStateOf("")
    }
    var change by remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        cookieU = LocalStore.cookieU
        cookieS = LocalStore.cookieS
    }
    Dialog(onCloseRequest = onClose, visible = visible, title = "设置") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(10.dp))
            TextField(
                cookieU,
                onValueChange = {
                    cookieU = it
                    change = true
                },
                label = { Text("Cookie _U") },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(10.dp))
            TextField(
                cookieS,
                onValueChange = {
                    cookieS = it
                    change = true
                },
                label = { Text("Cookie SRCHHPGUSR") },
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Button({
                LocalStore.cookieU = cookieU
                LocalStore.cookieS = cookieS
                change = false
            }, enabled = change) {
                Text("保存")
            }
        }
    }
}
