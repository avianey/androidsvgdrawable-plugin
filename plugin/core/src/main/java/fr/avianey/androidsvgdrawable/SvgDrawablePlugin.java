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
package fr.avianey.androidsvgdrawable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.UnitProcessor;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;
import org.xml.sax.SAXException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.avianey.androidsvgdrawable.NinePatch.Zone;
import fr.avianey.androidsvgdrawable.batik.DensityAwareUserAgent;
import fr.avianey.androidsvgdrawable.util.Constants;
import fr.avianey.androidsvgdrawable.util.Log;

/**
 * Generates drawable from Scalable Vector Graphics (SVG) files.
 *
 * @author antoine vianey
 */
public class SvgDrawablePlugin {
    
    public static interface Parameters {
        
        public static final Integer DEFAULT_JPG_BACKGROUND_COLOR = -1;
        public static final Integer DEFAULT_JPG_QUALITY = 85;
        public static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
        public static final OutputType DEFAULT_OUTPUT_TYPE = OutputType.drawable;
        public static final BoundsType DEFAULT_BOUNDS_TYPE = BoundsType.sensitive;
        public static final OverrideMode DEFAULT_OVERRIDE_MODE = OverrideMode.always;
        public static final Boolean DEFAULT_CREATE_MISSING_DIRECTORIES = true;

        File getFrom();

        File getTo();

        boolean isCreateMissingDirectories();

        OverrideMode getOverrideMode();

        Density[] getTargetedDensities();

        Map<String, String> getRename();

        String getHighResIcon();

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
    
    public SvgDrawablePlugin(final Parameters parameters, final Log log) {
        this.parameters = parameters;
        this.log = log;
    }
    
    private Log getLog() {
        return this.log;
    }

    public void execute() {
        // validating target densities specified in pom.xml
        // untargetted densities will be ignored except for the fallback density if specified
        final Set<Density> targetDensities = new HashSet<>(Arrays.asList(parameters.getTargetedDensities()));
        if (targetDensities.isEmpty()) {
            targetDensities.addAll(EnumSet.allOf(Density.class));
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
         * List input svgmask to use *
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

        QualifiedResource highResIcon = null;
        Rectangle highResIconBounds = null;
        
        /*********************************
         * Create svg in res/* folder(s) *
         *********************************/
        
        for (QualifiedResource svg : svgToConvert) {
            try {
                getLog().info("Transcoding " + FilenameUtils.getName(svg.getAbsolutePath()) + " to targeted densities");
                Rectangle bounds = extractSVGBounds(svg);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("+ source dimensions [width=" + bounds.getWidth() + " - height=" + bounds.getHeight() + "]");
                }
                if (parameters.getHighResIcon() != null && parameters.getHighResIcon().equals(svg.getName())) {
                    highResIcon = svg;
                    highResIconBounds = bounds;
                }
                // for each target density :
                // - find matching destinations :
                //   - matches all extra qualifiers
                //   - no other output with a qualifiers set that is a subset of this output
                // - if no match, create required directories
                for (Density d : targetDensities) {
                    NinePatch ninePatch = ninePatchMap.getBestMatch(svg);
                    File destination = svg.getOutputFor(d, parameters.getTo(), ninePatch == null ? parameters.getOutputType() : OutputType.drawable);
                    if (!destination.exists() && parameters.isCreateMissingDirectories()) {
                        destination.mkdirs();
                    }
                    if (destination.exists()) {
                        getLog().debug("Transcoding " + svg.getName() + " to " + destination.getName());
                        transcode(svg, d, bounds, destination, ninePatch);
                    } else {
                        getLog().info("Qualified output " + destination.getName() + " does not exists. " +
                        		"Set 'createMissingDirectories' to true if you want it to be created if missing...");
                    }
                }
            } catch (IOException | TranscoderException | InstantiationException | IllegalAccessException e) {
                getLog().error(e);
			}
        }
        
        /******************************************
         * Generates the play store high res icon *
         ******************************************/
        
        if (highResIcon != null) {
            try {
                // TODO : add a garbage density (NO_DENSITY) for the highResIcon
            	// TODO : make highResIcon size configurable
            	// TODO : generates other play store assets
                // TODO : parameterized SIZE
                getLog().info("Generating high resolution icon");
                transcode(highResIcon, Density.mdpi, highResIconBounds, new File("."), 512, 512, null);
            } catch (IOException | TranscoderException | InstantiationException | IllegalAccessException e) {
                getLog().error(e);
			}
        }
    }

