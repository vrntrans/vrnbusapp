package ru.boomik.vrnbus.dialogs

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowInsetsCompat
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.managers.DataManager

class FaveParamsDialog {
    companion object {
        fun show(activity: Activity, type: String, mInsets: WindowInsetsCompat) {


            val stationsList = DataManager.stations
            if (stationsList == null) {
                Toast.makeText(activity, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
                return
            }

            val decorView = activity.window.decorView as FrameLayout

            AsyncLayoutInflater(activity).inflate(R.layout.select_station_dialog, decorView) { view, _, _ ->
                val dialogView = view as LinearLayout
                dialogView.tag = "dialog"
                val paramsFirst = dialogView.layoutParams as ViewGroup.MarginLayoutParams
                val params = dialogView.layoutParams as ViewGroup.MarginLayoutParams
                val paramsLast = dialogView.getChildAt(dialogView.childCount - 1).layoutParams as ViewGroup.MarginLayoutParams
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    paramsFirst.topMargin += mInsets.systemWindowInsetTop
                    params.leftMargin += mInsets.systemWindowInsetLeft
                    params.rightMargin += mInsets.systemWindowInsetRight
                    paramsLast.bottomMargin = activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt() + mInsets.systemWindowInsetBottom
                } else {
                    paramsFirst.topMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                    paramsLast.bottomMargin += activity.resources.getDimension(R.dimen.activity_vertical_margin).toInt()
                }
                dialogView.setOnClickListener {
                    decorView.removeView(dialogView)
                }


                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            }
            }
    }
}