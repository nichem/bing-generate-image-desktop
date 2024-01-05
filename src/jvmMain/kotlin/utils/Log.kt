package utils

object Log {
    fun e(tag: String, msg: String) {
        println("$tag   $msg")
    }

    fun d(tag: String, msg: String) {
        println("$tag   $msg")
    }
}