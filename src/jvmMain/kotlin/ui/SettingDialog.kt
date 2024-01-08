package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import utils.LocalStore
import utils.Log

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
