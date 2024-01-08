import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import utils.DownloadUtil
import utils.ImageGenUtil
import utils.LocalStore
import utils.Log
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream


data class BtnState(
    var enable: Boolean = true,
    var text: String
)

@Composable
@Preview
fun App(settingIsVisible: Boolean, onSettingClose: () -> Unit) {
    MaterialTheme {
        var getBtnState by remember {
            mutableStateOf(BtnState(text = "提示词"))
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
                    getBtnState = getBtnState.copy(enable = true, text = "提示词")
                    refresh++
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        SettingDialog(settingIsVisible, onClose = onSettingClose) {
            refresh++
        }
    }
}

private suspend fun loadImageUrls(prompt: String, onCallback: (msg: String) -> Unit): List<String> {
    val setting = LocalStore.setting
    val genImage = ImageGenUtil(
        setting.cookieU,
        setting.cookieS,
        setting.proxy
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
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(position = WindowPosition(Alignment.Center)),
        title = "AI生成图片 作者：dlearn",
        icon = painterResource("icon.png")
    ) {
        LaunchedEffect(Unit) {
            Log.logFileEnable = LocalStore.setting.isOutputLog
        }
        var settingIsVisible by remember {
            mutableStateOf(false)
        }
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
        App(settingIsVisible, onSettingClose = { settingIsVisible = false })
    }
}

@Composable
fun SettingDialog(visible: Boolean, onClose: () -> Unit, onSave: () -> Unit) {
    val rate = 0.9f
    var setting by remember { mutableStateOf(LocalStore.Setting()) }
    var change by remember {
        mutableStateOf(false)
    }
    val proxyRegex = remember { Regex(".*?:\\d+") }
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        setting = LocalStore.setting
    }
    Dialog(onCloseRequest = onClose, visible = visible, title = "设置", icon = painterResource("icon.png")) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(scrollState).weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    setting.cookieU,
                    onValueChange = {
                        setting = setting.copy(cookieU = it)
                        change = true
                    },
                    label = { Text("Cookie _U") },
                    modifier = Modifier.fillMaxWidth(rate),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                TextField(
                    setting.cookieS,
                    onValueChange = {
                        setting = setting.copy(cookieS = it)
                        change = true
                    },
                    label = { Text("Cookie SRCHHPGUSR") },
                    modifier = Modifier.fillMaxWidth(rate),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                var isError by remember { mutableStateOf(false) }
//                if (isError) Text("例如：http://127.0.0.1:7890", color = Color.Red)
                TextField(
                    setting.proxy,
                    onValueChange = {
                        isError = !proxyRegex.matches(it)
                        setting = setting.copy(proxy = it)
                        change = true
                    },
                    isError = isError,
                    label = { Text("代理") },
                    leadingIcon = {
                        Text("http://")
                    },
                    modifier = Modifier.fillMaxWidth(rate),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                Row(Modifier.fillMaxWidth(rate), verticalAlignment = Alignment.CenterVertically) {
                    Text("显示图片列表：")
                    Switch(setting.isShowImageList, onCheckedChange = {
                        setting = setting.copy(isShowImageList = it)
                        change = true
                    })
                }
                Row(Modifier.fillMaxWidth(rate), verticalAlignment = Alignment.CenterVertically) {
                    Text("输出日志：")
                    Switch(setting.isOutputLog, onCheckedChange = {
                        setting = setting.copy(isOutputLog = it)
                        change = true
                    })
                }
            }
            Button({
                val localSetting = LocalStore.setting
                if (!proxyRegex.matches(setting.proxy))
                    setting.proxy = localSetting.proxy
                LocalStore.setting = setting
                Log.logFileEnable = setting.isOutputLog
                change = false
                onSave()
            }, enabled = change) {
                Text("保存")
            }
        }
    }
}

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

@Composable
fun ImageList(refresh: Int, modifier: Modifier) {
    var list: List<File> by remember { mutableStateOf(emptyList()) }
    var isShowImageList by remember { mutableStateOf(true) }
    LaunchedEffect(refresh) {
        isShowImageList = LocalStore.setting.isShowImageList
        if (isShowImageList) {
            list = withContext(IO) { LocalStore.getImageFiles() }
        } else if (list.isNotEmpty()) list = emptyList()
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

    if (!isShowImageList) {
        Text("不展示图片，前往设置打开", modifier, textAlign = TextAlign.Center)
    }
}