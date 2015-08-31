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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.avianey.androidsvgdrawable.NinePatch.Zone;
import fr.avianey.androidsvgdrawable.util.Log;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import static fr.avianey.androidsvgdrawable.util.Constants.MM_PER_INCH;
import static java.awt.Color.BLACK;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.*;
import static org.apache.batik.transcoder.image.ImageTranscoder.KEY_BACKGROUND_COLOR;
import static org.apache.batik.transcoder.image.JPEGTranscoder.KEY_QUALITY;

/**
 * Generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawablePlugin {

    public interface Parameters {

        Integer DEFAULT_JPG_BACKGROUND_COLOR = -1;

        Integer DEFAULT_JPG_QUALITY = 85;
        OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
        OutputType DEFAULT_OUTPUT_TYPE = OutputType.drawable;
        BoundsType DEFAULT_BOUNDS_TYPE = BoundsType.sensitive;
        OverrideMode DEFAULT_OVERRIDE_MODE = OverrideMode.always;
        Boolean DEFAULT_CREATE_MISSING_DIRECTORIES = true;
        File getFrom();

        File getTo();

        boolean isCreateMissingDirectories();

        OverrideMode getOverrideMode();

        Density.Value[] getTargetedDensities();

        File getNinePatchConfig();

        File getSvgMaskDirectory();

        File getSvgMaskResourcesDirectory();

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
        // validating target densities specified in pom.xml
        // un-targeted densities will be ignored except for the fallback density if specified
        final Set<Density.Value> targetDensities = new HashSet<>(Arrays.asList(parameters.getTargetedDensities()));
        if (targetDensities.isEmpty()) {
            targetDensities.addAll(EnumSet.allOf(Density.Value.class));
        }
        getLog().info("Targeted densities : " + Joiner.on(", ").join(targetDensities));

        /********************************
         * Load NinePatch configuration *
         ********************************/

        NinePatchMap ninePatchMap = new NinePatchMap();
        if (parameters.getNinePatchConfig() != null && parameters.getNinePatchConfig().isFile()) {
            getLog().info("Loading NinePatch configuration file " + parameters.getNinePatchConfig().getAbsolutePath());
            try (final Reader reader = new FileReader(parameters.getNinePatchConfig())) {
                Type t = new TypeToken<Set<NinePatch>>(){}.getType();
                Set<NinePatch> ninePathSet = (Set<NinePatch>) (new GsonBuilder().create().fromJson(reader, t));
                ninePatchMap = NinePatch.init(ninePathSet);
            } catch (IOException e) {
                getLog().error(e);
            }
        } else {
            getLog().info("No NinePatch configuration file specified");
        }

        /*****************************
         * List input svg to convert *
         *****************************/

        getLog().info("Listing SVG files in " + parameters.getFrom().getAbsolutePath());
        final Collection<QualifiedResource> svgToConvert = listQualifiedResources(parameters.getFrom(), "svg");
        getLog().info("SVG files : " + Joiner.on(", ").join(svgToConvert));

        /*****************************
         * List input SVGMASK to use *
         *****************************/

        File svgMaskDirectory = parameters.getSvgMaskDirectory();
        File svgMaskResourcesDirectory = parameters.getSvgMaskResourcesDirectory();
        if (svgMaskDirectory == null) {
            svgMaskDirectory = parameters.getFrom();
        }
        if (svgMaskResourcesDirectory == null) {
            svgMaskResourcesDirectory = svgMaskDirectory;
        }
        getLog().info("Listing SVGMASK files in " + svgMaskDirectory.getAbsolutePath());
        final Collection<QualifiedResource> svgMasks = listQualifiedResources(svgMaskDirectory, "svgmask");
        final Collection<QualifiedResource> svgMaskResources = new ArrayList<>();
        getLog().info("SVGMASK files : " + Joiner.on(", ").join(svgMasks));
        if (!svgMasks.isEmpty()) {
            // list masked resources
            if (svgMaskResourcesDirectory.equals(parameters.getFrom())) {
                svgMaskResources.addAll(svgToConvert);
            } else {
                svgMaskResources.addAll(listQualifiedResources(svgMaskResourcesDirectory, "svg"));
            }
            getLog().info("SVG files to mask : " + Joiner.on(", ").join(svgMaskResources));
            // generate masked svg
            for (QualifiedResource maskFile : svgMasks) {
                getLog().info("Generating masked files for " + maskFile);
                try {
                	Collection<QualifiedResource> generatedResources = new SvgMask(maskFile).generatesMaskedResources(
                            qualifiedSVGResourceFactory,
                	        parameters.getSvgMaskedSvgOutputDirectory(), svgMaskResources,
                	        parameters.isUseSameSvgOnlyOnceInMask(), parameters.getOverrideMode());
                    getLog().debug("+ " + Joiner.on(", ").join(generatedResources));
                    svgToConvert.addAll(generatedResources);
                } catch (XPathExpressionException | TransformerException | ParserConfigurationException | SAXException | IOException e) {
                    getLog().error(e);
                }
            }
        } else {
            getLog().info("No SVGMASK file found.");
        }

        /*********************************
         * Create svg in res/* folder(s) *
         *********************************/

        for (QualifiedResource svg : svgToConvert) {
            try {
                getLog().info("Transcoding " + FilenameUtils.getName(svg.getAbsolutePath()) + " to targeted densities");
                // for each target density :
                // - find matching destinations :
                //   - matches all extra qualifiers
                //   - no other output with a qualifiers set that is a subset of this output
                // - if no match, create required directories
                for (Density.Value d : targetDensities) {
                    NinePatch ninePatch = ninePatchMap.getBestMatch(svg);
                    File destination = svg.getOutputFor(d, parameters.getTo(), ninePatch == null ? parameters.getOutputType() : OutputType.drawable);
                    if (!destination.exists() && parameters.isCreateMissingDirectories()) {
                        destination.mkdirs();
                    }
                    if (destination.exists()) {
                        getLog().debug("Transcoding " + svg.getName() + " to " + destination.getName());
                        transcode(svg, d, destination, ninePatch);
                    } else {
                        getLog().info("Qualified output " + destination.getName() + " does not exists. " +
                        		"Set 'createMissingDirectories' to true if you want it to be created if missing...");
                    }
                }
            } catch (Exception e) {
                getLog().error("Error while converting " + svg, e);
			}
        }

    }

    /**
     * Given it's bounds, transcodes a svg file to a raster image for the desired density
     * @param svg
     * @param targetDensity
     * @param destination
     * @throws IOException
     * @throws TranscoderException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    // TODO no need for bounds when QualifiedSVGResource
    @VisibleForTesting
    void transcode(QualifiedResource svg, Density.Value targetDensity, File destination, NinePatch ninePatch) throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
        final Rectangle outputBounds = svg.getScaledBounds(targetDensity);
        if (getLog().isDebugEnabled()) {
            getLog().debug("+ target dimensions [width=" + outputBounds.getWidth() + " - height=" + outputBounds.getHeight() +"]");
        }
        ImageTranscoder t = parameters.getOutputFormat().getTranscoderClass().newInstance();
        if (t instanceof JPEGTranscoder) {
        	// custom jpg hints
	        t.addTranscodingHint(KEY_QUALITY, min(1, max(0, parameters.getJpgQuality() / 100f)));
	        t.addTranscodingHint(KEY_BACKGROUND_COLOR, new Color(parameters.getJpgBackgroundColor()));
        }
        t.addTranscodingHint(KEY_WIDTH, new Float(outputBounds.getWidth()));
        t.addTranscodingHint(KEY_HEIGHT, new Float(outputBounds.getHeight()));
        TranscoderInput input = new TranscoderInput(new FileInputStream(svg)); // TODO close

        // final name
        final String finalName = new StringBuilder(destination.getAbsolutePath())
            .append(System.getProperty("file.separator"))
            .append(svg.getName())
            .append(ninePatch != null && parameters.getOutputFormat().hasNinePatchSupport() ? ".9" : "")
            .append(".")
            .append(parameters.getOutputFormat().name().toLowerCase())
            .toString();

        final File finalFile = new File(finalName);

        if (parameters.getOverrideMode().shouldOverride(svg, finalFile, parameters.getNinePatchConfig())) {
        	// unit conversion for size not in pixel (in, mm, ...)
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
            getLog().debug(finalName + " already exists and is up to date... skiping generation!");
            getLog().debug("+ " + finalName + " last modified on " + new File(finalName).lastModified());
            getLog().debug("+ " + svg.getAbsolutePath() + " last modified on " + svg.lastModified());
            if (ninePatch != null && parameters.getNinePatchConfig() != null /* for tests */) {
                getLog().debug("+ " + parameters.getNinePatchConfig().getAbsolutePath() + " last modified on " + parameters.getNinePatchConfig().lastModified());
            }
        }
    }

    /**
     * Draw the stretch and content area defined by the {@link NinePatch} around the given image
     * @param is
     * @param finalName
     * @param ninePatch
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

        ImageIO.write(ninePatchImage, "png", new File(finalName));
    }

    /**
     * List {@link QualifiedResource} from an input directory.
     * @param from
     * @param extension
     * @return
     */
    // TODO test
    private Collection<QualifiedResource> listQualifiedResources(final File from, final String extension) {
        Preconditions.checkNotNull(extension);
        final Collection<QualifiedResource> resources = new ArrayList<>();
        if (from.isDirectory()) {
            for (File f : from.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && extension.equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                        try {
                            resources.add(qualifiedSVGResourceFactory.fromSVGFile(file));
                            return true;
                        } catch (Exception e) {
                            getLog().error(e);
                        }
                        getLog().warn("Invalid " + extension + " file : " + file.getAbsolutePath());
                    } else {
                        getLog().debug("+ skipping " + file.getAbsolutePath());
                    }
                    return false;
                }
            })) {
                // log matching svgmask inputs
                getLog().debug("+ found " + extension + " file : " + f.getAbsolutePath());
            }
        } else {
            throw new RuntimeException(from.getAbsolutePath() + " is not a directory");
        }
        return resources;
    }


    @VisibleForTesting
    QualifiedSVGResourceFactory getQualifiedSVGResourceFactory() {
        return qualifiedSVGResourceFactory;
    }

}
