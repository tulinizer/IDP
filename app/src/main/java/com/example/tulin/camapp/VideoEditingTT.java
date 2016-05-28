package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.util.ArrayList;


public class VideoEditingTT extends Activity {

    private VideoView videoView;

    private  TwoThumbsSeekbar<Double> TTSeekbar;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;
    private Uri fileUri = Uri.parse(path.toString());

    boolean initialState = true;
    boolean initSeekbar = true;
    boolean videoIsBeingTouched = false;
    double videoDuration, stopPos;
    double currentMinPos, currentMaxPos,  previewEndPos, previewStartPos;
    double durationDouble;
    CustomBordersVideoFrame borders;
    // private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";
    Handler mHandler=new Handler();
    int padding = 22, frameWidth = 10, frameHeight = 10, seekbarWidth;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    Bitmap icon;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin, textViewMax;
    boolean playing = false;
    DrawRect rect;

    float textViewPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        Button pauseButton = (Button) findViewById(R.id.pause_button);
        Button splitButton = (Button) findViewById(R.id.split_button);
        rect = (DrawRect) findViewById(R.id.rect);

        textViewMin = (TextView) findViewById(R.id.timestampMin);
        textViewMax = (TextView) findViewById(R.id.timestampMax);
        textViewMin.bringToFront();
        textViewMax.bringToFront();

