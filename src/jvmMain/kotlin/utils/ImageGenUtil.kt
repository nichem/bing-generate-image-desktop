package utils


import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.URLEncoder
import java.util.Date
import kotlin.random.Random

private const val BING_URL = "https://www.bing.com"

class ImageGenUtil(private val u: String, private val s: String, private val proxyString: String = "") {
    companion object {
//        private const val BING_URL = "https://www.bing.com"
//        fun newInstance() = ImageGenUtil(Repository.U, Repository.S)
    }

    private val proxy by lazy {
        val proxyRegex = Regex(".*?:\\d+")
        if (proxyRegex.matches(proxyString)) {
            val address = proxyString.split(":")[0]
            val port = proxyString.split(":")[1].toInt()
            Proxy(Proxy.Type.HTTP, InetSocketAddress(address, port))
        } else null
    }

    private val client = OkHttpClient.Builder()
        .proxy(proxy)
        .build()
    private val client2 = OkHttpClient.Builder()
        .proxy(proxy)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private val cookies = mutableMapOf("_U" to u, "SRCHHPGUSR" to s)


    suspend fun getImages(
        p: String,
        onError: (String) -> Unit = {
            Log.e("test", it)
        },
        onProgress: (Int) -> Unit
    ): List<String> {
        val url = "$BING_URL/images/create?q=${p.urlEncode()}&rt=4&FORM=GENCRE"
        var res = post(url, mapOf("q" to p.urlEncode(), "qs" to "ds"), false)
        onProgress(25)
        if (res?.code != 302) {
            val url2 = "$BING_URL/images/create?q=${p.urlEncode()}&rt=3&FORM=GENCRE"
            res = post(url2, mapOf(), false)
            if (res?.code != 302) {
                onError("error code 1,response code:${res?.code}")
                return emptyList()
            }
        }
        var redirectUrl = res.headers["Location"] ?: ""
        redirectUrl = redirectUrl.replace("&nfy=1", "")
        Log.d("test", "redirectUrl: $redirectUrl")

        if (redirectUrl.isBlank()) {
            onError("error code 2")
            return emptyList()
        }
        val tmp = redirectUrl.split("id=")
        if (tmp.isEmpty()) {
            onError("error code 3")
            return emptyList()
        }
        val id = tmp.last()
        Log.d("test", "id:$id")
        get("$BING_URL${redirectUrl}", true)
        onProgress(50)
        var text = ""
        var count = 0
        while (true) {
            val url2 = "$BING_URL/images/create/async/results/${id}?q=${p.urlEncode()}"
            val res2 =
                post(url2, mapOf(), true)
            if (res2 != null && res2.code != 200) {
                onError("error code 4")
                return emptyList()
            } else {
                text = res2?.body?.string() ?: ""
                Log.d("test", "result text: $text")
                if ("errorMessage" in text || text.isEmpty()) delay(1000)
                else break
            }
            onProgress(Math.min(75 + count, 99))
            count++
        }
        val regex = Regex("src=\"(.*?)\"")
        val result = regex.findAll(text)
        val imageUrls = result.map {
            it.groupValues[1]
        }.toMutableList()
        imageUrls.removeAll { "svg" in it }
        onProgress(100)
        return imageUrls.map { it.replace("270", "1024") }
    }


    private val baseHeaders = mapOf(
        "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "accept-language" to "en-US,en;q=0.9",
        "cache-control" to "max-age=0",
        "content-type" to "application/x-www-form-urlencoded",
        "referrer" to "https://www.bing.com/images/create/",
        "origin" to "https://www.bing.com",
        "user-agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63",
    )

    private val random by lazy {
        Random(Date().time)
    }

    private fun buildHeaders(): Headers {
        val builder = Headers.Builder()
        for (i in baseHeaders.entries) {
            builder.add(i.key, i.value)
        }
        val tmp =
            "13.${random.nextInt(104, 108)}.${random.nextInt(0, 256)}.${random.nextInt(0, 256)}"
        builder.add("x-forwarded-for", tmp)
        builder.add("Cookie", cookies.toCookie())
        return builder.build()
    }

    private fun Map<String, String>.toCookie(): String {
        var cookie = ""
        for (i in entries) {
            cookie += "${i.key}=${i.value};"
        }
        return cookie.removeSuffix(";")
    }

    private fun MutableMap<String, String>.updateCookie(setCookie: String) {
        val cookies = setCookie.split(";")
        var count = 0
        cookies.forEach {
            val i = it.indexOf("=")
            if (i >= 0) {
                count++
                set(it.substring(0, i), it.substring(i + 1, it.length))
            }
        }
        Log.d("test", "update cookie count:$count")
    }

    private fun String.urlEncode(): String {
        return URLEncoder.encode(this, "UTF-8")
    }

    private suspend fun post(
        url: String,
        data: Map<String, String>,
        isRedirect: Boolean
    ): Response? {
        val formBodyBuilder = FormBody.Builder()
        data.forEach { e ->
            formBodyBuilder.add(e.key, e.value)
        }
        val request = Request.Builder()
            .url(url)
            .headers(buildHeaders())
            .post(formBodyBuilder.build())
            .build()
        val client = if (isRedirect) client else client2
        return withContext(IO) {
            try {
                val response = client.newCall(request).execute()
                cookies.updateCookie(response.headers["Set-Cookie"] ?: "")
                Log.d("test", "[POST $url] ${response.code} ${response.message}")
                response
            } catch (e: Exception) {
                Log.e("test-post-err", e.stackTraceToString())
                null
            }
        }
    }


    private suspend fun get(url: String, isRedirect: Boolean): Response? {
        val request = Request.Builder()
            .url(url)
            .headers(buildHeaders())
            .get()
            .build()
        val client = if (isRedirect) client else client2
        return withContext(IO) {
            try {
                val response = client.newCall(request).execute()
                cookies.updateCookie(response.headers["Set-Cookie"] ?: "")
                Log.d("test", "[GET $url] ${response.code} ${response.message}")
                response
            } catch (e: Exception) {
                Log.e("test-post-err", e.stackTraceToString())
                null
            }
        }

    }
}