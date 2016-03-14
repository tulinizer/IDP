package com.example.tulin.camapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;

public class VideoEditing extends Activity {

    private VideoView videoView;
    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment. DIRECTORY_PICTURES), "MyCameraApp");
    private String path = mediaStorageDir.getPath() + File.separator + ".mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        videoView =(VideoView)findViewById(R.id.videoView);
        Button playButton = (Button) findViewById(R.id.play_video);
        Button pauseButton = (Button) findViewById(R.id.pause_video);

        //Creating MediaController
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        //specify the location of media file
        Uri uri=Uri.parse(path);

        //Setting MediaController and URI, then starting the videoView
        videoView.setVideoURI(uri);

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                videoView.start();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener () {

            @Override
            public void onClick(View view) {
                videoView.pause();
            }
        });

    }

}
