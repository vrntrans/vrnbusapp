package ru.boomik.vrnbus.views

import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import android.text.TextWatcher
import android.graphics.drawable.Drawable
import ru.boomik.vrnbus.R
import android.text.Editable
import android.content.Context
import android.util.AttributeSet
import android.annotation.SuppressLint
import android.view.View.OnTouchListener
import android.view.View
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.hootsuite.nachos.ClearableMultiAutoCompleteTextView

/**
 * sub class of [android.widget.AutoCompleteTextView] that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
 * based on code from {https://www.gubed.net/clearableautocompletetextview}
 * @author Michael Derazon
 */
class ClearableMultiAutoCompleteTextView : AppCompatMultiAutoCompleteTextView, TextWatcher {
    // was the text just cleared?
    var justCleared = false

    // if not set otherwise, the default clear listener clears the text in the
    // text view
    private val defaultClearListener = ClearableMultiAutoCompleteTextView.OnClearListener {
        val et = this@ClearableMultiAutoCompleteTextView
        et.setText("")
    }
    private var onClearListener = defaultClearListener

    // The image we defined for the clear button
    val imgClearButton : Drawable? = ContextCompat.getDrawable(this.context, R.drawable.ic_clear)
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s == null || s.toString().isEmpty()) hideClearButton() else showClearButton()
    }

    override fun afterTextChanged(s: Editable) {}
    interface OnClearListener {
        fun onClear()
    }

    /* Required methods, not used in this implementation */
    constructor(context: Context?) : super(context!!) {
        init()
    }

    /* Required methods, not used in this implementation */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        init()
    }

    /* Required methods, not used in this implementation */
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        // Set the bounds of the button

        // if the clear button is pressed, fire up the handler. Otherwise do nothing
        setOnTouchListener { v: View?, event: MotionEvent ->
            val et = this@ClearableMultiAutoCompleteTextView
            if (et.compoundDrawables[2] == null) return@setOnTouchListener false
            if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false
            if (imgClearButton!= null && event.x > et.width - et.paddingRight - imgClearButton.intrinsicWidth) {
                onClearListener.onClear()
                justCleared = true
            }
            false
        }
        if (text != null && text.toString().isNotEmpty()) showClearButton()
        addTextChangedListener(this)
    }


    fun setOnClearListener(clearListener: ClearableMultiAutoCompleteTextView.OnClearListener) {
        onClearListener = clearListener
    }

    fun hideClearButton() {
        setCompoundDrawables(null, null, null, null)
    }

    fun showClearButton() {
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null)
    }
}