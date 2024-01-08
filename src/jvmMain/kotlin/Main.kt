import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import ui.*
import utils.DownloadUtil
import utils.ImageGenUtil
import utils.LocalStore
import utils.Log
import java.awt.Desktop


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
        var toast by remember { mutableStateOf("") }
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
                    val setting = LocalStore.setting
                    if (setting.cookieU.isBlank() || setting.cookieS.isBlank()) {
                        toast = "你还未设置cookie"
                        getBtnState = getBtnState.copy(enable = true, text = "提示词")
                        return@launch
                    }
                    val list = loadImageUrls(prompt, setting) {
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
        if (toast.isNotBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Toast(toast) {
                    toast = ""
                }
            }
        }
    }
}

private suspend fun loadImageUrls(
    prompt: String,
    setting: LocalStore.Setting,
    onCallback: (msg: String) -> Unit
): List<String> {
    val genImage = ImageGenUtil(
        setting.cookieU, setting.cookieS, setting.proxy
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
                Item("打开输出目录") {
                    Desktop.getDesktop().open(LocalStore.getImagesDir())
                }
                Item("打开日志文件") {
                    Desktop.getDesktop().open(Log.getLogFile())
                }
                Item("设置") {
                    settingIsVisible = true
                }
            }
        }
        App(settingIsVisible, onSettingClose = { settingIsVisible = false })
    }
}