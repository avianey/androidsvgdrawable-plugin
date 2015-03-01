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
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import fr.avianey.androidsvgdrawable.BoundsType;
import fr.avianey.androidsvgdrawable.Density;
import fr.avianey.androidsvgdrawable.OutputFormat;
import fr.avianey.androidsvgdrawable.OverrideMode;
import fr.avianey.androidsvgdrawable.SvgDrawablePlugin;

public class SvgDrawableTask extends DefaultTask implements SvgDrawablePlugin.Parameters {

    public File from;
    public File to;
    public boolean createMissingDirectories = DEFAULT_CREATE_MISSING_DIRECTORIES;
    public OverrideMode overrideMode = OverrideMode.always;
    public Density[] targetedDensities;
    public Map<String, String> rename;
    public String highResIcon;

    // nine patch
    public File ninePatchConfig;

    // colorizer
    public File colorizerConfig;

    // masking
    public File svgMaskDirectory;
    public File svgMaskResourcesDirectory;
    public File svgMaskedSvgOutputDirectory;
    public boolean useSameSvgOnlyOnceInMask;

    // format
    public OutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;
    public int jpgQuality = DEFAULT_JPG_QUALITY;
    public int jpgBackgroundColor = DEFAULT_JPG_BACKGROUND_COLOR;

    // deprecated
    public BoundsType svgBoundsType = DEFAULT_BOUNDS_TYPE;

    @TaskAction
    public void svgToDrawable() {
        if (svgMaskedSvgOutputDirectory == null) {
            svgMaskedSvgOutputDirectory = new File(getProject().getBuildDir(), "generated-svg");
        }
        SvgDrawablePlugin plugin = new SvgDrawablePlugin(this, new GradleLogger(getProject().getLogger()));
        plugin.execute();
    }

    @Override
    public File getFrom() {
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
    public OverrideMode getOverrideMode() {
        return overrideMode;
    }

    @Override
    public Density[] getTargetedDensities() {
        return targetedDensities;
    }

    @Override
    public Map<String, String> getRename() {
        return rename;
    }

    @Override
    public String getHighResIcon() {
        return highResIcon;
    }

    @Override
    public File getNinePatchConfig() {
        return ninePatchConfig;
    }

    @Override
    public File getColorizerConfig() {
        return colorizerConfig;
    }

    @Override
    public File getSvgMaskDirectory() {
        return svgMaskDirectory;
    }

    @Override
    public File getSvgMaskResourcesDirectory() {
        return svgMaskResourcesDirectory;
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

    public void setFrom(File from) {
        this.from = from;
    }

    public void setTo(File to) {
        this.to = to;
    }

    public void setCreateMissingDirectories(boolean createMissingDirectories) {
        this.createMissingDirectories = createMissingDirectories;
    }

    public void setOverrideMode(OverrideMode overrideMode) {
        this.overrideMode = overrideMode;
    }

    public void setTargetedDensities(Density[] targetedDensities) {
        this.targetedDensities = targetedDensities;
    }

    public void setRename(Map<String, String> rename) {
        this.rename = rename;
    }

    public void setHighResIcon(String highResIcon) {
        this.highResIcon = highResIcon;
    }

    public void setNinePatchConfig(File ninePatchConfig) {
        this.ninePatchConfig = ninePatchConfig;
    }

    public void setColorizerConfig(File colorizerConfig) {
        this.colorizerConfig = colorizerConfig;
    }

    public void setSvgMaskDirectory(File svgMaskDirectory) {
        this.svgMaskDirectory = svgMaskDirectory;
    }

    public void setSvgMaskResourcesDirectory(File svgMaskResourcesDirectory) {
        this.svgMaskResourcesDirectory = svgMaskResourcesDirectory;
    }

    public void setSvgMaskedSvgOutputDirectory(File svgMaskedSvgOutputDirectory) {
        this.svgMaskedSvgOutputDirectory = svgMaskedSvgOutputDirectory;
    }

    public void setUseSameSvgOnlyOnceInMask(boolean useSameSvgOnlyOnceInMask) {
        this.useSameSvgOnlyOnceInMask = useSameSvgOnlyOnceInMask;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void setJpgQuality(int jpgQuality) {
        this.jpgQuality = jpgQuality;
    }

    public void setJpgBackgroundColor(int jpgBackgroundColor) {
        this.jpgBackgroundColor = jpgBackgroundColor;
    }

    public void setSvgBoundsType(BoundsType svgBoundsType) {
        this.svgBoundsType = svgBoundsType;
    }

}