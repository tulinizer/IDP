package com.example.tulin.camapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by tulin on 25.05.16.
 */
public class DrawRect extends View {
    private Paint mBorderLinePaint;
    private float mOffset;
    private float mSelectionStart;
    public float mSelectionEnd;
    private float mHeight;
    public int width;

    public DrawRect(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBorderLinePaint = new Paint();
        mBorderLinePaint.setAntiAlias(true);
        mBorderLinePaint.setStrokeWidth(4f);
        mBorderLinePaint.setColor(Color.BLACK);
        mBorderLinePaint.setAlpha(125);

    }

    public void setHeight(float height) {
        mHeight = (float)height;
    }


    public void setParameters(float start, float end, float offset) {
        mSelectionStart = start;
        mSelectionEnd = end;
        mOffset = offset;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw rect
        canvas.drawRect(
                0, 0,
                mSelectionStart , mHeight,
                mBorderLinePaint);
        canvas.drawRect(
                mSelectionEnd, 0,
                width, mHeight,
                mBorderLinePaint);


        //Log.w("rect", String.valueOf(width)); //Height is matched as per thmbnail height
      //  canvas.drawLine(mSelectionEnd, 0, mSelectionEnd, mHeight, mBorderLinePaint);
    }
}
