package com.example.tulin.camapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * Created by tulin on 04.07.16.
 */
public class DrawBookmarkTextPlayback extends ImageView {
    private Paint mBorderLinePaint, mPaint;
    Bitmap mDrawBitmap;
    Canvas mBitmapCanvas;
    int mHeight;
    String mTag = " ";

    HashMap<String, Float> mLines;

    public DrawBookmarkTextPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBorderLinePaint = new Paint();
        mBorderLinePaint.setAntiAlias(true);
        mBorderLinePaint.setStrokeWidth(4f);
        mBorderLinePaint.setColor(Color.RED);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(16);

        mLines = new HashMap<>();
    }

    public void setParameters(HashMap<String, Float> lines, int height) {
        mLines = lines;
        mHeight = height;

    }

    public void setHeight(int height) {
        mHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (String s : mLines.keySet()) {
            canvas.drawText(s, mLines.get(s), 20, mPaint);

        }

    }

}
