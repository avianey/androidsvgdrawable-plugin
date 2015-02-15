package fr.avianey.androidsvgdrawable.util;

import java.io.File;
import java.util.Map;

import fr.avianey.androidsvgdrawable.BoundsType;
import fr.avianey.androidsvgdrawable.Density;
import fr.avianey.androidsvgdrawable.OutputFormat;
import fr.avianey.androidsvgdrawable.OverrideMode;
import fr.avianey.androidsvgdrawable.SvgDrawablePlugin;

public class TestParameters implements SvgDrawablePlugin.Parameters {

    public File from;
    public File to;
    public boolean createMissingDirectories = DEFAULT_CREATE_MISSING_DIRECTORIES;
    public OverrideMode overrideMode = OverrideMode.always;
    public Density[] targetedDensities;
    public Map<String, String> rename;
    public String highResIcon;

    // nine patch
    public File ninePatchConfig;

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

}
