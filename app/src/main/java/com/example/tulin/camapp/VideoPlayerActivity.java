/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tulin.camapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends Activity {

    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");
    private String fileName = "VID.mp4";
    private String path = mediaStorageDir.getPath() + File.separator + fileName;
    public String VIDEO_PATH =  mediaStorageDir.getPath() + File.separator + fileName;

    private VideoView mVideoView;
    private MediaController mVideoController;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.video_player_activity);

        init();

        //   if (icicle != null) {
          //  String path = icicle.getString("path");

            playVideo(path);
        //}
    }

    public void onPause() {
        super.onPause();

        mVideoView.stopPlayback();
    }

    private void init() {
        mVideoView = (VideoView) findViewById(R.id.video_view);

        mVideoController = new MediaController(this, false);

        mVideoView.setMediaController(mVideoController);

        mVideoView.requestFocus();
        mVideoView.setZOrderOnTop(true);
    }

    private void playVideo(String path) {
        Uri uri = Uri.parse(path);
        mVideoView.setVideoURI(uri);

        mVideoView.start();
    }
}
