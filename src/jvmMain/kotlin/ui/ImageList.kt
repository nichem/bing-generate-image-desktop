package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.LocalStore
import java.io.File
import java.io.FileInputStream

@Composable
fun ImageList(refresh: Int, modifier: Modifier) {
    var list: List<File> by remember { mutableStateOf(emptyList()) }
    var isShowImageList by remember { mutableStateOf(true) }
    LaunchedEffect(refresh) {
        isShowImageList = LocalStore.setting.isShowImageList
        if (isShowImageList) {
            list = withContext(Dispatchers.IO) { LocalStore.getImageFiles() }
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