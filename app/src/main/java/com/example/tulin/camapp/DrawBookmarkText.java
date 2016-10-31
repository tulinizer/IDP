package com.example.tulin.camapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by tulin on 27.06.16.
 */
public class DrawBookmarkText extends ImageView {
    private float x1, y2;
    private Paint mBorderLinePaint, mPaint;
    Bitmap mDrawBitmap;
    Canvas mBitmapCanvas;
    int mHeight;
    String mTag = " ";

        public DrawBookmarkText(Context context, AttributeSet attrs) {
            super(context, attrs);

            mBorderLinePaint = new Paint();
            mBorderLinePaint.setAntiAlias(true);
            mBorderLinePaint.setStrokeWidth(4f);
            mBorderLinePaint.setColor(Color.RED);

            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(16);
        }

        public void setParameters(float start1, float end1, float start2, float end2, String tag) {
            x1 = start1;
            y2 = end2;

            mTag = mTag.replaceAll(".", "");
            mTag = mTag.concat(tag);
      //      mTime = time;


        }

        public void setHeight(int height) {
            mHeight = height;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (mDrawBitmap == null) {
                Log.d("Drawing..."+String.valueOf(this.getWidth()), String.valueOf(y2));
                mDrawBitmap = Bitmap.createBitmap(this.getWidth(), mHeight+40, Bitmap.Config.ARGB_8888);
                mBitmapCanvas = new Canvas(mDrawBitmap);
            }

            Log.d("Drawing to canvas..."+String.valueOf(x1), mTag);

            if (mTag.length() > 6) {
                mBitmapCanvas.drawText(mTag.substring(0,5)+"...", x1-25, 15, mPaint);
            } else if ( mTag.length() < 5) {
                mBitmapCanvas.drawText(mTag, x1 - 15, 15, mPaint);
            } else
                mBitmapCanvas.drawText(mTag, x1 - 20, 15, mPaint);

            canvas.drawBitmap(mDrawBitmap, 0, 0, mBorderLinePaint);


        }



}

