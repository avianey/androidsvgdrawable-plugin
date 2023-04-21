/*
 * Copyright 2013, 2014, 2015 Antoine Vianey
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
package fr.avianey.androidsvgdrawable.plugin;

import org.gradle.api.*;

import java.util.Set;

/**
 * Task that generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawableGradlePlugin implements Plugin<Project> {

    private static final String ANDROID_PLUGIN_CLASS_REGEXP = "com\\.android\\.build\\.gradle\\.(?:AtomPlugin|FeaturePlugin|InstantAppPlugin|AppPlugin|LibraryPlugin)";

    @Override
    public void apply(Project project) {
        // Verify that Android plugin is applied
        Plugin<?> androidPlugin = null;
        for (Plugin<?> p : project.getPlugins()) {
            if (p.getClass().getCanonicalName().matches(ANDROID_PLUGIN_CLASS_REGEXP)) {
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
                    project.getLogger().error("The Android plugin 'preBuild' task could not be found. Skipping SVG generation...");
                } else {
                    preBuildTasks.iterator().next().dependsOn(project.getTasks().withType(SvgDrawableTask.class));
                }
            }
        });
    }

}
