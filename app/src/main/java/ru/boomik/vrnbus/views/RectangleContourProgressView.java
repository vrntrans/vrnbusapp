package ru.boomik.vrnbus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by boomv on 27.08.2015.
 */
public class RectangleContourProgressView extends View {
    private Offsetter mOffsetter;
    private float mThickness;
    private float mProgressLength;
    private Paint mPaintProgress;
    private int mDuration = 2000;
    private boolean mAnimated;

    public RectangleContourProgressView(Context context) {
        super(context);
        init(context);
    }

    public RectangleContourProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RectangleContourProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mOffsetter = new Offsetter(new LinearInterpolator());

        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(30);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setFakeBoldText(true);

        mPaintProgress = new Paint();
        mPaintProgress.setColor(Color.WHITE);
        mPaintProgress.setStyle(Paint.Style.FILL);
        float mDensity = context.getResources().getDisplayMetrics().density;
        mThickness = (2 * mDensity);
        mProgressLength = (160 * mDensity);
        int mStartColor = Color.BLUE;//0xFFFFFFFF;
        int mEndColor = Color.TRANSPARENT;

        LinearGradient mGradientForPaint = new LinearGradient(0, 0, mProgressLength, mThickness, mEndColor, mStartColor, Shader.TileMode.CLAMP);
        mPaintProgress.setShader(mGradientForPaint);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mAnimated) return;
        canvas.save();
        int width = getWidth();
        int height = getHeight();

        if (mOffsetter.computeValue()) {
            int offset = mOffsetter.getCurrValue();

            if (offset <= width) { //top
                canvas.translate(offset, 0);
                canvas.drawRect(0, 0, mProgressLength, mThickness, mPaintProgress);
            }
            if (offset >= width - mProgressLength && offset <= width + height) {//right
                canvas.restore();
                canvas.save();
                int newOffset = offset - width;
                canvas.translate(width, newOffset);
                canvas.rotate(90);
                canvas.drawRect(0, 0, mProgressLength, mThickness, mPaintProgress);
            }
            if (offset >= width + height - mProgressLength && offset <= width * 2 + height) {//bottom
                canvas.restore();
                canvas.save();
                int newOffset = (int) (offset - width - height);
                canvas.translate(width - newOffset, height);
                canvas.rotate(180);
                canvas.drawRect(0, 0, mProgressLength, mThickness, mPaintProgress);
            }
            if (offset >= width * 2 + height - mProgressLength && offset <= width * 2 + height * 2) {//left
                canvas.restore();
                canvas.save();
                int newOffset = offset - width * 2 - height;
                canvas.translate(0, height - newOffset);
                canvas.rotate(270);
                canvas.drawRect(0, 0, mProgressLength, mThickness, mPaintProgress);
            }
            if (offset >= width * 2 + height * 2 - mProgressLength) {
                canvas.restore();
                canvas.save();
                int newOffset = offset - width * 2 - height * 2;
                canvas.translate(newOffset, 0);
                canvas.drawRect(0, 0, mProgressLength, mThickness, mPaintProgress);
            }

            canvas.restore();
            invalidate();
        } else {
            mOffsetter.startChange(0, width * 2 + height * 2, mDuration);
            invalidate();
        }
    }

    public void startAnimate() {
        mAnimated = true;

        int width = getWidth();
        int height = getHeight();
        mOffsetter.startChange(0, width * 2 + height * 2, mDuration);
        invalidate();
    }

    public void stopAnimate() {
        mAnimated=false;
    }


    class Offsetter {
        long startTime = -1;
        long endTime = -1;
        int startValue;
        int endValue;
        int duration;
        int deltaValue;
        Interpolator interpolator;
        private int currValue;
        private float deltaTime;

        Offsetter(Interpolator interpolator) {
            this.interpolator = interpolator;
        }

        boolean IsFinished() {
            return endTime == -1 || endTime < SystemClock.elapsedRealtime();
        }


        boolean computeValue() {
            boolean finish = IsFinished();
            if (!finish) {
                deltaTime = (float) (endTime - SystemClock.elapsedRealtime()) / (float) duration;
                deltaTime = 1 - deltaTime;
                deltaTime = interpolator.getInterpolation(deltaTime);
                currValue = (int) (deltaValue * deltaTime + startValue);
            } else {
                currValue = endValue;
            }

            return !finish;
        }

        void startChange(int start, int end, int duration) {
            startTime = SystemClock.elapsedRealtime();
            endTime = startTime + duration;
            startValue = start;
            endValue = end;
            deltaValue = (endValue - startValue);
            this.duration = duration;
            currValue = startValue;
        }

        void forceFinished() {
            endTime = -1;
            currValue = endValue;
        }

        int getCurrValue() {
            return currValue;
        }
    }
}
