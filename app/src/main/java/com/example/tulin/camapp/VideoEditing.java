package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.util.ArrayList;


public class VideoEditing extends Activity implements SeekBar.OnSeekBarChangeListener {

    private VideoView videoView;


    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment. DIRECTORY_PICTURES), "MyCameraApp");
    private String path = mediaStorageDir.getPath() + File.separator + "VID" + ".mp4";

   // private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";

    int videoDuration;
    int currentMinPos, currentMaxPos = 1;
    Handler mHandler=new Handler();
    private Context context;
    private SeekBar videoProgressBar;
    private Utilities utils;
    int sampleFreq = 1;
    ArrayList<Bitmap> frameList;
    Bitmap icon;
    LinearLayout layout;
    int TOTALFRAME = 14;
    int padding = 25, frameWidth = 10, frameHeight = 10;
    HorizontalScrollView scrollView;
    TextView textView;
    int seekbarWidth;
    int iconWidth = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        context = this;

        videoView = (VideoView) findViewById(R.id.video_view);
        Button playButton = (Button) findViewById(R.id.play_button);
        Button pauseButton = (Button) findViewById(R.id.pause_button);
     //   videoProgressBar = (SeekBar) findViewById(R.id.seekBar);
        Button splitButton = (Button) findViewById(R.id.split_button);

      //  textView = (TextView) findViewById(R.id.timestamp);
        textView.bringToFront();
        scrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scroll);

        icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.music2);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        utils = new Utilities();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int dp = size.x / (int)getResources().getDisplayMetrics().density;

        Resources r = getResources();
        float padd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, r.getDisplayMetrics());

        seekbarWidth = dp - (padding*2);

        Log.d(String.valueOf(seekbarWidth), "width");

        // Listener
        videoProgressBar.setOnSeekBarChangeListener(this);

        //specify the location of media file
        Uri uri = Uri.parse(path);

        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        double durationLong = 1L;
        if (time != null) {
            videoDuration = Integer.parseInt(time);
            durationLong = Long.parseLong(time) * 1000;
        }
        if (videoDuration != 0) {
            videoDuration /= 1000;
        }

        CalculateFrameSize(seekbarWidth);

        layout = (LinearLayout) findViewById(R.id.linear_images);
        frameList = new ArrayList<Bitmap>();

        Log.d("frame width: ", String.valueOf(frameWidth));

        double frameTime = 0.;
        double d = durationLong/(double)TOTALFRAME;

        for (int i = 0; i < TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);

            Log.d("i", String.valueOf(i));
            Log.d("duration", String.valueOf(videoDuration));


            Log.d("time frame", String.valueOf(frameTime));
            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameWidth, frameHeight, true);

            frameTime += d;

            frameList.add(bm);

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            imageView.setLayoutParams(new LayoutParams(frameWidth, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }
        

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                videoView.start();
                updateProgressBar();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.pause();
            }
        });

        splitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                videoView.pause();
                currentMaxPos =  videoView.getCurrentPosition()/1000;

                new SplitVideo(context, path, currentMinPos, currentMaxPos);
                Log.d("Split min: " + String.valueOf(currentMinPos), "Split max:" + String.valueOf(currentMaxPos));

                layout.removeAllViews();

           //     seekbarWidth -= iconWidth;
            //    CalculateFrameSize(seekbarWidth);

                int replaceId = currentMaxPos / sampleFreq;
                Log.d(String.valueOf(currentMaxPos), String.valueOf(sampleFreq));
                // frames.put(replaceId, icon);
       //         frameList.remove(replaceId);
               // frameList.add(replaceId, icon);
                Log.d(String.valueOf(frameWidth), "frame size");


                currentMinPos = currentMaxPos;

                RelativeLayout iconLayout = (RelativeLayout) findViewById(R.id.iconView);

                ImageView iconView = new ImageView(getApplicationContext());
                int progress = (int)(utils.getProgressPercentage(videoView.getCurrentPosition(), videoView.getDuration()));
                int val = (progress * (videoProgressBar.getWidth() - 2 * videoProgressBar.getThumbOffset())) / videoProgressBar.getMax();
                iconView.setImageBitmap(icon);

                int thumbPos = (int) val + videoProgressBar.getThumbOffset() / 2;

                //  iconView.setY(306);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iconWidth, frameHeight);
                params.leftMargin = thumbPos;
                iconView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iconLayout.addView(iconView, params);

//                layout.addView(iconView);
                //    iconView.setX(videoProgressBar.getX() + val + videoProgressBar.getThumbOffset() / 2);

                String f = Float.toString(videoProgressBar.getX() + val + videoProgressBar.getThumbOffset() / 2);
                Log.d(f,String.valueOf(videoProgressBar.getX()));
                iconView.bringToFront();

                for (int i = 0; i < frameList.size(); i++) {
                    ImageView imageView = new ImageView(getApplicationContext());

                    if (frameList.get(i).equals(icon)) {
                        ;//       imageView.setLayoutParams(new LayoutParams(iconWidth, frameHeight));

                    } else {
                        imageView.setLayoutParams(new LayoutParams(frameWidth, frameHeight));
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    imageView.setImageBitmap(frameList.get(i));
                    layout.addView(imageView);
                }

            }
        });

    }


     // Update timer on seekbar

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 10);
    }

     // Background Runnable thread
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = videoView.getDuration();
            long currentDuration = videoView.getCurrentPosition();

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            videoProgressBar.setProgress(progress);
         //   videoProgressBar2.setProgress(progress);

            // Running this thread after 10 milliseconds
            mHandler.postDelayed(this, 10);
        }
    };

    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        textView.invalidate();

        int val = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
        textView.setText("" + videoView.getCurrentPosition() / 1000);
        textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
        textView.bringToFront();
    }


     // When user starts moving the progress handler

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }


     // When user stops moving the progress hanlder

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = videoView.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        videoView.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }


    private void CalculateFrameSize(int width) {
        // Double d  = Math.ceil(videoDuration / TOTALFRAME);
       // sampleFreq = videoDuration/10;
       // sampleFreq /= 2;

        sampleFreq = videoDuration / TOTALFRAME;
        if (sampleFreq < 1)
            sampleFreq = 1;

        frameWidth = width / TOTALFRAME;
        frameHeight = frameWidth;
    }

}

