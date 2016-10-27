package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.tulin.camapp.controls.TimelineItem;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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

import static android.R.attr.data;

/**
 * Created by tulin on 28.06.16.
 */

public class AddBookmarkActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private VideoView videoView;
    private OneThumbSeekbar<Double> TTSeekbar;
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
    int padding = 65, frameHeight = 100, seekbarWidth;
    int TOTALFRAME = 14;
    ArrayList<Bitmap> frameList;
    LinearLayout layout;
    int screenWidthPx;
    TextView textViewMin, textViewTs;
    Button nextButton;
    boolean playing = true;
    boolean bookmarkAdded = false;

    float padd, textViewPadding;

    DrawBookmarkLine bookmarkLine;
    DrawBookmarkText bookmarkText;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    JSONArray jsonArray;
    int index = 1;

    EditText edittextTag, edittextDesc;

    int REQUEST_TAKE_GALLERY_VIDEO = 100;
    String selectedImagePath = null;
    Context context;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bookmark);

        videoView = (VideoView) findViewById(R.id.video_view);
        final Button playButton = (Button) findViewById(R.id.play_button);
        final Button bookmarkButton = (Button) findViewById(R.id.bookmark_button);
        textViewTs = (TextView) findViewById(R.id.textView_ts);
        edittextTag = (EditText) findViewById(R.id.edittextTag);
        edittextDesc = (EditText) findViewById(R.id.edittextDesc);
        bookmarkLine = (DrawBookmarkLine) findViewById(R.id.bookmark_line);
        bookmarkText = (DrawBookmarkText) findViewById(R.id.bookmark_text);

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
        textViewPadding = 0.5f * Left_thumbImage_nrml.getWidth();

        final MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // get screen width in pixels
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidthPx = size.x;


        frameHeight = (int) (size.y) / 6;

        // Convert padding to px from dp
        Resources r = getResources();
        padd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, r.getDisplayMetrics());

        seekbarWidth = screenWidthPx - (int) (padd);

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
        double frameFreq = durationDouble / TOTALFRAME;

        for (int i = 0; i <= TOTALFRAME; i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(i);


            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime((long) frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
            Bitmap bm = Bitmap.createScaledBitmap(bmFrame, frameHeight, frameHeight, true);

            frameTime += frameFreq;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(frameHeight, frameHeight));

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bmFrame);

            imageView.setImageBitmap(bm);
            layout.addView(imageView);
        }

        bookmarkLine.setHeight(frameHeight);
        bookmarkText.setHeight(frameHeight);

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


        jsonArray = new JSONArray();


        bookmarkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                videoView.pause();
                playButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_play));

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

                    bookmarkLine.setParameters((float) LineParameters(previewStartPos), 0, (float) LineParameters(previewStartPos), frameHeight, tag);
                    bookmarkLine.invalidate();

                    bookmarkText.setParameters((float) LineParameters(previewStartPos), 0, (float) LineParameters(previewStartPos), frameHeight, tag);
                    bookmarkText.invalidate();

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
                    videoView.seekTo((int) ((tS * 1000)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

            case R.id.action_next: {
                try {
                    writeToStorage(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
            }

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddBookmarkActivity.this, VideoEditingTT.class);
        startActivity(intent);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                String filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

//                Log.d("dosya path: ", selectedImagePath);

                if (selectedImagePath != null) {

                    Intent intent = new Intent(AddBookmarkActivity.this,
                            PlaybackActivity.class);
                    intent.putExtra("path", selectedImagePath);
                    startActivity(intent);
                }
            }
        }
    }

    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
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


                //handle start stage

                currentMinPos = minValue;
                videoView.seekTo((int) (currentMinPos * 1000));

                previewStartPos = minValue;

            }
        });

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.range_seekbar);
        viewGroup.bringToFront();


        if (initSeekbar) {
            viewGroup.addView(TTSeekbar);
            initSeekbar = false;
        } else {
            viewGroup.removeAllViews();
            viewGroup.addView(TTSeekbar);
        }

    }

    private double LineParameters(double val) {

        return (val / videoDuration) * (seekbarWidth);
    }

    private double Scaler(double val_to_scale) {

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

        return ((padd - textViewPadding) + ((val_to_scale / videoDuration) * (seekbarWidth)));

    }

    private void CalculateFrameSize(int width) {

        TOTALFRAME = (width / frameHeight);
        Log.d("TOTAL FRAME", String.valueOf(TOTALFRAME));

        // frameHeight = frameWidth;
    }

    private void writeToStorage(JSONArray array) throws JSONException {
        try {
            FileWriter fileWriter = new FileWriter(new File(mediaStorageDir.getPath() + File.separator + "bookmarks.txt"));
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

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("AddBookmark Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
