package com.example.tulin.camapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import java.io.File;


public class VideoEditing extends Activity {

    private VideoView videoView;

    private  TwoThumbsSeekbar<Integer> seekBar;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment. DIRECTORY_PICTURES), "MyCameraApp");
    //private String path = mediaStorageDir.getPath() + File.separator + "VID" + ".mp4";

    Boolean initialState = true;
    Boolean initSeekbar = true;
    Boolean videoIsBeingTouched = false;
    int currentMinPos, currentMaxPos, videoDuration, previewEndPos, previewStartPos, stopPos;
    CustomBordersVideoFrame borders;
    private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";
    Handler mHandler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView = (VideoView) findViewById(R.id.video_view);
         Button playButton = (Button) findViewById(R.id.play_button);
        Button pauseButton = (Button) findViewById(R.id.pause_button);
        borders = (CustomBordersVideoFrame) findViewById(R.id.thumbs_borders);

        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        //  videoView.setMediaController(mediaController);

        //specify the location of media file
        Uri uri = Uri.parse(path);

        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (time != null) {
            videoDuration = Integer.parseInt(time);
        }
        if (videoDuration != 0) {
            videoDuration /= 1000;
        }

        seekBar = new TwoThumbsSeekbar<Integer>(0, videoDuration, this);
        initSeekbar();

        videoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!videoIsBeingTouched) {
                    videoIsBeingTouched = true;
                    if (videoView.isPlaying()) {
                        Log.d("pase", "pse");
                        stopPos = videoView.getCurrentPosition();
                        videoView.pause();
                        //playbtn.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("res", "res");
                        videoView.seekTo(stopPos);
                        videoView.start();
                        //playbtn.setVisibility(View.INVISIBLE);
                        //videoView.resume(); <== resets everytime so replaced by above code
                    }
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Log.d("flg", "set false");
                            videoIsBeingTouched = false;
                            Log.d("touchflg", "set");
                        }
                    }, 200);
                }
                return true;
            }
        });


        int sampleNo = videoDuration/14;

        LinearLayout layout = (LinearLayout) findViewById(R.id.linear_images);

        for (int i = 0; i < videoDuration; i = i+sampleNo) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);
            Log.d("i", String.valueOf(i));


        //    linearLayout.setLayoutParams(new LayoutParams(65, 65));
          //  linearLayout.setGravity(Gravity.CENTER);

            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, 65, 65, true);

            imageView.setLayoutParams(new LayoutParams(65, 65));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);


            imageView.setImageBitmap(bm);
            layout.addView(imageView);

        }
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                videoView.start();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.pause();
            }
        });

    }

    private void initSeekbar() {

        //Handle range seekbar

        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new TwoThumbsSeekbar.OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(TwoThumbsSeekbar<?> bar, Integer minValue, Integer maxValue) {

                //handle start stage
                if (initialState && minValue != 0) {
                    initialState = false;
                    currentMinPos = minValue;
                    videoView.seekTo(currentMinPos * 1000000);
                    Log.d("setminInitial",String.valueOf(currentMinPos));
                }
                else if (initialState && maxValue != (videoDuration)){
                    initialState = false;
                    currentMaxPos = maxValue;
                    videoView.seekTo(currentMaxPos * 1000000);
                    Log.d("setmaxInitial",String.valueOf(currentMaxPos));
                }

                //Handle current stage
                if (currentMaxPos == maxValue){
                    currentMinPos = minValue;
                    videoView.seekTo(currentMinPos * 1000000);
                    Log.d("setmin",String.valueOf(currentMinPos));
                }
                else if (currentMinPos == minValue ){
                    currentMaxPos = maxValue;
                    videoView.seekTo(currentMaxPos * 1000000);
                    Log.d("setmax",String.valueOf(currentMaxPos));
                }
                Log.d("seekbar1", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);

                //Moveborders on thmbnails
                borders.setParameters((Scaler(currentMinPos)*4.85f),(Scaler(currentMaxPos)*4.85f),0);
                borders.invalidate();

                //Set last End Pos so to stop at that time
                previewEndPos = maxValue;
                //Same for initial start time
                previewStartPos = minValue;

            }
        });

        ViewGroup viewGroup = (ViewGroup)findViewById(R.id.range_seekbar);
        if (initSeekbar) {
            viewGroup.addView(seekBar);
            initSeekbar = false;
        }
        else{
            viewGroup.removeAllViews();
            viewGroup.addView(seekBar);
        }

    }

    private float Scaler(int val_to_scale){

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

        float min = 0, max = videoDuration, a = 0, b = 200, x = val_to_scale;

        float scaled_val = (((b-a)*(x-min)) / (max-min))+a;
        Log.d("Scaler",String.valueOf(scaled_val));
        return scaled_val;
    }

}

