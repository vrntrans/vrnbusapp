package ru.boomik.vrnbus.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.boomik.vrnbus.Log
import java.io.IOException
import java.nio.charset.Charset
import android.graphics.drawable.Drawable
import android.os.Build
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
fun Int.color(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity.resources.getColor(this, activity.theme)
    } else activity.resources.getColor(this)
}

fun loadJSONFromAsset(context: Context, fileName: String, loaded: (String) -> Unit) {
    Thread {
        loaded(try {
            val inputStream = context.assets.open(fileName)
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

suspend fun loadStringFromFile(file: File): String = withContext(Dispatchers.IO) {
    try {
        val inputStream = file.inputStream()
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer, Charset.forName("UTF-8"))
    } catch (exception: IOException) {
        Log.e("Hm..", exception)
        ""
    }
}

suspend fun saveStringToFile(file: File, value: String) = withContext(Dispatchers.IO) {
    try {
        if (file.exists()) file.delete()
        val outputStream = file.outputStream()
        val osw = OutputStreamWriter(outputStream)
        osw.write(value)
        osw.close()
    } catch (exception: IOException) {
        Log.e("Hm..", exception)
        ""
    }
}


fun loadStringFromNetworkAsync(url: String) = GlobalScope.async(Dispatchers.IO) {
    suspendCoroutine<String?> { cont ->
       try {
           url.httpGet().responseString(Charsets.UTF_8) { _, response, result ->
               Log.e("Request", response.statusCode.toString() + "(" + response.url + ")")
               if (result.component2() != null) cont.resumeWithException(result.component2()!!)
               else cont.resume(result.component1())
           }
       } catch (e: Throwable) {
           cont.resumeWithException(e)
       }
    }
}

fun createImageRoundedBitmap(type: Int, icon: Drawable?, size: Int, name: String, azimuth: Double, color: Int): Bitmap {

    val paintCircle = Paint()
    val paintText = Paint()

    paintCircle.color = color
    paintCircle.alpha = 255
    paintCircle.isAntiAlias = true
    paintCircle.style = Paint.Style.FILL


    val arrowSize = size * 0.2F

    var width = size
    if (type == 2) width = size * 3


    val output = Bitmap.createBitmap(width, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    canvas.save()
    canvas.rotate(azimuth.toFloat(), size / 2F, size / 2F)

    canvas.drawCircle(size / 2F, size / 2F, size / 2F - arrowSize, paintCircle)

    val path = Path()
    path.moveTo(size / 2F - arrowSize / 2f, arrowSize)
    path.lineTo(size / 2F + arrowSize / 2f, arrowSize)
    path.lineTo(size / 2F, 0F)
    path.lineTo(size / 2F - arrowSize / 2f, arrowSize)
    path.close()
    canvas.drawPath(path, paintCircle)

    canvas.restore()


    if (type > 0) {
        val padding = (size * 0.3).toInt()

        icon?.let {
            icon.setBounds(padding, padding, size - padding, size - padding)
            icon.draw(canvas)
        }

        if (type == 2) {
            val textSize = size / 2.4F
            paintText.color = Color.WHITE
            paintText.textSize = textSize
            val textWidth = paintText.measureText(name)

            val textRect = Rect((size - arrowSize).toInt(), ((size - textSize).toInt()), (size - arrowSize + textWidth + textWidth * 0.2).toInt(), size)
            paintCircle.style = Paint.Style.FILL
            paintCircle.color = color
            paintCircle.alpha = 180
            canvas.drawRect(textRect, paintCircle)

            canvas.drawText(name, (size - arrowSize + textWidth * 0.1).toFloat(), size.toFloat() - textSize * 0.2f, paintText)
        }
    }
    return output
}


fun requestPermission(activity: Activity, permission: String, requestCode: Int): Boolean {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        return false
        /*
        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity, arrayOf(permission),Consts.LOCATION_PERMISSION_REQUEST)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }*/
    } else {
        // Permission has already been granted
        return true
    }
}