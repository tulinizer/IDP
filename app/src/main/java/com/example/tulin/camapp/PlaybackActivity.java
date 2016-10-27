package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
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
public class PlaybackActivity extends AppCompatActivity {

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
    int padding = 65, frameHeight = 100, seekbarWidth;
    float padd;
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
    DrawBookmarkTextPlayback bookmarkText;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    JSONArray jsonArray;
    int index = 1;

    HashMap<String, Float> lines;

    TimelineItem item = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button bookmarkButton = (Button) findViewById(R.id.bookmark_button);
        bookmarkLine = (DrawBookmarkLinePlayback) findViewById(R.id.bookmark_line);
        bookmarkText = (DrawBookmarkTextPlayback) findViewById(R.id.bookmark_text);

        context = getApplicationContext();


        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


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

        frameHeight =  (int) (size.y)/6;

        // Convert padding to px from dp
        Resources r = getResources();
        padd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, r.getDisplayMetrics());

        seekbarWidth = screenWidthPx - (int)(padd);

        Log.d(String.valueOf(seekbarWidth), "seekbar width px");
        Log.d(String.valueOf(textViewPadding), "padding in px");

        //specify the location of media file
        final Uri uri = Uri.parse(path);
        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                playButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_play));
                playing = false;

            }
        });

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

        for (int i = 0; i <= TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);


            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long)frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameHeight, frameHeight, true);

            frameTime += frameFreq;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(frameHeight, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }

        bookmarkLine.setHeight(frameHeight);
        bookmarkLine.bringToFront();

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
                    playButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_play));
                    playing = false;
                } else {
                    videoView.start();
                    playButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_pause));
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PlaybackActivity.this, AddBookmarkActivity.class);
        startActivity(intent);
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

        return ((padd - textViewPadding) + ((val_to_scale/videoDuration) * (seekbarWidth)));

    }

    private void CalculateFrameSize(int width) {

        TOTALFRAME = (width / frameHeight);
        Log.d("TOTAL FRAME", String.valueOf(TOTALFRAME));

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

            bookmarkLine.setParameters(lines, frameHeight);
            bookmarkLine.invalidate();
            bookmarkText.setParameters(lines, frameHeight);
            bookmarkText.invalidate();

        } catch (FileNotFoundException e) {
            Log.d("file not foud ***"," ");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("io exc ***"," ");

            e.printStackTrace();
        }
    }
}