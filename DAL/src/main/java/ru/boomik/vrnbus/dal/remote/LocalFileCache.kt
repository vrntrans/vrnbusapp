package ru.boomik.vrnbus.dal.remote

import com.google.gson.Gson
import java.io.File
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*


@Suppress("unused")
class LocalFileCache(cachePath: String) {

    var gson: Gson = Gson()
    private var cacheDir: File = File(cachePath, "Responses")
    private val ext = ".json"

    init {
        cacheDir.mkdirs()
    }


    fun <T> write(key : String, data : T, cacheTime : Long? = null) {
            try {
                val cacheKey = URLEncoder.encode(key, "UTF-8")
                val currentFile = getFileNameByKey(cacheKey)
                if (currentFile!=null) delete(currentFile)

                val date = Date()

                val validUntil = if (cacheTime!=null) date.time+cacheTime else Long.MAX_VALUE
                val fileName = "${key}#${validUntil}${ext}"
                val file = File(cacheDir, fileName)

                val value = gson.toJson(data)

                val outputStream = file.outputStream()
                val osw = OutputStreamWriter(outputStream)
                osw.write(value)
                osw.close()
            }
            catch (e : Throwable) {
                //ignored
                val z = e
            }

    }

    private fun delete(fileName : String) {
        if (fileName.isNotBlank()) {
            val file = File(cacheDir, fileName)
            if(file.exists()) file.delete()
        }
    }

    fun getValidUntil(key : String) : Long {
        val cacheKey = URLEncoder.encode(key, "UTF-8")
        val fileName = getFileNameByKey(cacheKey) ?: return 0
        return getValidUntilFromFile(cacheKey, fileName)
    }


    fun <T> get(key : String, cacheClass : Class<T>) : T? {
        try {
            val cacheKey = URLEncoder.encode(key, "UTF-8")

            val fileName = getFileNameByKey(cacheKey) ?: return null
            val validUntil = getValidUntilFromFile(cacheKey, fileName)
            val valid = checkItemValid(fileName, validUntil)
            var value : T? = null
            if (valid) {
                try {
                    val file = File(cacheDir, fileName)

                    val inputStream = file.inputStream()
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    val valueString = String(buffer, Charset.forName("UTF-8"))
                    value = gson.fromJson<T>(valueString, cacheClass)

                }
                catch (e : Throwable) {
                    println(e)
                }
            }

            return value
        }
        catch  (e : Throwable) {
            val z = e
            return null
        }
    }

    private fun getValidUntilFromFile(key : String, fileName : String) : Long {
        try {
            val time = fileName.replace(ext, "").replace(key, "").replace("#", "")
            if (time.isNotBlank()) {
                val validUntilLong = time.toLongOrNull()
                if (validUntilLong != null) {
                    return Date().time + validUntilLong
                }
            }
        }
        catch (e : Throwable) {
            val z = e
            //ignored
        }

        return Long.MIN_VALUE
    }


    private fun checkItemValid(fileName : String, validUntil : Long) : Boolean {
        if (validUntil >= Date().time)
            return true
        delete(fileName)
        return false
    }

    private fun getFileNameByKey(key : String) : String? {
        return cacheDir.listFiles()?.firstOrNull { it.name.startsWith("$key#") }?.name
    }


    fun clear() {
          cacheDir.deleteRecursively()
    }

}