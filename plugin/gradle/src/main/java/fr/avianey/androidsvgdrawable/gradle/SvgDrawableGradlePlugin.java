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

import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * Task that generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawableGradlePlugin implements Plugin<Project> {

    private static final String ANDROID_PLUGIN_CLASS = "com.android.build.gradle.AppPlugin";

    @Override
    public void apply(Project project) {
        // Verify that Android plugin is applied
        Plugin<?> androidPlugin = null;
        for (Plugin<?> p : project.getPlugins()) {
            if (ANDROID_PLUGIN_CLASS.equals(p.getClass().getCanonicalName())) {
                androidPlugin = p;
                break;
            }
        }
        if (androidPlugin == null) {
            throw new GradleException("AndroidSvgDrawable MUST be used with the Android plugin.");
        }

        // Generate SVG on before Android plugin 'preBuild' task.
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Set<Task> preBuildTasks = project.getTasksByName("preBuild", false);
                if (preBuildTasks.isEmpty()) {
                    preBuildTasks.iterator().next().dependsOn(project.getTasks().withType(SvgDrawableTask.class));
                } else {
                    project.getLogger().warn("The Android plugin 'preBuild' task could not be found. Skipping SVG generation...");
                }
            }
        });
    }

}
