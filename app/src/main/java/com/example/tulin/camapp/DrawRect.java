package com.example.tulin.camapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by tulin on 25.05.16.
 */
public class DrawRect extends View {
    private Paint mBorderLinePaint;
    private float mSelectionStart;
    public float mSelectionEnd;
    private float mHeight;
    private boolean mRectTrim = true;
    public int width;
    public int startPos;

    public DrawRect(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBorderLinePaint = new Paint();
        mBorderLinePaint.setAntiAlias(true);
        mBorderLinePaint.setStrokeWidth(4f);
        mBorderLinePaint.setColor(Color.BLACK);
        mBorderLinePaint.setAlpha(150);

  //      Resources r = getResources();
//        startPos = (int)Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));


    }

    public void setHeight(float height, int startPoss) {
        mHeight = (float)height;

        startPos = startPoss;
        // convert 60 dp to px
    //    mHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

        Log.d("frame height:" , String.valueOf(mHeight));
        Log.d("frame height float: " , String.valueOf(startPos));//String.valueOf(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics())));

    }



    public void setParameters(float start, float end, float offset, boolean rectType) {
        mSelectionStart = start;
        mSelectionEnd = end;
        mRectTrim = rectType;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw rect
        if (mRectTrim) {
            canvas.drawRect(
                    startPos, startPos,
                    mSelectionStart, mHeight,
                    mBorderLinePaint);
            canvas.drawRect(
                    mSelectionEnd, startPos,
                    width, mHeight,
                    mBorderLinePaint);
        } else {
            canvas.drawRect(
                    mSelectionStart, startPos,
                    mSelectionEnd, mHeight,
                    mBorderLinePaint);
        }


        Log.w("rect: "+String.valueOf(mSelectionStart), String.valueOf(mSelectionEnd)); //Height is matched as per thmbnail height
        Log.w("rect2: "+String.valueOf(startPos), String.valueOf(mHeight)); //Height is matched as per thmbnail height
      //  canvas.drawLine(mSelectionEnd, 0, mSelectionEnd, mHeight, mBorderLinePaint);
    }
}
