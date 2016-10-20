package com.example.tulin.camapp;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.tulin.camapp.controls.TimelineItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tulin on 03.07.16.
 */
public class PlaybackActivity extends Activity {

    private VideoView videoView;
    private  OneThumbSeekbar<Double> TTSeekbar;
    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;

    boolean initSeekbar = true;
    double videoDuration;
    double currentMinPos, previewEndPos, previewStartPos;
    double durationDouble;
    long durationLong = 1L;
    int padding = 22, frameWidth = 10, frameHeight = 10, seekbarWidth;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin;
    Button apply;
    boolean playing = true;
    boolean bookmarkAdded = false;

    float textViewPadding;
    DrawBookmarkLinePlayback bookmarkLine;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    JSONArray jsonArray;
    int index = 1;

    HashMap<String, Float> lines;

    TimelineItem item = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button bookmarkButton = (Button) findViewById(R.id.bookmark_button);
        bookmarkLine = (DrawBookmarkLinePlayback) findViewById(R.id.bookmark_line);


        lines = new HashMap<>();
        //   bookmarkLine.setParameters((float) 200, 0, (float)200, 100);
        //  bookmarkLine.invalidate();

        // crate bookmark view
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listDataHeader.add("Bookmarks");
        List<String> l = new ArrayList<>();
        l.add("");
        listDataChild.put("Bookmarks", l);
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);

        playButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);

        textViewMin = (TextView) findViewById(R.id.timestampMin);
        textViewMin.bringToFront();

        final Bitmap Left_thumbImage_nrml = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_norm);
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


        TTSeekbar = new OneThumbSeekbar<>(0., videoDuration, this);

        initSeekbar();

        previewStartPos = videoDuration * 0.1;
        previewEndPos = videoDuration * 0.9;
        CalculateFrameSize(seekbarWidth);

        layout = (LinearLayout) findViewById(R.id.linear_images);
        frameList = new ArrayList<>();

        durationDouble = Double.parseDouble(time) * 1000;

        double frameTime = 0.;
        double frameFreq = durationDouble/TOTALFRAME;

        for (int i = 0; i < TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);


            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameWidth, frameHeight, true);

            frameTime += frameFreq;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(frameWidth, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }

        bookmarkLine.setHeight(frameHeight);

        // fill the jsonarray with bookmark list
        try {
            readFromStorage();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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


        expListView.setOnChildClickListener(new ExpandableListView
                .OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView elv, View view, int i,
                                        int i2, long l) {

                Log.d(String.valueOf(i), "index degeri");
                try {
                    JSONObject jObj = (JSONObject) jsonArray.get(i);
                    double tS = jObj.getDouble("timestamp");
                    Log.d(String.valueOf(i), String.valueOf(tS));
                    videoView.seekTo((int)((tS*1000)));
                 //   TTSeekbar.drawLThumb(tS*1000, Left_thumbImage_nrml, );
                    currentMinPos = tS;
                    TTSeekbar.setSelectedMinValue(tS);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });


    }


    private void initSeekbar() {
        //Handle range seekbar
        TTSeekbar.setNotifyWhileDragging(true);
        TTSeekbar.setOnRangeSeekBarChangeListener(new OneThumbSeekbar.OnRangeSeekBarChangeListener<Double>() {


            @Override
            public void onRangeSeekBarValuesChanged(OneThumbSeekbar<?> bar, Double minValue) {

                textViewMin.invalidate();
                textViewMin.setText("" + String.format("%.2f", minValue));
                textViewMin.setX((float) Scaler(minValue));

                currentMinPos = minValue;
                videoView.seekTo((int)(currentMinPos * 1000));

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

    private double LineParameters(double val) {

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
        Double d  = Math.ceil((double)width / (double)TOTALFRAME);

        frameWidth = d.intValue();

        frameHeight = frameWidth;
    }

    private void readFromStorage() throws JSONException {
        try {

            jsonArray = new JSONArray();

            FileInputStream fis =  new FileInputStream(mediaStorageDir.getPath() + "/bookmarks.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            index = 1;
            while ((line = bufferedReader.readLine()) != null) {
                JSONObject jObj = new JSONObject(line);
                Log.d(jObj.get("tag").toString(), " ***** ");
                if (jObj != null) {

                    float pos = (float)LineParameters((double)jObj.get("timestamp"));

                    lines.put((String)jObj.get("tag"), pos);


                    Log.d(String.valueOf((jObj.get("timestamp"))), " *****000000000000000 "+String.valueOf(pos));
                    //    Log.d(String.valueOf(((double)jObj.get("timestamp"))/videoDuration), " *****00011111111111 "+String.valueOf(seekbarWidth));

                    jsonArray.put(index, jObj);
                    index++;
                }
                try {
                    listDataHeader.add(jObj.getString("tag"));
                    List<String> l = new ArrayList<String>();
                    l.add(jObj.getString("description"));
                    l.add(String.format("%.2f", jObj.getDouble("timestamp")));

                    listDataChild.put(jObj.getString("tag"), l);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            bookmarkLine.setParameters(lines);
            bookmarkLine.invalidate();

        } catch (FileNotFoundException e) {
            Log.d("file not foud ***"," ");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("io exc ***"," ");

            e.printStackTrace();
        }
    }
}