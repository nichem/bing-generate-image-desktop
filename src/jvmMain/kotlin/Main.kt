import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import utils.DownloadUtil
import utils.ImageGenUtil
import utils.LocalStore
import java.io.File


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
        SendLayout(getBtnState) { text ->
            val prompt = text.trim()
            if (prompt.isBlank()) return@SendLayout
            scope.launch(IO) {
                getBtnState = getBtnState.copy(enable = false, text = "获取中")
                val list = loadImageUrls(prompt) {
                    getBtnState = getBtnState.copy(text = "获取中：$it")
                }
                list.forEachIndexed { index, url ->
                    downloadList(url) {
                        getBtnState = getBtnState.copy(text = "下载${index + 1}/${list.size}：$it")
                    }
                }
//                println("下载链接：$list")
                getBtnState = getBtnState.copy(enable = true, text = "获取")
            }
        }
    }
}

private suspend fun loadImageUrls(prompt: String, onCallback: (msg: String) -> Unit): List<String> {
    val genImage = ImageGenUtil(
        LocalStore.cookieU,
        LocalStore.cookieS,
        LocalStore.proxy
    )
    return genImage.getImages(prompt) {
        onCallback("${it}%")
    }
}

private suspend fun downloadList(url: String, onCallback: (msg: String) -> Unit) {
    DownloadUtil.download(url, LocalStore.getNewImageFile()) {
        onCallback("$it%")
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

@Composable
fun SendLayout(btnState: BtnState, onSend: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
        )
        Button(onClick = { onSend(prompt) }, enabled = btnState.enable) {
            Text(btnState.text)
        }
    }
}

@Composable
fun ImageList(refresh: Int) {
    var list: List<File> by remember { mutableStateOf(emptyList()) }
    LaunchedEffect(refresh) {
        list = withContext(IO) { LocalStore.getImageFiles() }
    }
//    LazyVerticalGrid(columns = ){
//
//    }
}