    /**
     * Extract the viewbox of the input SVG
     * @param svg
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    @VisibleForTesting
    Rectangle extractSVGBounds(QualifiedResource svg) throws MalformedURLException, IOException {
    	// check <svg> attributes first : x, y, width, height
    	SVGDocument svgDocument = getSVGDocument(svg);
    	SVGSVGElement svgElement = svgDocument.getRootElement();
    	if (svgElement.getAttributeNode("width") != null && svgElement.getAttribute("height") != null) {

            UserAgent userAgent = new DensityAwareUserAgent(svg.getDensity().getDpi());
            UnitProcessor.Context context = org.apache.batik.bridge.UnitProcessor.createContext(
            		new BridgeContext(userAgent), svgElement);
            
    		float width = svgLengthInPixels(svgElement.getWidth().getBaseVal(), context);
    		float height = svgLengthInPixels(svgElement.getHeight().getBaseVal(), context);
    		float x = 0;
    		float y = 0;
    		// check x and y attributes
    		if (svgElement.getX() != null && svgElement.getX().getBaseVal() != null) {
    			x = svgLengthInPixels(svgElement.getX().getBaseVal(), context);
    		}
    		if (svgElement.getY() != null && svgElement.getY().getBaseVal() != null) {
    			y = svgLengthInPixels(svgElement.getY().getBaseVal(), context);
    		}
    		
    		return new Rectangle((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(width), (int) Math.ceil(height));
    	}
    	
    	// use computed bounds
    	getLog().warn("Take time to fix desired width and height attributes of the root <svg> node for this file... " +
    			"ROI will be computed by magic using Batik " + parameters.getSvgBoundsType().name() + " bounds");
    	return parameters.getSvgBoundsType().getBounds(getGraphicsNode(svgDocument, svg.getDensity().getDpi()));
    }
    
    /**
     * Convert an {@link SVGLength} to a value in {@link SVGLength#SVG_LENGTHTYPE_PX}
     * @param length
     * @param context
     * @return
     */
    private float svgLengthInPixels(SVGLength length, UnitProcessor.Context context) {
    	return UnitProcessor.svgToUserSpace(length.getValueAsString(), "px", UnitProcessor.OTHER_LENGTH, context);
    }
    
