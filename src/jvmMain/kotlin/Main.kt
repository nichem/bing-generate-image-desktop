import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import utils.ImageGenUtil
import utils.LocalStore


data class BtnState(
    var enable: Boolean = true,
    var text: String
)

@Composable
@Preview
fun App() {
    var settingIsVisible by remember {
        mutableStateOf(false)
    }

    MaterialTheme {
        var getBtnState by remember {
            mutableStateOf(BtnState(text = "获取"))
        }
        val scope = rememberCoroutineScope()
        Row {
            Button(
                onClick = { settingIsVisible = true },
            ) {
                Text("to Setting")
            }

            Button(
                onClick = {
                    scope.launch(IO) {
                        getBtnState = getBtnState.copy(enable = false, text = "获取中")
                        val u = LocalStore.cookieU
                        val s = LocalStore.cookieS
                        val genImage = ImageGenUtil(u, s)
                        val list = genImage.getImages("兔子") {
                            getBtnState = getBtnState.copy(text = "获取中：${it}%")
                        }
                        println("下载链接：$list")
                        getBtnState = getBtnState.copy(enable = true, text = "获取")
                    }
                }, enabled = getBtnState.enable
            ) {
                Text(
                    getBtnState.text
                )
            }
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
