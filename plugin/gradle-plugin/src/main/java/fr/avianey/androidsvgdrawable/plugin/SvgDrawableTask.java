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

import fr.avianey.androidsvgdrawable.*;
import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class SvgDrawableTask extends DefaultTask implements SvgDrawablePlugin.Parameters {

    @InputFiles
    public FileCollection from;

    @OutputDirectory
    public File to;
    @Input
    public boolean createMissingDirectories = DEFAULT_CREATE_MISSING_DIRECTORIES;
    @Input
    public Density.Value[] targetedDensities;
    @Input
    @Optional
    public Density.Value noDpiDensity;

    // nine patch
    @InputFile
    @Optional
    public File ninePatchConfig;

    // masking
    @InputFiles
    @Optional
    public FileCollection svgMaskFiles;
    @InputFiles
    @Optional
    public FileCollection svgMaskResourceFiles;
    @OutputDirectory
    @Optional
    public File svgMaskedSvgOutputDirectory;
    @Input
    public boolean useSameSvgOnlyOnceInMask;

    // type
    @Input
    public OutputType outputType = DEFAULT_OUTPUT_TYPE;

    // format
    @Input
    public OutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;
    @Input
    public int jpgQuality = DEFAULT_JPG_QUALITY;
    @Input
    public int jpgBackgroundColor = DEFAULT_JPG_BACKGROUND_COLOR;

    // deprecated
    @Input
    @Nullable
    public BoundsType svgBoundsType = DEFAULT_BOUNDS_TYPE;

    @Override
    public Task configure(Closure closure) {
        Task task = super.configure(closure);
        if (svgMaskedSvgOutputDirectory == null) {
            svgMaskedSvgOutputDirectory = new File(getProject().getBuildDir(), "generated-svg");
        }
        return task;
    }

    @TaskAction
    public void transcode() {
        SvgDrawablePlugin plugin = new SvgDrawablePlugin(this, new GradleLogger(getProject().getLogger()));
        plugin.execute();
    }

    @Override
    @InputFiles
    public Iterable<File> getFiles() {
        if (from == null) {
            return Collections.emptyList();
        }
        return from.getFiles();
    }

    public FileCollection getFrom() {
        return from;
    }

    @Override
    public File getTo() {
        return to;
    }

    @Override
    public boolean isCreateMissingDirectories() {
        return createMissingDirectories;
    }

    @Override
    public Density.Value[] getTargetedDensities() {
        return targetedDensities;
    }

    @Nullable
    @Override
    public Density.Value getNoDpiDensity() {
        return noDpiDensity;
    }

    @Override
    public File getNinePatchConfig() {
        return ninePatchConfig;
    }

    @Override
    public Iterable<File> getSvgMaskFiles() {
        return svgMaskFiles;
    }

    @Override
    public Iterable<File> getSvgMaskResourceFiles() {
        return svgMaskResourceFiles;
    }

    @Override
    public File getSvgMaskedSvgOutputDirectory() {
        return svgMaskedSvgOutputDirectory;
    }

    @Override
    public boolean isUseSameSvgOnlyOnceInMask() {
        return useSameSvgOnlyOnceInMask;
    }

    @Override
    public OutputType getOutputType() {
        return outputType;
    }

    @Override
    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public int getJpgQuality() {
        return jpgQuality;
    }

    @Override
    public int getJpgBackgroundColor() {
        return jpgBackgroundColor;
    }

    @Override
    public BoundsType getSvgBoundsType() {
        return svgBoundsType;
    }

}