    /**
     * Return the {@link GraphicsNode} of the {@link SVGDocument}
     * @param svgDocument
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private GraphicsNode getGraphicsNode(SVGDocument svgDocument, int dpi) throws MalformedURLException, IOException {
        UserAgent userAgent = new DensityAwareUserAgent(dpi);
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        GVTBuilder builder = new GVTBuilder();
        GraphicsNode rootGN = builder.build(ctx, svgDocument);
        return rootGN;
    }
    
    /**
     * Return the {@link SVGDocument} of the SVG {@link QualifiedResource}
     * @param svg
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    @VisibleForTesting
    SVGDocument getSVGDocument(QualifiedResource svg) throws MalformedURLException, IOException {
    	String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        return (SVGDocument) f.createDocument(svg.toURI().toURL().toString());
    }
    
    /**
     * Given it's bounds, transcodes a svg file to a raster image for the desired density
     * @param svg
     * @param targetDensity 
     * @param bounds
     * @param destination
     * @throws IOException
     * @throws TranscoderException
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @VisibleForTesting
    void transcode(QualifiedResource svg, Density targetDensity, Rectangle bounds, File destination, NinePatch ninePatch) throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
        transcode(svg, targetDensity, bounds, destination, 
                new Float(bounds.getWidth() * svg.getDensity().ratio(targetDensity)), 
                new Float(bounds.getHeight() * svg.getDensity().ratio(targetDensity)),
                ninePatch);
    }
    
    /**
     * Given a desired width and height, transcodes a svg file to a raster image for the desired density
     * @param svg
     * @param targetDensity 
     * @param bounds
     * @param dest
     * @param targetWidth
     * @param targetHeight
     * @throws IOException
     * @throws TranscoderException
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void transcode(QualifiedResource svg, Density targetDensity, Rectangle bounds, File dest, float targetWidth, float targetHeight, NinePatch ninePatch) throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
        final Float width = Math.max(new Float(Math.floor(targetWidth)), 1);
        final Float height = Math.max(new Float(Math.floor(targetHeight)), 1);
        if (getLog().isDebugEnabled()) {
            getLog().debug("+ target dimensions [width=" + width + " - length=" + height +"]");
        }
        ImageTranscoder t = parameters.getOutputFormat().getTranscoderClass().newInstance();
        if (t instanceof JPEGTranscoder) {
        	// custom jpg hints
	        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, Math.min(1, Math.max(0, parameters.getJpgQuality() / 100f)));
	        t.addTranscodingHint(JPEGTranscoder.KEY_BACKGROUND_COLOR, new Color(parameters.getJpgBackgroundColor()));
        }
        t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, width);
        t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, height);
        TranscoderInput input = new TranscoderInput(svg.toURI().toURL().toString());
        String outputName = svg.getName();
        if (parameters.getRename() != null && parameters.getRename().containsKey(outputName)) {
            if (parameters.getRename().get(outputName) != null && parameters.getRename().get(outputName).matches("\\w+")) {
                outputName = parameters.getRename().get(outputName);
            } else {
                getLog().warn(parameters.getRename().get(outputName) + " is not a valid replacment name for " + outputName);
            }
        }
        
        // final name
        final String finalName = new StringBuilder(dest.getAbsolutePath())
            .append(System.getProperty("file.separator"))
            .append(outputName)
            .append(ninePatch != null && parameters.getOutputFormat().hasNinePatchSupport() ? ".9" : "")
            .append(".")
            .append(parameters.getOutputFormat().name().toLowerCase())
            .toString();
        
        final File finalFile = new File(finalName);
        
        if (parameters.getOverrideMode().shouldOverride(svg, finalFile, parameters.getNinePatchConfig())) {
        	// unit conversion for size not in pixel
        	t.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(Constants.MM_PER_INCH / svg.getDensity().getDpi()));
        	// set the ROI
        	t.addTranscodingHint(ImageTranscoder.KEY_AOI, bounds);
        	
            if (ninePatch == null || !parameters.getOutputFormat().hasNinePatchSupport()) {
            	if (ninePatch != null) {
            		getLog().warn("skipping the nine-patch configuration for the JPG output format !!!");
            	}
                // write file directly
                OutputStream ostream = new FileOutputStream(finalName);
                TranscoderOutput output = new TranscoderOutput(ostream);
                t.transcode(input, output);
                ostream.flush();
                ostream.close();
            } else {
                // write in memory
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(ostream);
                t.transcode(input, output);
                // fill the patch
                ostream.flush();
                InputStream istream = new ByteArrayInputStream(ostream.toByteArray());
                ostream.close();
                ostream = null;
                toNinePatch(istream, finalName, ninePatch, svg.getDensity().ratio(targetDensity));
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
     * @param ratio
     * @throws IOException
     */
    private void toNinePatch(final InputStream is, final String finalName, final NinePatch ninePatch, final double ratio) throws IOException {
        BufferedImage image = ImageIO.read(is);
        final int w = image.getWidth();
        final int h = image.getHeight();
        BufferedImage ninePatchImage = new BufferedImage(
                w + 2, 
                h + 2, 
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = ninePatchImage.getGraphics();
        g.drawImage(image, 1, 1, null);
        
        // draw patch
        g.setColor(Color.BLACK);
        
        Zone stretch = ninePatch.getStretch();
        Zone content = ninePatch.getContent();
        
        if (stretch.getX() == null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("+ ninepatch stretch(x) [start=0 - size=" + w + "]");
            }
        	g.fillRect(1, 0, w, 1);
        } else {
	        for (int[] seg : stretch.getX()) {
	            final int start = NinePatch.start(seg[0], seg[1], w, ratio);
	            final int size = NinePatch.size(seg[0], seg[1], w, ratio);
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
	            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
	            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
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
	            final int start = NinePatch.start(seg[0], seg[1], w, ratio);
	            final int size = NinePatch.size(seg[0], seg[1], w, ratio);
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
	            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
	            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
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
    private Collection<QualifiedResource> listQualifiedResources(final File from, final String extension) {
        Preconditions.checkNotNull(extension);
        final Collection<QualifiedResource> resources = new ArrayList<>();
        if (from.isDirectory()) {
            for (File f : from.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && extension.equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                        try {
                            resources.add(QualifiedResource.fromFile(file));
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
    
}
