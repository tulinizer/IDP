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

package org.m4m.domain;

import org.m4m.IRecognitionPlugin;
import org.m4m.IVideoEffect;

import org.m4m.domain.dsl.*;
import org.m4m.domain.mediaComposer.ProgressListenerFake;

public class TestBase {
    protected final Father create = new Father();
    protected final Father a;

    public TestBase() {
        a = create;
    }

    protected CommandQueueAssert assertThat(CommandQueue commandQueue) {
        return new CommandQueueAssert(commandQueue);
    }

    protected SinkAssert assertThat(Render sink) {
        return new SinkAssert(sink);
    }

    protected MediaSourceAssert assertThat(IOutput mediaSource) {
        return new MediaSourceAssert(create, mediaSource);
    }

    protected CommandProcessorAssert assertThat(CommandProcessorSpy commandProcessor) {
        return new CommandProcessorAssert(commandProcessor);
    }

    protected ProgressListenerAssert assertThat(ProgressListenerFake progressListener) {
        return new ProgressListenerAssert(progressListener);
    }

    protected SegmentAssert assertThat(Segments segments) {
        return new SegmentAssert(segments);
    }

    protected RecognitionPluginAssert assertThat(IRecognitionPlugin plugin) {
        return new RecognitionPluginAssert(plugin);
    }

    protected VideoEffectAssert assertThat(IVideoEffect videoEffect) {
        return new VideoEffectAssert(videoEffect);
    }
}
