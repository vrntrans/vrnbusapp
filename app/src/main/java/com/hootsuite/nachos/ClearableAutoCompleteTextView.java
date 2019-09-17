package com.hootsuite.nachos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import ru.boomik.vrnbus.R;
/**
        * sub class of {@link android.widget.AutoCompleteTextView} that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
         * based on code from {https://www.gubed.net/clearableautocompletetextview}
        * @author Michael Derazon
        *
        */
public class ClearableAutoCompleteTextView extends androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView implements TextWatcher {
    // was the text just cleared?
    boolean justCleared = false;

    // if not set otherwise, the default clear listener clears the text in the
    // text view
    private OnClearListener defaultClearListener = () -> {
        ClearableAutoCompleteTextView et = ClearableAutoCompleteTextView.this;
        et.setText("");
    };

    private OnClearListener onClearListener = defaultClearListener;

    // The image we defined for the clear button
    public Drawable imgClearButton = getResources().getDrawable(R.drawable.ic_clear);

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s==null || s.toString().isEmpty()) hideClearButton();
        else showClearButton();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnClearListener {
        void onClear();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    void init() {
        // Set the bounds of the button

        // if the clear button is pressed, fire up the handler. Otherwise do nothing
        this.setOnTouchListener((v, event) -> {
            ClearableAutoCompleteTextView et = ClearableAutoCompleteTextView.this;

            if (et.getCompoundDrawables()[2] == null)
                return false;

            if (event.getAction() != MotionEvent.ACTION_UP)
                return false;

            if (event.getX() > et.getWidth() - et.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
                onClearListener.onClear();
                justCleared = true;
            }
            return false;
        });

        if (getText()!=null && !getText().toString().isEmpty()) showClearButton();

        addTextChangedListener(this);
    }

    public void setImgClearButton(Drawable imgClearButton) {
        this.imgClearButton = imgClearButton;
    }

    public void setOnClearListener(final OnClearListener clearListener) {
        this.onClearListener = clearListener;
    }

    public void hideClearButton() {
        this.setCompoundDrawables(null, null, null, null);
    }

    public void showClearButton() {
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null);
    }

}