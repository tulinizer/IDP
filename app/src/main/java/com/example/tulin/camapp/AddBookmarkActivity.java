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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tulin on 28.06.16.
 */

public class AddBookmarkActivity extends Activity {

    private VideoView videoView;
    private  OneThumbSeekbar<Double> TTSeekbar;
    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;
    private Uri fileUri = Uri.parse(path.toString());

    boolean initialState = true;
    boolean initSeekbar = true;
    double videoDuration, stopPos;
    double currentMinPos, currentMaxPos, previewEndPos, previewStartPos;
    double durationDouble;
    long durationLong = 1L;
    int padding = 22, frameWidth = 10, frameHeight = 10, seekbarWidth;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin, textViewTs;
    Button nextButton;
    boolean playing = true;
    boolean bookmarkAdded = false;

    float textViewPadding;

    DrawBookmarkLine bookmarkLine;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    JSONArray jsonArray;
    int index = 1;

    EditText edittextTag, edittextDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bookmark);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button bookmarkButton = (Button) findViewById(R.id.bookmark_button);
        nextButton = (Button) findViewById(R.id.next_button);
        textViewTs = (TextView) findViewById(R.id.textView_ts);
        edittextTag = (EditText) findViewById(R.id.edittextTag);
        edittextDesc = (EditText) findViewById(R.id.edittextDesc);
        bookmarkLine = (DrawBookmarkLine) findViewById(R.id.bookmark_line);

     //   bookmarkLine.setParameters((float) 200, 0, (float)200, 100);
      //  bookmarkLine.invalidate();


        // get the listview
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


        jsonArray = new JSONArray();


        bookmarkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                videoView.pause();
                playButton.setText("Play");

                if (bookmarkAdded == false) {
                    edittextTag.setVisibility(View.VISIBLE);
                    edittextDesc.setVisibility(View.VISIBLE);
                    textViewTs.setText(String.format("time: %.2f", previewStartPos));
                    //  textViewTs.setVisibility(View.VISIBLE);
                    bookmarkButton.setText("ADD");
                    bookmarkAdded = true;
                } else {
                    edittextTag.setVisibility(View.INVISIBLE);
                    edittextDesc.setVisibility(View.INVISIBLE);
                    bookmarkButton.setText("ADD BOOKMARK");
                    bookmarkAdded = false;

                    //    textViewTs.setVisibility(View.INVISIBLE);

                    JSONObject bookMark = new JSONObject();
                    try {
                        bookMark.put("timestamp", previewStartPos);
                        bookMark.put("tag", edittextTag.getText().toString());
                        bookMark.put("description", edittextDesc.getText().toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        jsonArray.put(index, bookMark);
                        index++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String tag = edittextTag.getText().toString();

                    edittextTag.setText("");
                    edittextDesc.setText("");

                    bookmarkLine.setParameters((float) LineParameters(previewStartPos), 0, (float)LineParameters(previewStartPos), frameHeight, tag);
                    bookmarkLine.invalidate();

                    try {
                        listDataHeader.add(bookMark.getString("tag"));
                        List<String> l = new ArrayList<String>();
                        l.add(bookMark.getString("description"));
                        l.add(String.format("%.2f", bookMark.getDouble("timestamp")));

                        listDataChild.put(bookMark.getString("tag"), l);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            try {
                writeToStorage(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent();
            intent.setClass(AddBookmarkActivity.this, PlaybackActivity.class);
            startActivity(intent);
            }
        });


        expListView.setOnChildClickListener(new ExpandableListView
                .OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView elv, View view, int i,
                                        int i2, long l) {

                Log.d(String.valueOf(i), "index degeri");
                try {
                    JSONObject bookMark = (JSONObject) jsonArray.get(i);
                    double tS = bookMark.getDouble("timestamp");
                    Log.d(String.valueOf(i), String.valueOf(tS));
                    videoView.seekTo((int)((tS*1000)));
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
                textViewTs.invalidate();
                textViewMin.setText("" + String.format("%.2f", minValue));
                textViewTs.setText("" + String.format("%.2f", minValue));
                textViewMin.setX((float) Scaler(minValue));


              //  bookmarkLine.setParameters((float) LineParameters(minValue)-50, 0, (float)LineParameters(minValue)-50, frameHeight);
              //  bookmarkLine.invalidate();

                //handle start stage

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

    private void writeToStorage(JSONArray array) throws JSONException {
        try {
            FileWriter fileWriter= new FileWriter(new File(mediaStorageDir.getPath() + File.separator + "bookmarks.txt"));
            BufferedWriter out = new BufferedWriter(fileWriter);

            String[] jsonString = new String[array.length()];
            for (int i = 1; i < array.length(); i++) {
                jsonString[i] = array.get(i).toString();
                out.write(jsonString[i] + "\n");
            }
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
