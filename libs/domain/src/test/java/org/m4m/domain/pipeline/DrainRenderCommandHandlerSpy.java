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

package org.m4m.domain.pipeline;

import org.m4m.domain.IPluginOutput;
import org.m4m.domain.Render;
import org.m4m.domain.dsl.CommandProcessorSpy;

public class DrainRenderCommandHandlerSpy extends DrainRenderCommandHandler {

    private final CommandProcessorSpy commandProcessorSpy;
    private final IPluginOutput plugin;

    public DrainRenderCommandHandlerSpy(IPluginOutput plugin, Render render, CommandProcessorSpy processorSpy) {
        super(render);
        this.plugin = plugin;
        this.commandProcessorSpy = processorSpy;
    }

    @Override
    public void handle() {
        commandProcessorSpy.logPair(plugin, render);
        super.handle();
    }
}
