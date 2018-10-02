package ru.boomik.vrnbus.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

fun createImageRounded(width: Int, height: Int, name: String, azimuth: Double): BitmapDescriptor? {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.save()
    canvas.rotate(azimuth.toFloat(),width/2F,height/2F)

    val paintCircle = Paint()
    val paintText = Paint()

    paintCircle.color = Color.BLUE
    paintCircle.isAntiAlias = true
    paintCircle.style = Paint.Style.FILL

    val arrowSize = width*0.2F
    val circleSize = width-arrowSize*2

    canvas.drawCircle(width/2F,height/2F, width/2F - arrowSize, paintCircle)

    val path = Path()
    path.moveTo(width/2F - arrowSize/2f,arrowSize)
    path.lineTo(width/2F + arrowSize/2f,arrowSize)
    path.lineTo(width/2F,0F)
    path.lineTo(width/2F - arrowSize/2f,arrowSize)
    path.close()
    canvas.drawPath(path, paintCircle)

    canvas.restore()

    paintText.color = Color.WHITE
    paintText.textSize = width/3.2F

    var textWidth = paintText.measureText(name)

    while (textWidth>circleSize) {
        paintText.textSize = paintText.textSize-0.2f
        textWidth = paintText.measureText(name)
    }

    canvas.drawText(name, (width-textWidth)/2, height.toFloat()/2F+arrowSize/2, paintText)

    return BitmapDescriptorFactory.fromBitmap(output)
}

fun requestPermission(activity : Activity, permission : String, requestCode : Int) : Boolean {
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(activity, arrayOf(permission),requestCode)
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