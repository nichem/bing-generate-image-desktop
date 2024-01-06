import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.MenuBar
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
    MaterialTheme {
        var getBtnState by remember {
            mutableStateOf(BtnState(text = "获取"))
        }
        val scope = rememberCoroutineScope()
        Row {
            Button(
                onClick = {
                    scope.launch(IO) {
                        getBtnState = getBtnState.copy(enable = false, text = "获取中")
                        val genImage = ImageGenUtil(
                            LocalStore.cookieU,
                            LocalStore.cookieS,
                            LocalStore.proxy
                        )
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
    }
}

fun main() = application {
    var settingIsVisible by remember {
        mutableStateOf(false)
    }
    Window(onCloseRequest = ::exitApplication) {
        MenuBar {
            Menu("文件") {
                Item("设置") {
                    settingIsVisible = true
                }
            }
        }
        App()
        SettingDialog(settingIsVisible) {
            settingIsVisible = false
        }
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
    var proxy by remember { mutableStateOf("") }
    var change by remember {
        mutableStateOf(false)
    }
    val proxyRegex = remember { Regex(".*?:\\d+") }
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        cookieU = LocalStore.cookieU
        cookieS = LocalStore.cookieS
    }
    Dialog(onCloseRequest = onClose, visible = visible, title = "设置") {
        Column(Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(scrollState).weight(1f).fillMaxWidth()
            ) {
                Spacer(Modifier.height(10.dp))
                TextField(
                    cookieU,
                    onValueChange = {
                        cookieU = it
                        change = true
                    },
                    label = { Text("Cookie _U") },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    cookieS,
                    onValueChange = {
                        cookieS = it
                        change = true
                    },
                    label = { Text("Cookie SRCHHPGUSR") },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                var isError by remember { mutableStateOf(false) }
//                if (isError) Text("例如：http://127.0.0.1:7890", color = Color.Red)
                TextField(
                    proxy,
                    onValueChange = {
                        isError = !proxyRegex.matches(it)
                        proxy = it
                        change = true
                    },
                    isError = isError,
                    label = { Text("代理") },
                    leadingIcon = {
                        Text("http://")
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
            }
            Button({
                LocalStore.cookieU = cookieU
                LocalStore.cookieS = cookieS
                if (proxyRegex.matches(proxy)) LocalStore.proxy = proxy
                change = false
            }, enabled = change) {
                Text("保存")
            }
        }
    }
}
