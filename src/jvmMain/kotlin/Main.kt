import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import utils.DownloadUtil
import utils.ImageGenUtil
import utils.LocalStore
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream


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
        var refresh by remember {
            mutableStateOf(0)
        }
        val scope = rememberCoroutineScope()
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageList(refresh, Modifier.fillMaxWidth(0.9f).weight(1f))
            SendLayout(getBtnState, Modifier.fillMaxWidth(0.9f)) { text ->
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
                    refresh++
                }
            }
            Spacer(Modifier.height(10.dp))
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
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(position = WindowPosition(Alignment.Center))
    ) {
        MenuBar {
            Menu("文件") {
                Item("设置") {
                    settingIsVisible = true
                }
                Item("打开输出目录") {
                    Desktop.getDesktop().open(LocalStore.getImagesDir())
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
        proxy = LocalStore.proxy
    }
    Dialog(onCloseRequest = onClose, visible = visible, title = "设置") {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
fun SendLayout(btnState: BtnState, modifier: Modifier = Modifier.fillMaxWidth(), onSend: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = prompt,
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
fun ImageList(refresh: Int, modifier: Modifier) {
    var list: List<File> by remember { mutableStateOf(emptyList()) }
    LaunchedEffect(refresh) {
        list = withContext(IO) { LocalStore.getImageFiles() }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list) {
            Image(BitmapPainter(loadImageBitmap(FileInputStream(it))), "")
        }
    }
}