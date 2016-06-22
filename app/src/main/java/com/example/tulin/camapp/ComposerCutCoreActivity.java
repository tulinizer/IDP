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

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import org.m4m.MediaComposer;
import org.m4m.MediaFile;
import org.m4m.Uri;
import org.m4m.domain.Pair;

import java.io.File;
import java.io.IOException;

public class ComposerCutCoreActivity extends ComposerTranscodeCoreActivity {

    private long segmentPart1End, segmentPart2Start, segmentPart2End = 0;

    @Override
    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new Uri(b.getString("srcUri1"));

        segmentPart1End = b.getLong("segmentPart1End");
        segmentPart2Start = b.getLong("segmentPart2Start");
        segmentPart2End = b.getLong("segmentPart2End");
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        ///////////////////////////

        MediaFile mediaFile = mediaComposer.getSourceFiles().get(0);
        mediaFile.addSegment(new Pair<Long, Long>(0L, segmentPart1End));
        mediaFile.addSegment(new Pair<Long, Long>(segmentPart2Start, segmentPart2End));
      //  Log.d("segment from  " + String.valueOf((float)segmentFrom/1e6), "Segmetn to " + String.valueOf((float)segmentTo/1e6));
    }

    @Override
    protected void printDuration() {

    	TextView v = (TextView)findViewById(R.id.durationInfo);
     //   v.setText(String.format("duration = %.1f sec\n", (float) (segmentPart1End) / 1e6));
        v.append(String.format("from = %.1f sec\nto = %.1f sec\n", (float) 0L / 1e6, (float) segmentPart1End / 1e6));
        v.append(String.format("from = %.1f sec\nto = %.1f sec\n", (float) segmentPart2Start / 1e6, (float) segmentPart2End / 1e6));

    }
}

