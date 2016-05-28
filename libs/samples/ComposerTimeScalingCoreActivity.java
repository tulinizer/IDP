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

package org.m4m.samples;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;
import org.m4m.domain.FileSegment;
import org.m4m.domain.ISurfaceWrapper;
import org.m4m.samples.controls.TranscodeSurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ComposerTimeScalingCoreActivity extends ActivityWithTimeline implements SurfaceHolder.Callback {

    protected String srcMediaName = null;
    protected String dstMediaPath = null;
    protected org.m4m.Uri mediaUri;
    protected int timeScale;

    protected org.m4m.MediaFileInfo mediaFileInfo = null;

    protected long duration = 0;

    protected ProgressBar progressBar;

    protected TextView pathInfo;
    protected TextView durationInfo;
    protected TextView effectDetails;

    protected TextView transcodeInfoView;

    protected org.m4m.AudioFormat audioFormat = null;
    protected org.m4m.VideoFormat videoFormat = null;

    // Video
    protected int videoWidthOut;
    protected int videoHeightOut;

    protected int videoWidthIn = 640;
    protected int videoHeightIn = 480;

    protected Spinner frameSizeSpinner;
    protected Spinner videoBitRateSpinner;

    protected final String videoMimeType = "video/avc";
    protected int videoBitRateInKBytes = 5000;
    protected final int videoFrameRate = 30;

    protected final int videoIFrameInterval = 1;

    // Audio
    protected final String audioMimeType = "audio/mp4a-latm";
    protected final int audioSampleRate = 48000;
    protected final int audioChannelCount = 1;//2;

    protected final int audioBitRate = 96 * 1024;
    protected Button buttonStop;

    protected Button buttonStart;

    // Media Composer parameters and logic
    protected org.m4m.MediaComposer mediaComposer;

    private boolean isStopped = false;

    public org.m4m.IProgressListener progressListener = new org.m4m.IProgressListener() {
        @Override
        public void onMediaStart() {

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0);
                        frameSizeSpinner.setEnabled(false);
                        videoBitRateSpinner.setEnabled(false);
                        updateUI(true);
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        public void onMediaProgress(float progress) {

            final float mediaProgress = progress;

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) (progressBar.getMax() * mediaProgress/* * timeScale*/));
                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
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

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
            try {
                final Exception e = exception;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(false);
                        String message = (e.getMessage() != null) ? e.getMessage() : e.toString();
                        showMessageBox("Transcoding failed." + "\n" + message, null);
                    }
                });
            } catch (Exception e) {
            }
        }
    };
    protected AndroidMediaObjectFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.composer_transcode_core_activity);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        pathInfo = (TextView) findViewById(R.id.pathInfo);
        durationInfo = (TextView) findViewById(R.id.durationInfo);
        effectDetails = (TextView) findViewById(R.id.effectDetails);

        transcodeInfoView = (TextView) findViewById(R.id.transcodeInfo);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        TranscodeSurfaceView transcodeSurfaceView = (TranscodeSurfaceView) findViewById(R.id.transcodeSurfaceView);
        transcodeSurfaceView.getHolder().addCallback(this);

        getActivityInputs();

        initVideoSpinners();

        getFileInfo();
        setupUI();
        printFileInfo();

        transcodeSurfaceView.setImageSize(videoWidthIn, videoHeightIn);

        updateUI(false);
    }

    @Override
    protected void onDestroy() {
        if (mediaComposer != null) {
            mediaComposer.stop();
            isStopped = true;
        }
        super.onDestroy();
    }

    protected void setupUI() {
        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startTranscode();
            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTranscode();
            }
        });
    }

    protected ArrayAdapter<CharSequence> createFrameSizeArrayAdapter() {
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(this, R.array.frame_size_values, android.R.layout.simple_spinner_item);
        return adapter;
    }

    protected void initVideoSpinners() {

        // Video parameters spinners
        frameSizeSpinner = (Spinner) findViewById(R.id.frameSize_spinner);

        ArrayAdapter<CharSequence> adapter = createFrameSizeArrayAdapter();

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        frameSizeSpinner.setAdapter(adapter);
        frameSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] frameSizeContainer = frameSizeSpinner.getSelectedItem().toString().split("x", 2);
                videoWidthOut = Integer.valueOf(frameSizeContainer[0].trim());
                videoHeightOut = Integer.valueOf(frameSizeContainer[1].trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        videoBitRateSpinner = (Spinner) findViewById(R.id.videoBitRate_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.video_bit_rate_values, android.R.layout.simple_spinner_item);
        videoBitRateSpinner.setAdapter(adapter);
        videoBitRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                videoBitRateInKBytes = Integer.valueOf(videoBitRateSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName = b.getString("srcMediaName");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri = new org.m4m.Uri(b.getString("srcUri"));
        timeScale = b.getInt("timeScale");

//        audioSampleRate *= timeScale;
    }

    protected void getFileInfo() {
        try {
            mediaFileInfo = new org.m4m.MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri);

            duration = mediaFileInfo.getDurationInMicroSec() / timeScale;

            audioFormat = (org.m4m.AudioFormat) mediaFileInfo.getAudioFormat();
            if (audioFormat == null) {
                showMessageBox("Audio format info unavailable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            }

            videoFormat = (org.m4m.VideoFormat) mediaFileInfo.getVideoFormat();
            if (videoFormat == null) {
                showMessageBox("Video format info unavailable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            } else {
                videoWidthIn = videoFormat.getVideoFrameSize().width();
                videoHeightIn = videoFormat.getVideoFrameSize().height();
            }
        } catch (Exception e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

            showMessageBox(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
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

                showMessageBox(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
            }
        }
    }

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

    protected void configureVideoEncoder(org.m4m.MediaComposer mediaComposer, int width, int height) {

        VideoFormatAndroid videoFormat = new VideoFormatAndroid(videoMimeType, width, height);

        videoFormat.setVideoBitRateInKBytes(videoBitRateInKBytes);
        videoFormat.setVideoFrameRate(videoFrameRate);
        videoFormat.setVideoIFrameInterval(videoIFrameInterval);

        mediaComposer.setTargetVideoFormat(videoFormat);
    }

    protected void configureAudioEncoder(org.m4m.MediaComposer mediaComposer) {

        AudioFormatAndroid audioFormat = new AudioFormatAndroid(audioMimeType, audioSampleRate, audioChannelCount);

        audioFormat.setAudioBitrateInBytes(audioBitRate);
        audioFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mediaComposer.setTargetAudioFormat(audioFormat);
    }

    protected void setTranscodeParameters(org.m4m.MediaComposer mediaComposer) throws IOException {

        mediaComposer.addSourceFile(mediaUri);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);

        configureTimeScaler(mediaComposer);

//        configureAudioEncoder(mediaComposer);
    }

    private void configureTimeScaler(org.m4m.MediaComposer mediaComposer) {
        mediaComposer.setVideoTimeScale(timeScale, new FileSegment(0l, mediaFileInfo.getDurationInMicroSec()));
    }

    protected void transcode() throws Exception {

        factory = new AndroidMediaObjectFactory(getApplicationContext());
        mediaComposer = new org.m4m.MediaComposer(factory, progressListener);
        setTranscodeParameters(mediaComposer);
        mediaComposer.start();
    }

    public void startTranscode() {

        try {
            transcode();

        } catch (Exception e) {

            buttonStart.setEnabled(false);

            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();

            showMessageBox(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
    }

    public void stopTranscode() {
        mediaComposer.stop();
    }

    private void reportTranscodeDone() {

        String message = "Transcoding finished.";

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                progressBar.setVisibility(View.INVISIBLE);
                findViewById(R.id.buttonStart).setVisibility(View.GONE);
                findViewById(R.id.buttonStop).setVisibility(View.GONE);

                OnClickListener l = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        playResult();
                    }
                };

                ImageButton ib = (ImageButton) findViewById(R.id.imageButtonPlay);
                ib.setVisibility(View.VISIBLE);
                ib.setOnClickListener(l);
            }
        };
        showMessageBox(message, listener);
    }

    private void playResult() {

        String videoUrl = "file:///" + dstMediaPath;

        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri data = Uri.parse(videoUrl);
        intent.setDataAndType(data, "video/mp4");
        startActivity(intent);
    }

    private void updateUI(boolean inProgress) {
        buttonStart.setEnabled(!inProgress);
        buttonStop.setEnabled(inProgress);

        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void printPaths() {
        pathInfo.setText(String.format("srcMediaFileName = %s\ndstMediaPath = %s\n", srcMediaName, dstMediaPath));
    }

    protected void getDstDuration() {
    }

    protected void printDuration() {
        getDstDuration();
        durationInfo.setText(String.format("Duration = %d sec", TimeUnit.MICROSECONDS.toSeconds(duration)));
    }

    protected void printEffectDetails() {
    }

    protected void printFileInfo() {
        printPaths();
        printDuration();
        printEffectDetails();
    }
}
