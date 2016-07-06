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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCodecInfo;
import org.m4m.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;


import org.m4m.MediaFile;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.domain.Pair;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ComposerTranscodeCore implements SurfaceHolder.Callback {

    protected String srcMediaName1 = null;
    protected String srcMediaName2 = null;
    protected String dstMediaPath1 = null;
    protected org.m4m.Uri mediaUri1;
    protected org.m4m.Uri mediaUri2;

    protected org.m4m.MediaFileInfo mediaFileInfo = null;

    protected long duration = 0;

    public ProgressDialog progressDialog;
 //   protected ProgressBar progressDialog;

 //   protected TextView pathInfo;
  //  protected TextView durationInfo;
  //  protected TextView effectDetails;

   // private TextView audioChannelCountInfo;
   // private TextView audioSampleRateInfo;

  //  protected TextView transcodeInfoView;

    ///////////////////////////////////////////////////////////////////////////

    protected org.m4m.AudioFormat audioFormat = null;
    protected org.m4m.VideoFormat videoFormat = null;

    // Transcode parameters

    // Video
    protected int videoWidthOut;
    protected int videoHeightOut;

    protected int videoWidthIn = 640;
    protected int videoHeightIn = 480;

 //   protected Spinner frameSizeSpinner;
  //  protected Spinner videoBitRateSpinner;

    protected final String videoMimeType = "video/avc";
    protected int videoBitRateInKBytes = 5000;
    protected final int videoFrameRate = 30;

    protected final int videoIFrameInterval = 1;
    // Audio
    protected final String audioMimeType = "audio/mp4a-latm";
    protected final int audioSampleRate = 44100;
    protected final int audioChannelCount = 2;

    protected final int audioBitRate = 96 * 1024;

    public MediaFile mediaFile;
    public Long segmentFrom, segmentTo;
 //   protected Button buttonStop;

 //   protected Button buttonStart;

    ///////////////////////////////////////////////////////////////////////////

    // Media Composer parameters and logic

    protected org.m4m.MediaComposer mediaComposer;

    private boolean isStopped = false;
    public Context context;

    //  private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
    //         Environment.DIRECTORY_PICTURES), "MyCameraApp");
    //   private String fileName = "VID.mp4";
    // private String path = mediaStorageDir.getPath() + File.separator + fileName;


    public org.m4m.IProgressListener progressListener = new org.m4m.IProgressListener() {


        @Override
        public void onMediaStart() {
            Log.d("media" , "started");


        //    progressDialog.setProgress(0);
        }

        @Override
        public void onMediaProgress(float progress) {
            progressDialog.setProgress((int) (progressDialog.getMax() * progress));
        }

        @Override
        public void onMediaDone() {
//            progressDialog.dismiss();
            reportTranscodeDone();

         //   playResult();
        }

        /*  @Override
                public void onMediaDone() {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isStopped) {
                                    return;
                                }
                                updateUI(false);
                                reportTranscodeDone();
                            }
                        });
                    } catch (Exception e) {
                    }
                }
        */
        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {

        }
    };

    protected AndroidMediaObjectFactory factory;

    /////////////////////////////////////////////////////////////////////////

    protected ComposerTranscodeCore(Context context, ProgressDialog progressDialog) {

        this.context = context;
        this.progressDialog = progressDialog;

        initVideoSpinners();

//        progressDialog = (ProgressBar) findViewById(R.id.progressDialog);
  //      progressDialog.setMax(100);

        ////
   //     TranscodeSurfaceView transcodeSurfaceView = (TranscodeSurfaceView) findViewById(R.id.transcodeSurfaceView);
     //   transcodeSurfaceView.getHolder().addCallback(this);
        ////


     //   getActivityInputs();
  //      mediaFile = mediaComposer.getSourceFiles().get(0);

    }

    protected void onFinish() {
        if (mediaComposer != null) {
            mediaComposer.stop();
            isStopped = true;
        }
    }


    protected void initVideoSpinners() {

        videoWidthOut = Integer.valueOf(640);
        videoHeightOut = Integer.valueOf(480);

        videoBitRateInKBytes = Integer.valueOf(5000);
        //ArrayAdapter<CharSequence> adapter;

    }
    ///////////////////////////////////////////////////////////////////////////

    protected void getActivityInputs(String sourceMediaName, String dstMediaPath, String mediaUri, Long segmentFrom, Long segmentTo) {

    //    Bundle b = getIntent().getExtras();
        srcMediaName1 = sourceMediaName;
        dstMediaPath1 = dstMediaPath;
        mediaUri1 = new Uri(mediaUri);
        this.segmentTo = segmentTo;
        this.segmentFrom = segmentFrom;

        getFileInfo();
        startTranscode();
        printFileInfo();

        //   transcodeSurfaceView.setImageSize(videoWidthIn, videoHeightIn);

        updateUI(false);

    }

    /////////////////////////////////////////////////////////////////////////


    protected void getFileInfo() {
        try {

            mediaFileInfo = new org.m4m.MediaFileInfo(new AndroidMediaObjectFactory(context));
            mediaFileInfo.setUri(mediaUri1);

            duration = mediaFileInfo.getDurationInMicroSec();


            audioFormat = (org.m4m.AudioFormat) mediaFileInfo.getAudioFormat();
            if (audioFormat == null) {
                Log.d("Audio format info", "unavailable");

            }

            videoFormat = (org.m4m.VideoFormat) mediaFileInfo.getVideoFormat();
            if (videoFormat == null) {
               Log.d("Video format info ", "unavailable");
            } else {
                videoWidthIn = videoFormat.getVideoFrameSize().width();
                videoHeightIn = videoFormat.getVideoFrameSize().height();
            }
        } catch (Exception e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

            Log.d("File info", message);
        }
    }

    protected void displayVideoFrame(SurfaceHolder holder) {

        if (videoFormat != null) {
            try {
                ISurfaceWrapper surface = AndroidMediaObjectFactory.Converter.convert(holder.getSurface());
                mediaFileInfo.setOutputSurface(surface);

                ByteBuffer buffer = ByteBuffer.allocate(1);
                mediaFileInfo.getFrameAtPosition(100, buffer);

            } catch (Exception e) {
                String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

               Log.d("Display Video Frame", message);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        displayVideoFrame(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    ///////////////////////////////////////////////////////////////////////////

    protected void configureVideoEncoder(org.m4m.MediaComposer mediaComposer, int width, int height) {

        VideoFormatAndroid videoFormat = new VideoFormatAndroid(videoMimeType, width, height);

        videoFormat.setVideoBitRateInKBytes(videoBitRateInKBytes);
        videoFormat.setVideoFrameRate(videoFrameRate);
        videoFormat.setVideoIFrameInterval(videoIFrameInterval);

        mediaComposer.setTargetVideoFormat(videoFormat);

        Log.d("media codec", "fail" );
    }

    protected void configureAudioEncoder(org.m4m.MediaComposer mediaComposer) {

        /**
         * TODO: Audio resampling is unsupported by current m4m release
         * Output sample rate and channel count are the same as for input.
         */
        AudioFormatAndroid aFormat = new AudioFormatAndroid(audioMimeType, audioFormat.getAudioSampleRateInHz(), audioFormat.getAudioChannelCount());

        aFormat.setAudioBitrateInBytes(audioBitRate);
        aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mediaComposer.setTargetAudioFormat(aFormat);
    }

    protected void setTranscodeParameters(org.m4m.MediaComposer mediaComposer) throws IOException {

        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath1);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        mediaFile = mediaComposer.getSourceFiles().get(0);
     //   mediaFile.addSegment(new Pair<Long, Long>(segmentFrom, segmentTo));
    }

    protected void addSegment(Long segmentFrom, Long segmentTo) {
        mediaFile.addSegment(new Pair<Long, Long>(segmentFrom, segmentTo));
    }

    protected void transcode() throws Exception {

        factory = new AndroidMediaObjectFactory(context);
        mediaComposer = new org.m4m.MediaComposer(factory, progressListener);
        setTranscodeParameters(mediaComposer);
        mediaComposer.start();
    }

    public void startTranscode() {

        try {
            transcode();

        } catch (Exception e) {

       //     buttonStart.setEnabled(false);

            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

            Log.d("Start Transcode", message);
        }
    }

    public void stopTranscode() {
        mediaComposer.stop();
    }

    private void reportTranscodeDone() {

        String message = "Transcoding finished.";
        playResult();

    }

    private void playResult() {

        String videoUrl = "file:///" + dstMediaPath1;

        Log.d("dest:", dstMediaPath1.toString());
        //Intent intent = new Intent(Intent.ACTION_VIEW);

        Intent intent = new Intent (context, VideoEditingTT.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
     //   Uri data = Uri.parse(videoUrl);
       // intent.setDataAndType(data, "video/mp4");
    //    startActivity(intent);
    }

    private void updateUI(boolean inProgress) {
   //     buttonStart.setEnabled(!inProgress);
    //    buttonStop.setEnabled(inProgress);

        if (inProgress) {
     //       progressDialog.setVisibility(View.VISIBLE);
        } else {
       //     progressDialog.setVisibility(View.INVISIBLE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////    

    protected void printPaths() {
    //    pathInfo.setText(String.format("srcMediaFileName = %s\ndstMediaPath = %s\n", srcMediaName1, dstMediaPath1));
    }

    protected void getDstDuration() {
    }

    protected void printDuration() {
        getDstDuration();
     //   durationInfo.setText(String.format("Duration = %d sec", TimeUnit.MICROSECONDS.toSeconds(duration)));
    }

    protected void printEffectDetails() {
    }

    protected void printFileInfo() {
        printPaths();
        printDuration();
        printEffectDetails();
        printAudioInfo();
    }

    private void printAudioInfo() {
    //    audioChannelCountInfo.setText(String.valueOf(audioFormat.getAudioChannelCount()));
      //  audioSampleRateInfo.setText(String.valueOf(audioFormat.getAudioSampleRateInHz()));
    }
}


