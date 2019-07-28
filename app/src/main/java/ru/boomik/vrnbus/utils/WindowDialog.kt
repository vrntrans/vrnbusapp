package ru.boomik.vrnbus.utils

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.transition.Slide
import androidx.transition.TransitionManager


object WindowDialog {

    fun Show(activity: Activity, view: IWindowDialog) {

        val decorView = activity.window.decorView as FrameLayout

        decorView.postDelayed({
            val t = Slide(Gravity.TOP)
            TransitionManager.beginDelayedTransition(decorView, t)
            decorView.addView(view.getView(activity))
            //nachos.requestFocus()
            //imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
        }, 0)
    }


}


interface IWindowDialog {
    var showAnimation: Gravity
    var hideAnimation: Gravity
    fun getView(activity: Activity): View
    fun onHide()
}