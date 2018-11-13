package ru.boomik.vrnbus.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import ru.boomik.vrnbus.Log
import java.io.IOException
import java.nio.charset.Charset
import android.graphics.drawable.Drawable


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

fun createImageRoundedBitmap(icon: Drawable, size: Int, name: String, azimuth: Double): Bitmap {


    val paintCircle = Paint()
    val paintText = Paint()

    paintCircle.color = Color.BLUE
    paintCircle.isAntiAlias = true
    paintCircle.style = Paint.Style.FILL


    val textSize = size / 2.4F
    paintText.color = Color.WHITE
    paintText.textSize = textSize
    val textWidth = paintText.measureText(name)

    val arrowSize = size * 0.2F

    val output = Bitmap.createBitmap(size * 3, size, Bitmap.Config.ARGB_8888)
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


    val padding = (size * 0.3).toInt()
    icon.setBounds(padding, padding, size - padding, size - padding)
    icon.draw(canvas)

    val textRect = Rect((size-arrowSize).toInt(), ((size - textSize).toInt()), (size - arrowSize + textWidth + textWidth * 0.2).toInt(), size)
    paintCircle.style = Paint.Style.FILL
    paintCircle.color = Color.BLUE
    paintCircle.alpha = 130
    canvas.drawRect(textRect, paintCircle)
    /* paintCircle.alpha=255
     paintCircle.color = Color.WHITE
     paintCircle.style = Paint.Style.STROKE
     paintCircle.strokeWidth = size/36f
     canvas.drawRect(textRect, paintCircle)*/

    canvas.drawText(name, (size - arrowSize + textWidth * 0.1).toFloat(), size.toFloat() - textSize * 0.2f, paintText)



    return output
}

fun createImageRounded(resource: Drawable, size: Int, name: String, azimuth: Double): BitmapDescriptor? {

    return BitmapDescriptorFactory.fromBitmap(createImageRoundedBitmap(resource, size, name, azimuth))
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