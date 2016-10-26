package com.example.tulin.camapp;

import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.app.ProgressDialog;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import com.example.tulin.camapp.DrawRect;

import java.io.File;
import java.util.ArrayList;

public class VideoEditingTT extends AppCompatActivity {

    private VideoView videoView;

    private  TwoThumbsSeekbar<Double> TTSeekbar;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;
    private Uri fileUri = Uri.parse(path.toString());

    public MediaController mediaController;
    boolean initialState = true;
    boolean initSeekbar = true;
    double videoDuration, stopPos;
    double currentMinPos, currentMaxPos, previewEndPos, previewStartPos;
    double durationDouble;
    long durationLong = 1L;
    int padding = 65, frameHeight = 100, seekbarWidth;
    int HEIGHT= 100;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    Bitmap icon;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin, textViewMax;
    Button apply;
    boolean playing = true;
    DrawRect rect;
    boolean rectTrim = true;
    boolean bookmarkAdded, applyTrim = false;

    float textViewPadding;

    public ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button cutButton = (Button) findViewById(R.id.cut_button);
        final Button trimButton = (Button) findViewById(R.id.trim_button);
        rect = (DrawRect) findViewById(R.id.rect);

        trimButton.setPressed(true);
        trimButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        playButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);

        apply = (Button) findViewById(R.id.apply);
        textViewMin = (TextView) findViewById(R.id.timestampMin);
        textViewMax = (TextView) findViewById(R.id.timestampMax);
        textViewMin.bringToFront();
        textViewMax.bringToFront();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Bitmap Left_thumbImage_nrml = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_norm);
        textViewPadding = 0.5f *  Left_thumbImage_nrml.getWidth();

        // get screen width in pixels
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidthPx = size.x;

        // Convert padding to px from dp
        Resources r = getResources();
        float padd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, r.getDisplayMetrics());





        seekbarWidth =  screenWidthPx - (int)(padd);



        frameHeight =  (int) (size.y)/6; //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT, r.getDisplayMetrics());


        rect.setHeight(frameHeight, 0);

    //    seekbarWidth = screenWidthPx - (padd);//frameHeight*TOTALFRAME;
        Log.d("seebar width: ", String.valueOf(seekbarWidth));

        rect.width = seekbarWidth;
        rect.mSelectionEnd = seekbarWidth;

        rect.setParameters((float) (seekbarWidth * 0.1), ((float) (seekbarWidth * 0.9)), 0, rectTrim);
        rect.invalidate();


        //specify the location of media file
        final Uri uri = Uri.parse(path);
        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        mediaController = new MediaController(this);

        videoView.setMediaController(new MediaController(this) {
            @Override
            public void hide()
            {
                mediaController.show();
            }

        });

        mediaController.setAlpha(0.9f);

        //  videoView.setMediaController(mediaController);

        //mediaController.setAnchorView(videoView);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
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

        previewStartPos = videoDuration * 0.1;
        previewEndPos = videoDuration * 0.9;
        Log.d(String.valueOf(previewStartPos), String.valueOf(previewEndPos));
        CalculateFrameSize(seekbarWidth);



        layout = (LinearLayout) findViewById(R.id.linear_images);
    //    frameList = new ArrayList<Bitmap>();

        durationDouble = Double.parseDouble(time) * 1000;

        double frameTime = 0.;
        double frameFreq = durationDouble/TOTALFRAME;

    //    frameHeight = layout.getHeight();




        for (int i = 0; i <= TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);

            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameHeight, frameHeight, true);

            frameTime += frameFreq;

            imageView.setLayoutParams(new LayoutParams(frameHeight, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }


        Log.d("fHeight: ", String.valueOf(frameHeight));

    //    ImageView im = (ImageView) findViewById(R.id.movie_clip);
    //    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)seekbarWidth, frameHeight);
    //    im.setLayoutParams(layoutParams);
    //    im.setLayoutParams(new RelativeLayout.LayoutParams(screenWidthPx-100, 500));
     //   im.setPadding(0,0,5,10);


        videoView.start();

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

        trimButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                videoView.pause();
                apply.setText("Apply Trim");
                view.setPressed(true);
                trimButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                cutButton.setPressed(false);
                cutButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
                rectTrim = true;
                rect.setParameters((float) RectParameters(previewStartPos), ((float) RectParameters(previewEndPos)), 0, rectTrim);
                rect.invalidate();

            }
        });

        cutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.pause();
                apply.setText("Apply Cut");
                view.setPressed(true);
                cutButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);

                trimButton.setPressed(false);
                trimButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
                rectTrim = false;
                rect.setParameters((float) RectParameters(previewStartPos),((float) RectParameters(previewEndPos)),0, rectTrim);
                rect.invalidate();

            }
        });

        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                videoView.pause();

                if (apply.getText().equals("Apply Trim"))
                    applyTrim = true;
                else
                    applyTrim = false;

                new ProgressTask().execute();

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

       /* MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        */
        // Configure the search info and add any event listeners...

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_next: {
                Intent intent = new Intent();

                intent.setClass(VideoEditingTT.this, AddBookmarkActivity.class);
                startActivity(intent);
            }

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VideoEditingTT.this, MainActivity.class);
        startActivity(intent);
    }

    private class ProgressTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute(){

            super.onPreExecute();

            progressDialog = new ProgressDialog(VideoEditingTT.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Loading");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            long segmentFrom = (long)(previewStartPos * 1000000);
            long segmentTo = (long)(previewEndPos * 1000000);

            if (applyTrim) {
                ComposerTranscodeCore cT = new ComposerTranscodeCore(VideoEditingTT.this, progressDialog);
                cT.getActivityInputs(fileName, path, fileUri.toString(), segmentFrom, segmentTo);
                cT.addSegment(segmentFrom, segmentTo);

            } else {
                ComposerTranscodeCore cT = new ComposerTranscodeCore(VideoEditingTT.this, progressDialog);
                cT.getActivityInputs(fileName, path, fileUri.toString(), segmentFrom, segmentTo);
                cT.addSegment(0L, segmentFrom);
                cT.addSegment(segmentTo, durationLong);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d("exec", "done..");
        }
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

                rect.setParameters((float) RectParameters(minValue),((float) RectParameters(maxValue)),0, rectTrim);
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
                    Log.d("setmin", String.valueOf((int)(currentMinPos*1000)));
                } else if (currentMinPos == minValue) {
                    currentMaxPos = maxValue;
                    videoView.seekTo((int) (currentMaxPos * 1000));
                    Log.d("setmax", String.valueOf((int)(currentMaxPos*1000)));
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

    }

    private void CalculateFrameSize(int width) {

        TOTALFRAME = (width / frameHeight);
        Log.d("TOTAL FRAME", String.valueOf(TOTALFRAME));

       // frameHeight = frameWidth;
    }
}
