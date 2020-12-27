package ru.boomik.vrnbus.dal.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KFunction1

@Suppress("unused")
class LocalFileCache(cachePath: String) {

    //var gson: Gson = Gson()
    //val moshiPack = MoshiPack(moshi = Moshi.Builder().build())
    var cacheDir: File = File(cachePath, "Responses")
    val ext = ".json"

    init {
        cacheDir.mkdirs()
    }

    inline fun <reified T: Any> write(key: String, data: T, cacheTime: Long? = null, logFunc: KFunction1<String, Unit>? = null) {
        try {
            val cacheKey = URLEncoder.encode(key, "UTF-8")
            val currentFile = getFileNameByKey(cacheKey)
            if (currentFile != null) delete(currentFile)

            val date = Date()

            val validUntil = if (cacheTime != null) date.time + cacheTime else Long.MAX_VALUE
            val fileName = "${key}#${validUntil}${ext}"
            logFunc?.invoke("$key Cache write: $fileName")
            val file = File(cacheDir, fileName)

            val json =  Json.encodeToString(data)
            logFunc?.invoke(key+"Cache Write:" +json)

            val outputStream = file.outputStream()
            val osw = OutputStreamWriter(outputStream)
            osw.write(json)
            osw.close()

        } catch (e: Throwable) {
            //ignored
            logFunc?.invoke("Cache Write:" +e.message+"\n"+e.stackTrace)
        }
    }


    fun delete(fileName: String) {
        if (fileName.isNotBlank()) {
            val file = File(cacheDir, fileName)
            if (file.exists()) file.delete()
        }
    }

    fun getValidUntil(key: String): Long {
        val cacheKey = URLEncoder.encode(key, "UTF-8")
        val fileName = getFileNameByKey(cacheKey) ?: return 0
        return getValidUntilFromFile(cacheKey, fileName)
    }

    @Serializable
    class CacheFileClass<T> (val data : T)


    inline fun <reified T : Any> get(key: String, logFunc: KFunction1<String, Unit>? = null): T? {
        try {
            val cacheKey = URLEncoder.encode(key, "UTF-8")
            logFunc?.invoke("$key Cache start: $cacheKey")

            val fileName = getFileNameByKey(cacheKey) ?: return null
            val validUntil = getValidUntilFromFile(cacheKey, fileName)
            val valid = checkItemValid(fileName, validUntil)
            var value: T? = null
            var valueString = ""
            logFunc?.invoke("$key Cache valid: $valid")
            if (valid) {
                try {
                    val file = File(cacheDir, fileName)
                    val inputStream = file.inputStream()
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    valueString = String(buffer, Charset.forName("UTF-8"))
                    logFunc?.invoke("valueString:\n$valueString")
                } catch (e: Throwable) {
                    logFunc?.invoke(e.message+"\n"+e.stackTrace)
                }
                try {
                    value = Json.decodeFromString<T>(valueString)
                } catch (e: Throwable) {
                    logFunc?.invoke(e.message+"\n"+e.stackTrace)
                }
            }

            logFunc?.invoke("$key Cache result: $value")
            return value
        } catch (e: Throwable) {
            logFunc?.invoke(e.message+"\n"+e.stackTrace)
            return null
        }
    }

    fun getValidUntilFromFile(key: String, fileName: String): Long {
        try {
            val time = fileName.replace(ext, "").replace(key, "").replace("#", "")
            if (time.isNotBlank()) {
                val validUntilLong = time.toLongOrNull()
                if (validUntilLong != null) {
                    return Date().time + validUntilLong
                }
            }
        } catch (e: Throwable) {
            //ignored
        }

        return Long.MIN_VALUE
    }


    fun checkItemValid(fileName: String, validUntil: Long): Boolean {
        if (validUntil >= Date().time)
            return true
        delete(fileName)
        return false
    }

    fun getFileNameByKey(key: String): String? {
        return cacheDir.listFiles()?.firstOrNull { it.name.startsWith("$key#") }?.name
    }


    fun clear() {
        cacheDir.deleteRecursively()
    }

}