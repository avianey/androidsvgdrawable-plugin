/*
 * Copyright 2013, 2014 Antoine Vianey
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
package fr.avianey.androidsvgdrawable.gradle;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import fr.avianey.androidsvgdrawable.SvgDrawablePlugin;
import fr.avianey.androidsvgdrawable.SvgDrawablePlugin.Parameters;

/**
 * Goal which generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawableGradlePlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "svgDrawable";
    private static final String TASK_NAME = "svgDrawable";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(EXTENSION_NAME, Parameters.class);
        project.getTasks().create(TASK_NAME, SvgTask.class);
    }
    
    public static class SvgTask extends DefaultTask {

        @TaskAction
        public void svgToPng() {
            Parameters parameters = (Parameters) getProject().getExtensions().getByName(EXTENSION_NAME);
            if (parameters.svgMaskedSvgOutputDirectory == null) {
                parameters.svgMaskedSvgOutputDirectory = new File(getProject().getBuildDir(), "enerated-svg");
            }
            final SvgDrawablePlugin plugin = new SvgDrawablePlugin(parameters, new GradleLogger(getProject().getLogger()));
            plugin.execute();
        }
        
    }

}
