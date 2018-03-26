package ru.boomik.vrnbus.utils

import android.app.Activity
import ru.boomik.vrnbus.Log
import java.io.IOException
import java.nio.charset.Charset

fun loadJSONFromAsset(activity : Activity, fileName: String, loaded: (String) -> Unit){
    Thread {
        loaded(try {
            val inputStream = activity.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (exception: IOException) {
            Log.e("Hm..", exception)
            ""
        })
    }.run()
}