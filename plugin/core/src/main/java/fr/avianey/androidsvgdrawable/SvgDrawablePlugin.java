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
package fr.avianey.androidsvgdrawable;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.avianey.androidsvgdrawable.NinePatch.Zone;
import fr.avianey.androidsvgdrawable.util.Log;
import fr.avianey.androidsvgdrawable.util.QualifiedResourceFilter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkNotNull;
import static fr.avianey.androidsvgdrawable.util.Constants.MM_PER_INCH;
import static java.awt.Color.BLACK;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.*;
import static org.apache.batik.transcoder.image.ImageTranscoder.KEY_BACKGROUND_COLOR;
import static org.apache.batik.transcoder.image.JPEGTranscoder.KEY_QUALITY;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.TrueFileFilter.INSTANCE;

/**
 * Generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawablePlugin {

    private static final String SVG_EXTENSION = "svg";
    private static final String SVGMASK_EXTENSION = "svgmask";
    private static final String PNG_EXTENSION = "png";

    public interface Parameters {

        Integer DEFAULT_JPG_BACKGROUND_COLOR = -1;
        Integer DEFAULT_JPG_QUALITY = 85;
        OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
        OutputType DEFAULT_OUTPUT_TYPE = OutputType.drawable;
        BoundsType DEFAULT_BOUNDS_TYPE = BoundsType.sensitive;
        OverwriteMode DEFAULT_OVERRIDE_MODE = OverwriteMode.always;
        Boolean DEFAULT_CREATE_MISSING_DIRECTORIES = true;

        Iterable<File> getFiles();

        File getTo();

        boolean isCreateMissingDirectories();

        OverwriteMode getOverwriteMode();

        @Nullable
        Density.Value[] getTargetedDensities();

        @Nullable
        Density.Value getNoDpiDensity();

        @Nullable
        File getNinePatchConfig();

        Iterable<File> getSvgMaskFiles();

        Iterable<File> getSvgMaskResourceFiles();

        File getSvgMaskedSvgOutputDirectory();

        boolean isUseSameSvgOnlyOnceInMask();

        OutputFormat getOutputFormat();

        OutputType getOutputType();

        int getJpgQuality();

        int getJpgBackgroundColor();

        BoundsType getSvgBoundsType();

    }

    // log
    private final Log log;
    private final Parameters parameters;
    private final QualifiedSVGResourceFactory qualifiedSVGResourceFactory;

    public SvgDrawablePlugin(final Parameters parameters, final Log log) {
        this.parameters = parameters;
        this.log = log;
        this.qualifiedSVGResourceFactory = new QualifiedSVGResourceFactory(getLog(), parameters.getSvgBoundsType());
    }

    private Log getLog() {
        return this.log;
    }

    public void execute() {

        /**********************
         * Targeted densities *
         **********************/

        // validating targeted densities
        // un-targeted densities will be ignored
        // if the output type is 'raw' then targeted densities are ignored
        final Set<Density.Value> targetDensities = EnumSet.noneOf(Density.Value.class);
        if (parameters.getOutputType() != OutputType.raw) {
            if (parameters.getTargetedDensities() != null) {
                targetDensities.addAll(asList(parameters.getTargetedDensities()));
            }
            if (targetDensities.isEmpty()) {
                targetDensities.addAll(EnumSet.allOf(Density.Value.class));
            }
            getLog().info("Targeted densities : " + on(", ").join(targetDensities));
        } else {
            getLog().info("Ignoring targeted densities for 'raw' output type...");
        }

        /********************************
         * Load NinePatch configuration *
         ********************************/

        NinePatchMap ninePatchMap = new NinePatchMap();
        if (parameters.getNinePatchConfig() != null && parameters.getNinePatchConfig().isFile()) {
            if (parameters.getOutputType() == OutputType.mipmap) {
                getLog().warn("NinePatch is not supported by the Android platform. " +
                        "Skipping NinePatch configuration file " + parameters.getNinePatchConfig().getAbsolutePath());
            } else {
                getLog().info("Loading NinePatch configuration file " + parameters.getNinePatchConfig().getAbsolutePath());
                try (final Reader reader = new FileReader(parameters.getNinePatchConfig())) {
                    Type t = new TypeToken<Set<NinePatch>>() {
                    }.getType();
                    Set<NinePatch> ninePathSet = new GsonBuilder().create().fromJson(reader, t);
                    ninePatchMap = NinePatch.init(ninePathSet);
                } catch (IOException e) {
                    getLog().error("Error loading NinePatch configuration file", e);
                }
            }
        } else {
            getLog().info("No NinePatch configuration file specified");
        }

        /*****************************
         * List input SVG to convert *
         *****************************/

        getLog().info("Listing SVG files : " + on(", ").join(parameters.getFiles()));
        final Collection<QualifiedResource> svgToConvert = listQualifiedResources(parameters.getFiles(), SVG_EXTENSION);
        getLog().info("SVG files found : " + on(", ").join(svgToConvert));

        /*****************************
         * List input SVGMASK to use *
         *****************************/

        Iterable<File> svgMaskFiles = parameters.getSvgMaskFiles() == null ? parameters.getFiles() : parameters.getSvgMaskFiles();

        getLog().info("Listing SVGMASK files : " + on(", ").join(svgMaskFiles));
        final Collection<QualifiedResource> svgMasks = listQualifiedResources(svgMaskFiles, SVGMASK_EXTENSION);
        getLog().info("SVGMASK files found : " + on(", ").join(svgMasks));
        if (!svgMasks.isEmpty()) {
            // list resources to mask
            Iterable<File> svgMaskedResourcesFiles = parameters.getSvgMaskResourceFiles() == null ? svgMaskFiles : parameters.getSvgMaskResourceFiles();
            getLog().info("Listing SVG files to mask : " + on(", ").join(svgMaskedResourcesFiles));
            final Collection<QualifiedResource> svgMaskResources = listQualifiedResources(svgMaskedResourcesFiles, SVG_EXTENSION);
            getLog().info("SVG files to mask found : " + on(", ").join(svgMasks));
            // generate masked svg
            svgToConvert.addAll(generateMaskedSvg(svgMasks, svgMaskResources));
        } else {
            getLog().info("No SVGMASK file found.");
        }

        /*********************************
         * Create svg in res/* folder(s) *
         *********************************/

        for (QualifiedResource svg : svgToConvert) {
            try {
                getLog().info("Transcoding " + FilenameUtils.getName(svg.getAbsolutePath()) + " to targeted densities");
                Collection<Density.Value> _targetedDensities = parameters.getOutputType() == OutputType.raw ?
                        singletonList(svg.getDensity().getValue()) :
                        targetDensities;
                for (Density.Value d : _targetedDensities) {
                    NinePatch ninePatch = ninePatchMap.getBestMatch(svg);
                    File destination = parameters.getOutputType() == OutputType.raw ?
                            parameters.getTo() :
                            svg.getOutputFor(d, parameters.getTo(), parameters.getOutputType(), parameters.getNoDpiDensity());
                    if (!destination.exists() && parameters.isCreateMissingDirectories()) {
                        destination.mkdirs();
                    }
                    if (destination.exists()) {
                        getLog().debug("+ transcoding " + svg.getName() + " into " + destination.getName());
                        transcode(svg, d, destination, ninePatch);
                    } else {
                        getLog().info("Qualified output directory " + destination.getName() + " does not exists. " +
                        		"Set 'createMissingDirectories' to true if you want it to be created when missing...");
                    }
                }
            } catch (Exception e) {
                getLog().error("Error while converting " + svg, e);
			}
        }

    }

    /**
     * Generate masked SVG files to be handle like regular SVG files
     * @param svgMasks SVGMASK files
     * @param svgMaskResources SVG files to mask
     * @return masked qualified resources
     */
    private Collection<QualifiedResource> generateMaskedSvg(Collection<QualifiedResource> svgMasks, Collection<QualifiedResource> svgMaskResources) {
        Collection<QualifiedResource> maskedFiles = new ArrayList<>();
        for (QualifiedResource maskFile : svgMasks) {
            getLog().info("Generating masked files for " + maskFile);
            try {
                Collection<QualifiedResource> generatedResources = new SvgMask(maskFile).generatesMaskedResources(
                        qualifiedSVGResourceFactory,
                        parameters.getSvgMaskedSvgOutputDirectory(), svgMaskResources,
                        parameters.isUseSameSvgOnlyOnceInMask(), parameters.getOverwriteMode());
                if (!generatedResources.isEmpty()) {
                    getLog().debug("+ " + on(", ").join(generatedResources));
                } else {
                    getLog().debug("+ no matching masked resource file was found");
                }
                maskedFiles.addAll(generatedResources);
            } catch (XPathExpressionException | TransformerException | ParserConfigurationException | SAXException | IOException e) {
                getLog().error(e);
            }
        }
        return maskedFiles;
    }

    /**
     * Given it's bounds, transcodes a svg file to a raster image for the desired density
     * @param svg the svg to transcode
     * @param targetDensity the density to transcode to
     * @param destination where the transcoded files should be generated
     * @param ninePatch the nine patch configuration for the svg to transcode (if any)
     * @throws IOException
     * @throws TranscoderException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @VisibleForTesting
    void transcode(QualifiedResource svg, Density.Value targetDensity, File destination, @Nullable NinePatch ninePatch) throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
        final Rectangle outputBounds = svg.getScaledBounds(targetDensity);
        if (getLog().isDebugEnabled()) {
            getLog().debug("+ target dimensions [width=" + outputBounds.getWidth() + " - height=" + outputBounds.getHeight() +"]");
        }
        try (FileInputStream svgInputStream = new FileInputStream(svg)) {
            TranscoderInput input = new TranscoderInput(svgInputStream);

            // final name
            final String finalName = new StringBuilder(destination.getAbsolutePath())                              //
                    .append(System.getProperty("file.separator"))                                                  //
                    .append(svg.getName())                                                                         //
                    .append(ninePatch != null && parameters.getOutputFormat().hasNinePatchSupport() ? ".9" : "")   //
                    .append(".")                                                                                   //
                    .append(parameters.getOutputFormat().name().toLowerCase()).toString();                         //

            final File finalFile = new File(finalName);

            if (parameters.getOverwriteMode().shouldOverride(svg, finalFile, parameters.getNinePatchConfig())) {
                // unit conversion for size not in pixel (in, mm, ...)

                ImageTranscoder t = parameters.getOutputFormat().getTranscoderClass().newInstance();
                if (t instanceof JPEGTranscoder) {
                    // custom jpg hints
                    t.addTranscodingHint(KEY_QUALITY, min(1, max(0, parameters.getJpgQuality() / 100f)));
                    t.addTranscodingHint(KEY_BACKGROUND_COLOR, new Color(parameters.getJpgBackgroundColor()));
                }
                t.addTranscodingHint(KEY_WIDTH, new Float(outputBounds.getWidth()));
                t.addTranscodingHint(KEY_HEIGHT, new Float(outputBounds.getHeight()));
                t.addTranscodingHint(KEY_PIXEL_UNIT_TO_MILLIMETER, MM_PER_INCH / svg.getDensity().getDpi());

                if (ninePatch == null || !parameters.getOutputFormat().hasNinePatchSupport()) {
                    if (ninePatch != null) {
                        getLog().warn("skipping the nine-patch configuration for the JPG output format !!!");
                    }
                    // write file directly
                    OutputStream os = new FileOutputStream(finalName);
                    TranscoderOutput output = new TranscoderOutput(os);
                    t.transcode(input, output);
                    os.flush();
                    os.close();
                } else {
                    // write in memory
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        TranscoderOutput output = new TranscoderOutput(os);
                        t.transcode(input, output);
                        os.flush();
                        try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                            // fill the patch
                            toNinePatch(is, finalName, ninePatch, svg.getBounds(), outputBounds);
                        }
                    }
                }
            } else {
                getLog().debug(finalName + " already exists and is up to date... skipping generation!");
                getLog().debug("+ " + finalName + " last modified on " + new File(finalName).lastModified());
                getLog().debug("+ " + svg.getAbsolutePath() + " last modified on " + svg.lastModified());
                if (ninePatch != null && parameters.getNinePatchConfig() != null /* for tests */) {
                    getLog().debug("+ " + parameters.getNinePatchConfig().getAbsolutePath() + " last modified on " + parameters.getNinePatchConfig().lastModified());
                }
            }
        }
    }

    /**
     * Draw the stretch and content area defined by the {@link NinePatch} around the given image
     * @param is the generated PNG input file
     * @param finalName the targeted filename
     * @param ninePatch the nine patch configuration
     * @param svgBounds original svg bounds
     * @param outputBounds targeted bounds
     * @throws IOException
     */
    private void toNinePatch(final InputStream is, final String finalName, final NinePatch ninePatch, final Rectangle svgBounds, final Rectangle outputBounds) throws IOException {
        BufferedImage image = ImageIO.read(is);
        final double wRatio = outputBounds.getWidth() / svgBounds.getWidth();
        final double hRatio = outputBounds.getHeight() / svgBounds.getHeight();
        final int w = image.getWidth();
        final int h = image.getHeight();
        BufferedImage ninePatchImage = new BufferedImage(
                w + 2,
                h + 2,
                TYPE_INT_ARGB);
        Graphics g = ninePatchImage.getGraphics();
        g.drawImage(image, 1, 1, null);

        // draw patch
        g.setColor(BLACK);

        Zone stretch = ninePatch.getStretch();
        Zone content = ninePatch.getContent();

        if (stretch.getX() == null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("+ ninepatch stretch(x) [start=0 - size=" + w + "]");
            }
        	g.fillRect(1, 0, w, 1);
        } else {
	        for (int[] seg : stretch.getX()) {
	            final int start = NinePatch.start(seg[0], w, wRatio);
	            final int size = NinePatch.size(seg[0], seg[1], w, wRatio);
	            if (getLog().isDebugEnabled()) {
	                getLog().debug("+ ninepatch stretch(x) [start=" + start + " - size=" + size + "]");
	            }
	            g.fillRect(start + 1, 0, size, 1);
	        }
        }

        if (stretch.getY() == null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("+ ninepatch stretch(y) [start=0 - size=" + h + "]");
            }
        	g.fillRect(0, 1, 1, h);
        } else {
	        for (int[] seg : stretch.getY()) {
	            final int start = NinePatch.start(seg[0], h, hRatio);
	            final int size = NinePatch.size(seg[0], seg[1], h, hRatio);
	            if (getLog().isDebugEnabled()) {
	                getLog().debug("+ ninepatch stretch(y) [start=" + start + " - size=" + size + "]");
	            }
	            g.fillRect(0, start + 1, 1, size);
	        }
        }

        if (content.getX() == null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("+ ninepatch content(x) [start=0 - size=" + w + "]");
            }
        	g.fillRect(1, h + 1, w, 1);
        } else {
	        for (int[] seg : content.getX()) {
	            final int start = NinePatch.start(seg[0], w, hRatio);
	            final int size = NinePatch.size(seg[0], seg[1], w, hRatio);
	            if (getLog().isDebugEnabled()) {
	                getLog().debug("+ ninepatch content(x) [start=" + start + " - size=" + size + "]");
	            }
	            g.fillRect(start + 1, h + 1, size, 1);
	        }
        }

        if (content.getY() == null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("+ ninepatch content(y) [start=0 - size=" + h + "]");
            }
        	g.fillRect(w + 1, 1, 1, h);
        } else {
	        for (int[] seg : content.getY()) {
	            final int start = NinePatch.start(seg[0], h, hRatio);
	            final int size = NinePatch.size(seg[0], seg[1], h, hRatio);
	            if (getLog().isDebugEnabled()) {
	                getLog().debug("+ ninepatch content(y) [start=" + start + " - size=" + size + "]");
	            }
	            g.fillRect(w + 1, start + 1, 1, size);
	        }
        }

        ImageIO.write(ninePatchImage, PNG_EXTENSION, new File(finalName));
    }

    /**
     * List {@link QualifiedResource} from various input files / directories.
     * @param files files where to pick svg to convert from
     * @param extension the extension from which qualified resources should be extracted
     * @return qualified resource from the specified files (recursively)
     */
    private Collection<QualifiedResource> listQualifiedResources(final Iterable<File> files, final String extension) {
        checkNotNull(extension);
        QualifiedResourceFilter filter = new QualifiedResourceFilter(getLog(), qualifiedSVGResourceFactory, extension);
        for (File from : files) {
            if (from.isDirectory()) {
                listFiles(from, filter, INSTANCE);
            } else {
                filter.accept(from);
            }
        }
        return filter.filteredResources();
    }

    @VisibleForTesting
    QualifiedSVGResourceFactory getQualifiedSVGResourceFactory() {
        return qualifiedSVGResourceFactory;
    }

}
