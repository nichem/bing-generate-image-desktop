package utils

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import utils.Log
import java.io.File
import java.io.FileOutputStream

object DownloadUtil {
    private val okHttpClient: OkHttpClient = OkHttpClient()

    /**
     * @param url 下载连接
     * @param downloadFile 要写入的文件对象
     * @param listener 下载监听
     */
    suspend fun download(url: String, downloadFile: File, onDownloading: (Int) -> Unit): Boolean {
        Log.d("test", "downloadUrl:$url")
        val request: Request = Request
            .Builder()
            .url(url)
            .build()
        val response = withContext(IO) {
            try {
                okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                null
            }
        } ?: return false

        val body = response.body ?: return false
        return withContext(IO) {
            try {
                val ins = body.byteStream()
                val total = body.contentLength()
                val fos = FileOutputStream(downloadFile)
                val buf = ByteArray(2048)
                var downloadedLength: Long = 0
                while (true) {
                    val len = ins.read(buf)
                    if (len == -1) break
                    fos.write(buf, 0, len)
                    downloadedLength += len.toLong()
                    val progress = (downloadedLength * 1.0f / total * 100).toInt()
                    // 下载中
                    onDownloading(progress)
                }
                fos.flush()
                fos.close()
                // 下载完成
                true
            } catch (e: Exception) {
                Log.e("test", e.stackTraceToString())
                false
            }
        }
    }
}