package com.example.tulin.camapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

import java.io.File;


public class VideoEditing extends Activity {

    private VideoView videoView;

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment. DIRECTORY_PICTURES), "MyCameraApp");
    private String path = mediaStorageDir.getPath() + File.separator + "VID" + ".mp4";
    //private String path = Environment.getExternalStorageDirectory() + "/big_buck_bunn_short.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView =(VideoView)findViewById(R.id.video_view);
        Button playButton = (Button) findViewById(R.id.play_button);
        Button pauseButton = (Button) findViewById(R.id.pause_button);

        //Creating MediaController
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        //specify the location of media file
        Uri uri=Uri.parse(path);

        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);

        LinearLayout layout = (LinearLayout) findViewById(R.id.linear_images);

        int duration = 0;
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (time != null)
            duration = Integer.parseInt(time);
        if (duration != 0)
            duration /=  1000;

        int millis = 2000000;

        for (int i = 0; i < duration; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setPadding(0, 2, 0, 2);
            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(i * millis, MediaMetadataRetriever.OPTION_CLOSEST);

            imageView.setLayoutParams(new LayoutParams(100, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageBitmap(bmFrame);

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

}
