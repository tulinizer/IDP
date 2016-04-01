package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.SeekBar;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class VideoEditing extends Activity implements  SeekBar.OnSeekBarChangeListener {

    private VideoView videoView;

   // private  TwoThumbsSeekbar<Integer> seekBar;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment. DIRECTORY_PICTURES), "MyCameraApp");
  //  private String path = mediaStorageDir.getPath() + File.separator + "VID" + ".mp4";

    Boolean initialState = true;
    Boolean initSeekbar = true;
    Boolean videoIsBeingTouched = false;
    int videoDuration;
    int currentMinPos, currentMaxPos = 1;
    CustomBordersVideoFrame borders;
    private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";
    Handler mHandler=new Handler();
    private Context context;
   // private MediaPlayer mp;
    private SeekBar videoProgressBar;
    private Utilities utils;
    int sampleFreq = 1;
    HashMap<Integer, Bitmap> frames;
    ArrayList<Bitmap> frameList;
    Bitmap icon;
    LinearLayout layout;
    int TOTALFRAME = 14;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        context = this;

        videoView = (VideoView) findViewById(R.id.video_view);
        Button playButton = (Button) findViewById(R.id.play_button);
        Button pauseButton = (Button) findViewById(R.id.pause_button);
     //   borders = (CustomBordersVideoFrame) findViewById(R.id.thumbs_borders);
        videoProgressBar = (SeekBar) findViewById(R.id.seekBar);
        Button splitButton = (Button) findViewById(R.id.split_button);
        videoProgressBar.bringToFront();

        icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.music2);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        utils = new Utilities();

        // Listeners
        videoProgressBar.setOnSeekBarChangeListener(this); // Important

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



        if (videoDuration < TOTALFRAME) {
            // TODO
        } else
            sampleFreq = videoDuration/TOTALFRAME;

        layout = (LinearLayout) findViewById(R.id.linear_images);
        frames = new HashMap<Integer, Bitmap>();
        frameList = new ArrayList<Bitmap>();

        int k = 0;
        for (int i = 0; i < videoDuration; i = i+sampleFreq) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);
            Log.d("i", String.valueOf(i));
            Log.d("duration", String.valueOf(videoDuration));
            Log.d("sampleno", String.valueOf(sampleFreq));

        //    linearLayout.setLayoutParams(new LayoutParams(65, 65));
          //  linearLayout.setGravity(Gravity.CENTER);

            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, 65, 65, true);

            frames.put(k, bm);
            frameList.add(bm);
            k++;

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
                currentMaxPos =  videoView.getCurrentPosition()/1000;
                new SplitVideo(context, path, currentMinPos, currentMaxPos);
                Log.d("Split min: " +String.valueOf(currentMinPos),"Split max:" + String.valueOf(currentMaxPos));
                currentMinPos = currentMaxPos;

                layout.removeAllViews();

                int replaceId = currentMaxPos / sampleFreq;
               // frames.put(replaceId, icon);
                frameList.add(replaceId, icon);
                Log.d(String.valueOf(frameList.size()), "size");

                for (int i = 0; i < frameList.size(); i++) {
                    ImageView imageView = new ImageView(getApplicationContext());

                    if (frameList.get(i).equals(icon)) {
             //           imageView.setLayoutParams(new LayoutParams(20, 20));
                    } else {
                        imageView.setLayoutParams(new LayoutParams(65, 65));
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    imageView.setImageBitmap(frameList.get(i));
                    layout.addView(imageView);
                }

            }
        });

    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 10);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = videoView.getDuration();
            long currentDuration = videoView.getCurrentPosition();

            // Displaying Total Duration time
          //  songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            //songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            videoProgressBar.setProgress(progress);

            // Running this thread after 10 milliseconds
            mHandler.postDelayed(this, 10);
        }
    };

    /**
     *
     * */
    private int smoothnessFactor = 10;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
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


   /* private void initSeekbar() {

        //Handle range seekbar

        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new TwoThumbsSeekbar.OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(TwoThumbsSeekbar<?> bar, Integer minValue, Integer maxValue) {

                //handle start stage
                if (initialState && minValue != 0) {
                    initialState = false;
                    currentMinPos = minValue;
                    videoView.seekTo(currentMinPos * 1000);
                    Log.d("setminInitial", String.valueOf(currentMinPos));
                } else if (initialState && maxValue != (videoDuration)) {
                    initialState = false;
                    currentMaxPos = maxValue;
                    videoView.seekTo(currentMaxPos * 1000);
                    Log.d("setmaxInitial", String.valueOf(currentMaxPos));
                }

                //Handle current stage
                if (currentMaxPos == maxValue) {
                    currentMinPos = minValue;
                    videoView.seekTo(currentMinPos * 1000);
                    Log.d("setmin", String.valueOf(currentMinPos));
                } else if (currentMinPos == minValue) {
                    currentMaxPos = maxValue;
                    //   videoView.seekTo(currentMaxPos * 1000);
                    Log.d("setmax", String.valueOf(currentMaxPos));
                }
                Log.d("seekbar1", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);

                //Moveborders on thmbnails
                borders.setParameters((Scaler(currentMinPos) * 4.85f), (Scaler(currentMaxPos) * 4.85f), 0);
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
*/


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