        Bitmap Left_thumbImage_nrml = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_norm);
        textViewPadding = 0.5f *  Left_thumbImage_nrml.getWidth();

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // get screen width in pixels
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidthPx = size.x;

        // Convert padding to px from dp
        Resources r = getResources();
        float padd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, r.getDisplayMetrics());

        seekbarWidth = screenWidthPx - (int)(2 * padd);
        Log.d(String.valueOf(seekbarWidth), "seekbar width px");
        Log.d(String.valueOf(textViewPadding), "padding in px");

        //specify the location of media file
        final Uri uri = Uri.parse(path);
        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long durationLong = 1L;
        if (time != null) {
            videoDuration = Integer.parseInt(time);
            durationLong = Long.parseLong(time) * 1000;
        }
        if (videoDuration != 0) {
            videoDuration /= 1000;
        }


        TTSeekbar = new TwoThumbsSeekbar<Double>(0., videoDuration, this);

        Log.d("duration long ", String.valueOf(durationLong));
        initSeekbar();

        CalculateFrameSize(seekbarWidth);
        rect.setHeight(frameHeight);
        rect.width = seekbarWidth;
        rect.mSelectionEnd = seekbarWidth;

        layout = (LinearLayout) findViewById(R.id.linear_images);
        frameList = new ArrayList<Bitmap>();

        Log.d("frame width: ", String.valueOf(frameWidth));

        durationDouble = Double.parseDouble(time) * 1000;

        double frameTime = 0.;
        double frameFreq = durationDouble/TOTALFRAME;

         Log.d("frame freq", String.valueOf(frameFreq));

        for (int i = 0; i < TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);


            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameWidth, frameHeight, true);

            frameTime += frameFreq;
            Log.d("frame Time", String.valueOf(frameTime));

            //        frameList.add(bm);
            imageView.setLayoutParams(new LayoutParams(frameWidth, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }


        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (playing) {
                    videoView.pause();
                    playButton.setText("Play");
                    playing = false;
                } else {
                    videoView.start();
                    playButton.setText("Pause");
                    playing = true;
                }
            }
        });


        splitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.pause();
                playButton.setText("Play");
                playing = false;

                long segmentFrom = (long)(previewStartPos * 1000000);
                long segmentTo = (long)(previewEndPos * 1000000);

                Intent intent = new Intent();
                intent.setClass(VideoEditingTT.this, ComposerCutCoreActivity.class);

                Bundle b = new Bundle();
                b.putString("srcMediaName1", fileName);
                intent.putExtras(b);
                b.putString("dstMediaPath", path/*mediaStorageDir+"/segment.mp4"*/);
                intent.putExtras(b);
                b.putLong("segmentFrom", segmentFrom);
                intent.putExtras(b);
                b.putLong("segmentTo", segmentTo);
                intent.putExtras(b);
                b.putString("srcUri1", fileUri.toString());
                intent.putExtras(b);

                startActivity(intent);

            }
        });

    }

    private void initSeekbar() {

        //Handle range seekbar

        TTSeekbar.setNotifyWhileDragging(true);
        TTSeekbar.setOnRangeSeekBarChangeListener(new TwoThumbsSeekbar.OnRangeSeekBarChangeListener<Double>() {

            @Override
            public void onRangeSeekBarValuesChanged(TwoThumbsSeekbar<?> bar, Double minValue, Double maxValue) {

                textViewMin.invalidate();
                textViewMax.invalidate();

            //    int val = (minValue * (seekbarWidth - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                textViewMin.setText("" + String.format("%.2f", minValue));
                textViewMax.setText("" + String.format("%.2f", maxValue));
                textViewMin.setX((float) Scaler(minValue));
                textViewMax.setX((float) Scaler(maxValue));
                Log.d("setx location", String.valueOf(Scaler(minValue)));
                Log.d("min value", String.valueOf(minValue));

                rect.setParameters((float) RectParameters(minValue),((float) RectParameters(maxValue)),0);
                rect.invalidate();


                //handle start stage
                if (initialState && minValue != 0) {
                    initialState = false;
                    currentMinPos = minValue;
                    videoView.seekTo((int) (currentMinPos * 1000));
                    Log.d("setminInitial", String.valueOf(currentMinPos));
                } else if (initialState && maxValue != (videoDuration)) {
                    initialState = false;
                    currentMaxPos = maxValue;
                    videoView.seekTo((int) (currentMaxPos * 1000));
                    Log.d("setmaxInitial", String.valueOf(currentMaxPos));
                }

                //Handle current stage
                if (currentMaxPos == maxValue) {
                    currentMinPos = minValue;
                    videoView.seekTo((int) (currentMinPos * 1000));
                    Log.d("setmin", String.valueOf(currentMinPos));
                } else if (currentMinPos == minValue) {
                    currentMaxPos = maxValue;
                    videoView.seekTo((int) (currentMaxPos * 1000));
                    Log.d("setmax", String.valueOf(currentMaxPos));
                }
                Log.d("seekbar1", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);



                //Set last End Pos so to stop at that time
                previewEndPos = maxValue;
                //Same for initial start time
                previewStartPos = minValue;

            }
        });

        ViewGroup viewGroup = (ViewGroup)findViewById(R.id.range_seekbar);
        viewGroup.bringToFront();



        if (initSeekbar) {
            viewGroup.addView(TTSeekbar);
            initSeekbar = false;
        }
        else{
            viewGroup.removeAllViews();
            viewGroup.addView(TTSeekbar);
        }

    }

    private double RectParameters(double val) {

        return (val/videoDuration) * (seekbarWidth);
    }

    private double Scaler(double val_to_scale){

        //Method to scale down values in range 0-200
        /**
         * Formula used (b-a)(x-min) + a
         * 				------------
         * 				 max - min
         *
         * where [min,max] is range to scale and [a,b] is [0,200]
         * and x is current value to scale.
         *
         * */

       return ((padding - textViewPadding) + ((val_to_scale/videoDuration) * (seekbarWidth)));

/*        float min = 0, max = videoDuration, a = 0, b = 200, x = val_to_scale;

        float scaled_val = (((b-a)*(x-min)) / (max-min))+a;
        Log.d("Scaler", String.valueOf(scaled_val));
        return scaled_val;
  */
    }

    private void CalculateFrameSize(int width) {
         Double d  = Math.ceil((double)width / (double)TOTALFRAME);
      //   sampleFreq = videoDuration/10;
       //  sampleFreq /= 2;

       // frameWidth = width / TOTALFRAME;
        frameWidth = d.intValue();

        frameHeight = frameWidth;
    }
}
