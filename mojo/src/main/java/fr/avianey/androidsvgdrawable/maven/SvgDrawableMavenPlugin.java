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
package fr.avianey.androidsvgdrawable.maven;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import fr.avianey.androidsvgdrawable.BoundsType;
import fr.avianey.androidsvgdrawable.Density;
import fr.avianey.androidsvgdrawable.OutputFormat;
import fr.avianey.androidsvgdrawable.OverrideMode;
import fr.avianey.androidsvgdrawable.SvgDrawablePlugin;

/**
 * Goal which generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
@Mojo(name = "gen")
public class SvgDrawableMavenPlugin extends AbstractMojo {

    /**
     * Directory of the svg resources to generate drawable from.
     * 
     * @since 1.0.0
     */
    @Parameter(required = true)
    private File from;

    /**
     * Location of the Android "./src/main/res/drawable(-.*)" directories :
     * <ul>
     * <li>drawable</li>
     * <li>drawable-hdpi</li>
     * <li>drawable-ldpi</li>
     * <li>drawable-mdpi</li>
     * <li>drawable-xhdpi</li>
     * <li>drawable-xxhdpi</li>
     * <li>drawable-xxxhdpi</li>
     * </ul>
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/res")
    private File to;

    /**
     * Create a drawable-density directory when no directory exists for the
     * given qualifiers.<br/>
     * If set to false, the plugin will generate the drawable in the best
     * matching directory :
     * <ul>
     * <li>match all of the qualifiers</li>
     * <li>no other matching directory with less qualifiers</li>
     * </ul>
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "true")
    private boolean createMissingDirectories;

    /**
     * Enumeration of desired target densities.<br/>
     * If no density specified, PNG are only generated to existing directories.<br/>
     * If at least one density is specified, PNG are only generated in matching
     * directories.
     * 
     * @since 1.0.0
     */
    @Parameter
    private Density[] targetedDensities;

    /**
     * Use alternatives names for PNG resources<br/>
     * <dl>
     * <dt>Key</dt>
     * <dd>original svg name (without density prefix)</dd>
     * <dt>Value</dt>
     * <dd>target name</dd>
     * </dl>
     * 
     * @since 1.0.0
     */
    @Parameter
    private Map<String, String> rename;

    /**
     * Density for drawable directories without density qualifier
     * 
     * @since 1.0.0
     * @see Density
     */
    @Parameter(defaultValue = "mdpi")
    private Density fallbackDensity;

    /**
     * Name of the input file to use to generate a 512x512 high resolution
     * Google Play icon
     * 
     * @since 1.0.0
     */
    @Parameter
    private String highResIcon;

    /**
     * Path to the 9-patch drawable configuration file.
     * 
     * @since 1.0.0
     */
    @Parameter
    private File ninePatchConfig;

    /**
     * Path to the <strong>.svgmask</strong> directory.<br/>
     * The {@link SvgDrawableMavenPlugin#from} directory will be use if not specified.
     * 
     * @since 1.1.0
     */
    @Parameter
    private File svgMaskDirectory;

    /**
     * Path to a directory referencing additional svg resources to be taken in
     * account for masking.<br/>
     * The {@link SvgDrawableMavenPlugin#from} directory will be use if not specified.
     * 
     * @since 1.0.0
     */
    @Parameter
    private File svgMaskResourcesDirectory;

    /**
     * Path to the directory where masked svg files are generated.<br/>
     * "target/generated-svg"
     * 
     * @since 1.0.0
     */
    @Parameter(readonly = true, defaultValue = "${project.build.directory}/generated-svg")
    private File svgMaskedSvgOutputDirectory;

    /**
     * If set to true a mask combination will be ignored when a
     * <strong>.svgmask</strong> use the same <strong>.svg<strong> resources in
     * at least two different &lt;image&gt; tags.
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "true")
    private boolean useSameSvgOnlyOnceInMask;

    /**
     * Override existing generated resources.<br/>
     * It's recommended to use {@link OverrideMode#always} for tests and
     * production releases.
     * 
     * @since 1.0.0
     * @see OverrideMode
     */
    @Parameter(defaultValue = "always", alias = "override")
    private OverrideMode overrideMode;

    /**
     * <p>
     * <strong>USE WITH CAUTION</strong><br/>
     * You'll more likely take time to set desired width and height properly
     * </p>
     * 
     * When &lt;SVG&gt; attributes "x", "y", "width" and "height" are not
     * present defines which element are taken in account to compute the Area Of
     * Interest of the image. The plugin will output a WARNING log if no width
     * or height are specified within the &lt;svg&gt; element.
     * <dl>
     * <dt>all</dt>
     * <dd>This includes primitive paint, filtering, clipping and masking.</dd>
     * <dt>sensitive</dt>
     * <dd>This includes the stroked area but does not include the effects of clipping, masking or filtering.</dd>
     * <dt>geometry</dt>
     * <dd>This excludes any clipping, masking, filtering or stroking.</dd>
     * <dt>primitive</dt>
     * <dd>This is the painted region of fill <u>and</u> stroke but does not account for clipping, masking or filtering.</dd>
     * </dl>
     * 
     * @since 1.0.0
     * @see BoundsType
     */
    @Parameter(defaultValue = "sensitive")
    private BoundsType svgBoundsType;

    /**
     * The format for the generated images.
     * <ul>
     * <li>PNG</li>
     * <li>JPG</li>
     * </ul>
     * 
     * @since 1.0.0
     * @see OutputFormat
     */
    @Parameter(defaultValue = "PNG")
    private OutputFormat outputFormat;

    /**
     * The quality for the JPG output format.
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "85")
    private int jpgQuality;

    /**
     * The background color to use when {@link OutputFormat#JPG} is specified.<br/>
     * Default value is 0xFFFFFFFF (white)
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "-1")
    private int jpgBackgroundColor;

    public void execute() {
        final SvgDrawablePlugin.Parameters parameters = new SvgDrawablePlugin.Parameters();
        parameters.from = from;
        parameters.to = to;
        parameters.createMissingDirectories = createMissingDirectories;
        parameters.overrideMode = overrideMode;
        parameters.targetedDensities = targetedDensities;
        parameters.rename = rename;
        parameters.fallbackDensity = fallbackDensity;
        parameters.highResIcon = highResIcon;
        parameters.ninePatchConfig = ninePatchConfig;
        parameters.svgMaskDirectory = svgMaskDirectory;
        parameters.svgMaskResourcesDirectory = svgMaskResourcesDirectory;
        parameters.svgMaskedSvgOutputDirectory = svgMaskedSvgOutputDirectory;
        parameters.useSameSvgOnlyOnceInMask = useSameSvgOnlyOnceInMask;
        parameters.outputFormat = outputFormat;
        parameters.jpgQuality = jpgQuality;
        parameters.jpgBackgroundColor = jpgBackgroundColor;
        parameters.svgBoundsType = svgBoundsType;
        final SvgDrawablePlugin plugin = new SvgDrawablePlugin(parameters, new MavenLogger(getLog()));
        plugin.execute();
    }

}
