package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class VideoEditingTT extends Activity {

    private VideoView videoView;

    private  TwoThumbsSeekbar<Double> TTSeekbar;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;
    //private String path = mediaStorageDir.getPath() + File.separator  + "The Simpsons Movie - Trailer.mp4";
   //   private String path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() +"/capture.mp4";
    private Uri fileUri = Uri.parse(path.toString());

    boolean initialState = true;
    boolean initSeekbar = true;
    double videoDuration, stopPos;
    double currentMinPos, currentMaxPos, previewEndPos, previewStartPos;
    double durationDouble;
    long durationLong = 1L;
    CustomBordersVideoFrame borders;
    // private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";
    Handler mHandler=new Handler();
    int padding = 22, frameWidth = 10, frameHeight = 10, seekbarWidth;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    Bitmap icon;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin, textViewMax, textViewTs;
    Button apply;
    boolean playing = true;
    DrawRect rect;
    boolean rectTrim = true;
    boolean bookmarkAdded = false;

    float textViewPadding;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button cutButton = (Button) findViewById(R.id.cut_button);
        final Button trimButton = (Button) findViewById(R.id.trim_button);
        final Button bookmarkButton = (Button) findViewById(R.id.bookmark_button);
        textViewTs = (TextView) findViewById(R.id.textView_ts);
        final EditText edittext = (EditText) findViewById(R.id.edittext);
     //   final Button addBookmark = (Button) findViewById(R.id.add_bookmark);
        rect = (DrawRect) findViewById(R.id.rect);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);

        // preparing list data

        //prepareListData();


        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader.add("Bookmarks");
        List<String> l = new ArrayList<String>();
        l.add("");

        listDataChild.put("Bookmarks", l);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        trimButton.setPressed(true);
       // trimButton.setBackgroundColor(Color.DKGRAY);
        trimButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        playButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
      //  playButton.setBackgroundColor(Color.LTGRAY);

        apply = (Button) findViewById(R.id.apply);
        textViewMin = (TextView) findViewById(R.id.timestampMin);
        textViewMax = (TextView) findViewById(R.id.timestampMax);
        textViewMin.bringToFront();
        textViewMax.bringToFront();

        Bitmap Left_thumbImage_nrml = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_norm);
        textViewPadding = 0.5f *  Left_thumbImage_nrml.getWidth();

        final MediaController mediaController = new MediaController(this);
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
        Log.d(String.valueOf(videoDuration), "dur");
        CalculateFrameSize(seekbarWidth);
        rect.setHeight(frameHeight);
        rect.width = seekbarWidth;
        rect.mSelectionEnd = seekbarWidth;

        rect.setParameters((float) (seekbarWidth * 0.1), ((float) (seekbarWidth * 0.9)), 0, rectTrim);
        rect.invalidate();


        layout = (LinearLayout) findViewById(R.id.linear_images);
        frameList = new ArrayList<Bitmap>();

        Log.d("frame width: ", String.valueOf(frameWidth));

        durationDouble = Double.parseDouble(time) * 1000;

        double frameTime = 0.;
        double frameFreq = durationDouble/TOTALFRAME;

         Log.d("frame freq", String.valueOf(frameFreq));
         Log.d("path", String.valueOf(path));


        for (int i = 0; i < TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);


            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameWidth, frameHeight, true);
            

            frameTime += frameFreq;
      //      Log.d("frame Time", String.valueOf(frameTime));
            //        frameList.add(bm);
            imageView.setLayoutParams(new LayoutParams(frameWidth, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }

        videoView.start();
        //   videoView.pause();

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

               // Intent intent = new Intent();
                //intent.setClass(VideoEditingTT.this, VideoPlayerActivity.class);
                //startActivity(intent);

                videoView.pause();
                apply.setText("Apply Trim");
                view.setPressed(true);
                //view.setBackgroundColor(Color.DKGRAY);
                trimButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                cutButton.setPressed(false);
                //cutButton.setBackgroundColor(Color.LTGRAY);
                cutButton.getBackground().setColorFilter(Color.LTGRAY,PorterDuff.Mode.MULTIPLY);
                rectTrim = true;
                rect.setParameters((float) RectParameters(previewStartPos),((float) RectParameters(previewEndPos)),0, rectTrim);
                rect.invalidate();

            }
        });

        cutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.pause();
                apply.setText("Apply Cut");
                view.setPressed(true);
              //  view.setBackgroundColor(Color.DKGRAY);
                cutButton.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);

                trimButton.setPressed(false);
                //    trimButton.setBackgroundColor(Color.LTGRAY);
                trimButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
                rectTrim = false;
                rect.setParameters((float) RectParameters(previewStartPos),((float) RectParameters(previewEndPos)),0, rectTrim);
                rect.invalidate();

                /*
                Intent intent = new Intent();
                intent.setClass(VideoEditingTT.this, ComposerCutCoreActivity.class);

                Bundle b = new Bundle();
                b.putString("srcMediaName1", fileName);
                intent.putExtras(b);
                b.putString("dstMediaPath", path);
                intent.putExtras(b);
                b.putLong("segmentPart1End", (long)(previewStartPos * 1000000));
                intent.putExtras(b);
                b.putLong("segmentPart2Start", (long)(previewEndPos * 1000000));
                intent.putExtras(b);
                b.putLong("segmentPart2End", durationLong);
                intent.putExtras(b);
                b.putString("srcUri1", fileUri.toString());
                intent.putExtras(b);

                startActivity(intent);
    */
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (apply.getText().equals("Apply Trim")) {

                    long segmentFrom = (long)(previewStartPos * 1000000);
                    long segmentTo = (long)(previewEndPos * 1000000);

                    Intent intent = new Intent();
                    intent.setClass(VideoEditingTT.this, ComposerTrimCoreActivity.class);

                    Bundle b = new Bundle();
                    b.putString("srcMediaName1", fileName);
                    intent.putExtras(b);
                    b.putString("dstMediaPath", path);
                    intent.putExtras(b);
                    b.putLong("segmentFrom", segmentFrom);
                    intent.putExtras(b);
                    b.putLong("segmentTo", segmentTo);
                    intent.putExtras(b);
                    b.putString("srcUri1", fileUri.toString());
                    intent.putExtras(b);

                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(VideoEditingTT.this, ComposerCutCoreActivity.class);

                    Bundle b = new Bundle();
                    b.putString("srcMediaName1", fileName);
                    intent.putExtras(b);
                    b.putString("dstMediaPath", path);
                    intent.putExtras(b);
                    b.putLong("segmentPart1End", (long)(previewStartPos * 1000000));
                    intent.putExtras(b);
                    b.putLong("segmentPart2Start", (long)(previewEndPos * 1000000));
                    intent.putExtras(b);
                    b.putLong("segmentPart2End", durationLong);
                    intent.putExtras(b);
                    b.putString("srcUri1", fileUri.toString());
                    intent.putExtras(b);

                    startActivity(intent);
                }
            }
        });


       jsonArray = new JSONArray();

        bookmarkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                videoView.pause();
                playButton.setText("Play");

                if (bookmarkAdded == false) {
                    edittext.setVisibility(View.VISIBLE);
                    textViewTs.setText(String.format("time: %.2f", previewStartPos));
                  //  textViewTs.setVisibility(View.VISIBLE);
                    bookmarkButton.setText("ADD");
                    bookmarkAdded = true;
                } else {
                    edittext.setVisibility(View.INVISIBLE);
                    bookmarkButton.setText("ADD BOOKMARK");
                    bookmarkAdded = false;

                    String[] text = edittext.getText().toString().split(":");
                //    textViewTs.setVisibility(View.INVISIBLE);

                    JSONObject bm = new JSONObject();
                    try {
                        bm.put("timestamp", previewStartPos);
                        bm.put("tag", text[0]);
                        if (text.length < 2)
                            bm.put("description", "");
                        else
                            bm.put("description", text[1]);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    jsonArray.put(bm);

                    edittext.setText("");

                    try {
                        listDataHeader.add(bm.getString("tag"));
                        List<String> l = new ArrayList<String>();
                        l.add(bm.getString("description"));

                        listDataChild.put(bm.getString("tag"), l);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   /* try {
                        MetaDataInsert mDI = new MetaDataInsert();
                        MetaDataRead metaDataRead = new MetaDataRead();

                        mDI.writeRandomMetadata(path, "ILK STRING");
                        String xml = metaDataRead.read(path);
                        Log.d("***********OKUNAN:", xml);

                        mDI.writeRandomMetadata(path, xml+"YENI STRING!!");
                        xml = metaDataRead.read(path);
                        Log.d("***********OKUNAN2:", xml);

                    } catch (IOException e) {
                        Log.d("!!!!!!!!", "Exception write");
                        e.printStackTrace();
                    }
                    */
                }

            }
        });


    }

    /*
     * Preparing the list data
     */
    private void prepareListData() throws JSONException {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        for (int i = 0; i < jsonArray.length(); i++) {
            Log.d(String.valueOf(jsonArray.getJSONObject(i).getInt("timestamp")), jsonArray.getJSONObject(i).getString("tag"));
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
                textViewTs.invalidate();
            //    int val = (minValue * (seekbarWidth - 2 * seekBar.getThumbOffset())) / seekBar.getMax();
                textViewMin.setText("" + String.format("%.2f", minValue));
                textViewMax.setText("" + String.format("%.2f", maxValue));
                textViewTs.setText("" + String.format("%.2f", minValue));
                textViewMin.setX((float) Scaler(minValue));

                textViewMax.setX((float) Scaler(maxValue));
           //     Log.d("setx location", String.valueOf(Scaler(minValue)));
            //    Log.d("min value", String.valueOf(minValue));

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
