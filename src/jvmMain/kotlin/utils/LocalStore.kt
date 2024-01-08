package utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

private const val APP_ID = "GenerateImageConfig"

object LocalStore {
    private val gson = Gson()
    private val rootDir = File(System.getProperty("user.dir"))
    private val configFile = File(rootDir, "$APP_ID.json").apply {
        if (!exists()) {
            createNewFile()
            writeText("{}")
        }
        Log.d("test", "${path}是否存在：${exists()} 是否可写：${canWrite()}")
    }
    private val imagesDir = File(rootDir, "images").apply {
        if (!exists()) mkdirs()
    }

    private fun getJsonObject(): JsonObject = JsonParser.parseString(configFile.readText()).asJsonObject

    private fun updateJsonObject(jsonObject: JsonObject) {
        val json = jsonObject.toString()
        configFile.writeText(json)
    }

    private fun JsonObject.add(key: String, value: Any) {
        val jsonElement = gson.toJsonTree(value)
        add(key, jsonElement)
    }

    var num: Int
        get() = getJsonObject().get("num")?.asInt ?: 0
        set(value) {
            updateJsonObject(getJsonObject().apply { add("num", value) })
        }

    data class Setting(
        var cookieU: String = "",
        var cookieS: String = "",
        var proxy: String = "",
        var isShowImageList: Boolean = true,
        var isOutputLog: Boolean = false
    )

    var setting: Setting
        get() = gson.fromJson(configFile.readText(), Setting::class.java)
        set(value) {
            configFile.writeText(gson.toJson(value))
        }

    fun getNewImageFile(): File {
        val time = System.currentTimeMillis()
        return File(imagesDir, "$time.jpeg")
    }

    fun getImageFiles(): List<File> {
        return imagesDir.listFiles()?.filter {
            it.name.endsWith(".jpeg") && !it.isHidden
        }?.sortedByDescending {
            it.lastModified()
        } ?: emptyList()
    }

    fun getImagesDir(): File {
        return imagesDir
    }
}