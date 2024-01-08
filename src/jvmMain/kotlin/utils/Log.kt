package utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object Log {
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val logFile = File(System.getProperty("user.dir"), "log.txt").apply {
        if (!exists()) createNewFile()
    }
    var logFileEnable = false
    fun e(tag: String, msg: String) {
        log(Level.ERROR, tag, msg)
    }

    fun d(tag: String, msg: String) {
        log(Level.DEBUG, tag, msg)
    }


    private fun log(level: Level, tag: String, msg: String) {
        val log = "${simpleDateFormat.format(Date())} [level:${level.name}] [tag:$tag] $msg"
        if (logFileEnable) logFile.appendText("$log\n")
        println(log)
    }

    enum class Level {
        ERROR, DEBUG
    }
}