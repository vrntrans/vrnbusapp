package ru.boomik.vrnbus.utils

import android.app.Activity
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by boomv on 14.03.2018.
 */
fun loadJSONFromAsset(activity : Activity, fileName: String): String {
    var json: String? = null
    try {
        val inputStream = activity.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charset.forName("UTF-8"))
    } catch (ex: IOException) {
        ex.printStackTrace()
        return ""
    }

    return json
}