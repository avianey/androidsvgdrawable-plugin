package fr.avianey.androidsvgdrawable.util;

import fr.avianey.androidsvgdrawable.*;

import java.io.File;

public class TestParameters implements SvgDrawablePlugin.Parameters {

    public Iterable<File> from;
    public File to;
    public boolean createMissingDirectories = DEFAULT_CREATE_MISSING_DIRECTORIES;
    public OverrideMode overrideMode = OverrideMode.always;
    public Density.Value[] targetedDensities;

    // nine patch
    public File ninePatchConfig;

    // masking
    public Iterable<File> svgMaskFiles;
    public Iterable<File> svgMaskResourceFiles;
    public File svgMaskedSvgOutputDirectory;
    public boolean useSameSvgOnlyOnceInMask;

    // type
    public OutputType outputType = DEFAULT_OUTPUT_TYPE;

    // format
    public OutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;
    public int jpgQuality = DEFAULT_JPG_QUALITY;
    public int jpgBackgroundColor = DEFAULT_JPG_BACKGROUND_COLOR;

    // deprecated
    public BoundsType svgBoundsType = DEFAULT_BOUNDS_TYPE;

    @Override
    public Iterable<File> getFiles() {
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
    public Density.Value[] getTargetedDensities() {
        return targetedDensities;
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

    @Override
    public OutputType getOutputType() {
        return null;
    }

}
