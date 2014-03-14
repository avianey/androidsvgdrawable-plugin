package fr.avianey.mojo.androidgendrawable;

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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

import com.google.common.base.Joiner;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.avianey.mojo.androidgendrawable.NinePatch.Zone;
import fr.avianey.mojo.androidgendrawable.batik.DensityAwareUserAgent;
import fr.avianey.mojo.androidgendrawable.util.Constants;

/**
 * Goal which generates drawable from Scalable Vector Graphics (SVG) files.
 */
@Mojo(name = "gen")
public class Gen extends AbstractMojo {
    
    /**
     * Directory of the svg resources to generate drawable from.
     * 
     * @since 1.0.0
     */
	@Parameter(required = true)
    private File from;
    
    /**
     * Location of the Android "./res/drawable(...)" directories :
     * <ul>
     * <li>drawable</li>
     * <li>drawable-hdpi</li>
     * <li>drawable-ldpi</li>
     * <li>drawable-mdpi</li>
     * <li>drawable-xhdpi</li>
     * <li>drawable-xxhdpi</li>
     * </ul>
     * 
     * @since 1.0.0
     */
	@Parameter(defaultValue = "${project.basedir}/res")
    private File to;
    
    /**
     * Create a drawable-density directory when no directory exists for the given qualifiers.<br/>
     * If set to false, the plugin will generate the drawable in the best matching directory :
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
     * If at least one density is specified, PNG are only generated in matching directories.
     * 
     * @since 1.0.0
     */
	@Parameter
    private Set<Density> targetedDensities;

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
     * Name of the input file to use to generate a 512x512 high resolution Google Play icon
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
     * The {@link Gen#from} directory will be use if not specified.
     * 
     * @since 1.1.0
     */
	@Parameter
    private File svgMaskDirectory;

    /**
     * Path to a directory referencing additional svg resources to be taken in account for masking.<br/>
     * The {@link Gen#from} directory will be use if not specified.
     * 
     * @since 1.1.0
     */
	@Parameter
    private File svgMaskResourcesDirectory;

    /**
     * If set to true a mask combination will be ignored when a <strong>.svgmask</strong> use the same 
     * <strong>.svg<strong> resources in at least two different &lt;image&gt; tags.
     * 
     * @since 1.1.0
     */
	@Parameter(defaultValue = "true")
    private boolean useSameSvgOnlyOnceInMask;
    
    /**
     * Override existing generated resources.<br/>
     * It's recommended to use {@link OverrideMode#always} for tests and production releases.
     * 
     * @since 1.0.0
     * @see OverrideMode
     */
	@Parameter(defaultValue = "always")
    private OverrideMode override;

    /**
     * <p>
     * <strong>USE WITH CAUTION</strong><br/>
     * You'll more likely take time to set desired width and height properly
     * </p>
     * 
     * When &lt;SVG&gt; attributes "x", "y", "width" and "height" are not present defines which
     * element are taken in account to compute the Area Of Interest of the image. The plugin will
     * output a WARNING log if no width or height are specified within the &lt;svg&gt; element.
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
     * @since 1.0.1
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
     * @since 1.1.0
     * @see OutputFormat
     */
	@Parameter(defaultValue = "PNG")
    private OutputFormat outputFormat;
    
    /**
     * The quality for the JPG output format.
     * 
     * @since 1.1.0
     */
	@Parameter(defaultValue = "85")
    private int jpgQuality;
    
    /**
     * The background color to use when {@link OutputFormat#JPG} is specified.<br/>
     * Default value is 0xFFFFFFFF (white)
     * 
     * @since 1.1.0
     */
	@Parameter(defaultValue = "-1")
    private int jpgBackgroundColor;
    
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {
        
        // validating target densities specified in pom.xml
        // untargetted densities will be ignored except for the fallback density if specified
        final Set<Density> _targetDensities = targetedDensities;
        if (_targetDensities.isEmpty()) {
            _targetDensities.addAll(EnumSet.allOf(Density.class));
        }
        final Density _fallbackDensity = fallbackDensity;
        _targetDensities.add(_fallbackDensity);
        getLog().info("Targeted densities : " + Joiner.on(", ").join(_targetDensities));
        getLog().debug("Fallback density set to : " + fallbackDensity.toString());
        
        /********************************
         * Load NinePatch configuration *
         ********************************/
        
        NinePatchMap ninePatchMap = new NinePatchMap();
        if (ninePatchConfig != null && ninePatchConfig.isFile()) {
            getLog().info("Loading NinePatch configuration file " + ninePatchConfig.getAbsolutePath());
            try (final Reader reader = new FileReader(ninePatchConfig)) {
                Type t = new TypeToken<Set<NinePatch>>(){}.getType();
                Set<NinePatch> _ninePatchMap = (Set<NinePatch>) (new GsonBuilder().create().fromJson(reader, t));
                ninePatchMap = NinePatch.init(_ninePatchMap);
            } catch (IOException e) {
                getLog().error(e);
            }
        } else {
            getLog().info("No NinePatch configuration file specified");
        }
        
        /*****************************
         * List input svgmask to use *
         *****************************/

        if (svgMaskDirectory == null) {
        	svgMaskDirectory = from;
        }
        if (svgMaskResourcesDirectory == null) {
        	svgMaskResourcesDirectory = from;
        }
        getLog().info("Listing SVGMASK files to use from " + svgMaskDirectory.getAbsolutePath());
        final List<SvgMask> svgMask = new ArrayList<SvgMask>();
        if (from.isDirectory()) {
            for (File f : from.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && "svgmask".equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                        try {
                        	svgMask.add(new SvgMask(QualifiedResource.fromFile(file)));
                            return true;
                        } catch (Exception e) {
                            getLog().error(e);
                        }
                    	getLog().warn("Invalid SVGMASK file : " + file.getAbsolutePath());
                    }
                    if (ninePatchConfig != null 
                    		&& !ninePatchConfig.getAbsolutePath().equals(file.getAbsolutePath())
                    		&& !"svg".equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                    	getLog().debug("Skipping " + file.getAbsolutePath());
                    }
                    return false;
                }
            })) {
                // log matching svgmask inputs
                getLog().debug("Found SVGMASK file to use : " + f.getAbsolutePath());
            }
        } else {
            throw new MojoExecutionException(from.getAbsolutePath() + " is not a directory");
        }
        getLog().info("SVGMASK files found : " + Joiner.on(", ").join(svgMask));
        
        /*****************************
         * List input svg to convert *
         *****************************/

        getLog().info("Listing SVG files to convert from " + from.getAbsolutePath());
        final List<QualifiedResource> svgToConvert = new ArrayList<QualifiedResource>();
        if (from.isDirectory()) {
            for (File f : from.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && "svg".equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                        try {
                            svgToConvert.add(QualifiedResource.fromFile(file));
                            return true;
                        } catch (Exception e) {
                            getLog().error(e);
                        }
                    	getLog().warn("Invalid SVG file : " + file.getAbsolutePath());
                    }
                    if (ninePatchConfig != null 
                    		&& !ninePatchConfig.getAbsolutePath().equals(file.getAbsolutePath())
                    		&& !"svgmask".equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                    	getLog().debug("Skipping " + file.getAbsolutePath());
                    }
                    return false;
                }
            })) {
                // log matching svg inputs
                getLog().debug("Found SVG file to convert : " + f.getAbsolutePath());
            }
        } else {
            throw new MojoExecutionException(from.getAbsolutePath() + " is not a directory");
        }
        getLog().info("SVG files found : " + Joiner.on(", ").join(svgToConvert));

        QualifiedResource _highResIcon = null;
        Rectangle _highResIconBounds = null;
        
        /*********************************
         * Create svg in res/* folder(s) *
         *********************************/
        
        for (QualifiedResource svg : svgToConvert) {
            try {
                getLog().info("Transcoding " + FilenameUtils.getName(svg.getAbsolutePath()) + " to targeted densities");
                Rectangle bounds = extractSVGBounds(svg);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("source dimensions [width=" + bounds.getWidth() + " - height=" + bounds.getHeight() + "]");
                }
                if (highResIcon != null && highResIcon.equals(svg.getName())) {
                    _highResIcon = svg;
                    _highResIconBounds = bounds;
                }
                // for each target density :
                // - find matching destinations :
                //   - matches all extra qualifiers
                //   - no other output with a qualifiers set that is a subset of this output
                // - if no match, create required directories
                for (Density d : _targetDensities) {
                    File destination = svg.getOutputFor(d, to, _fallbackDensity);
                    if (!destination.exists() && createMissingDirectories) {
                        destination.mkdir();
                    }
                    if (destination.exists()) {
                        getLog().debug("Transcoding " + svg.getName() + " to " + destination.getName());
                        transcode(svg, d, bounds, destination, ninePatchMap.getBestMatch(svg));
                    } else {
                        getLog().info("Qualified output " + destination.getName() + " does not exists. " +
                        		"Set createMissingDirectories to true if you want it to be created if missing...");
                    }
                }
            } catch (MalformedURLException e) {
                getLog().error(e);
            } catch (IOException e) {
                getLog().error(e);
            } catch (TranscoderException e) {
                getLog().error(e);
            } catch (InstantiationException e) {
                getLog().error(e);
			} catch (IllegalAccessException e) {
                getLog().error(e);
			}
        }
        
        /******************************************
         * Generates the play store high res icon *
         ******************************************/
        
        if (_highResIcon != null) {
            try {
                // TODO : add a garbage density (NO_DENSITY) for the highResIcon
                getLog().info("Generating high resolution icon");
                transcode(_highResIcon, Density.mdpi, _highResIconBounds, new File("."), 512, 512, null);
            } catch (IOException e) {
                getLog().error(e);
            } catch (TranscoderException e) {
                getLog().error(e);
            } catch (InstantiationException e) {
                getLog().error(e);
			} catch (IllegalAccessException e) {
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
    			"ROI will be computed by magic using Batik " + svgBoundsType.name() + " bounds");
    	return svgBoundsType.getBounds(getGraphicsNode(svgDocument, svg.getDensity().getDpi()));
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
    public void transcode(QualifiedResource svg, Density targetDensity, Rectangle bounds, File destination, NinePatch ninePatch) throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
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
        Float width = new Float(Math.floor(targetWidth));
        Float height = new Float(Math.floor(targetHeight));
        if (getLog().isDebugEnabled()) {
            getLog().debug("target dimensions [width=" + width + " - length=" + height +"]");
        }
        ImageTranscoder t = outputFormat.getTranscoderClass().newInstance();
        if (t instanceof JPEGTranscoder) {
        	// custom jpg hints
	        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, Math.min(1, Math.max(0, jpgQuality / 100f)));
	        t.addTranscodingHint(JPEGTranscoder.KEY_BACKGROUND_COLOR, new Color(jpgBackgroundColor));
        }
        t.addTranscodingHint(ImageTranscoder.KEY_WIDTH, width);
        t.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, height);
        TranscoderInput input = new TranscoderInput(svg.toURI().toURL().toString());
        String outputName = svg.getName();
        if (rename != null && rename.containsKey(outputName)) {
            if (rename.get(outputName) != null && rename.get(outputName).matches("\\w+")) {
                outputName = rename.get(outputName);
            } else {
                getLog().warn(rename.get(outputName) + " is not a valid replacment name for " + outputName);
            }
        }
        
        // final name
        final String finalName = new StringBuilder(dest.getAbsolutePath())
            .append(System.getProperty("file.separator"))
            .append(outputName)
            .append(ninePatch != null && outputFormat.hasNinePatchSupport() ? ".9" : "")
            .append(".")
            .append(outputFormat.name().toLowerCase())
            .toString();
        
        if (override.override(svg, new File(finalName), outputFormat, ninePatchConfig, ninePatch != null)) {
        	
        	// unit conversion for size not in pixel
        	t.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(Constants.MM_PER_INCH / svg.getDensity().getDpi()));
        	// set the ROI
        	t.addTranscodingHint(ImageTranscoder.KEY_AOI, bounds);
        	
            if (ninePatch == null || !outputFormat.hasNinePatchSupport()) {
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
            getLog().debug(finalName + " last modified " + new File(finalName).lastModified());
            getLog().debug(svg.getAbsolutePath() + " last modified " + svg.lastModified());
            if (ninePatch != null) {
                getLog().debug(ninePatchConfig.getAbsolutePath() + " last modified " + ninePatchConfig.lastModified());
            }
            getLog().debug(finalName + " already exists and is up to date... skiping generation!");
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
        int[][] segment = null;
        segment = stretch.getX() == null ? new int[][] {{0, w}} : stretch.getX();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], w, ratio);
            final int size = NinePatch.size(seg[0], seg[1], w, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug("ninepatch stretch(x) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(start + 1, 0, size, 1);
        }
        segment = stretch.getY() == null ? new int[][] {{0, h}} : stretch.getY();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug("ninepatch stretch(y) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(0, start + 1, 1, size);
        }
        Zone content = ninePatch.getContent();
        segment = content.getX() == null ? new int[][] {{0, w}} : content.getX();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], w, ratio);
            final int size = NinePatch.size(seg[0], seg[1], w, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug("ninepatch content(x) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(start + 1, h + 1, size, 1);
        }
        segment = content.getY() == null ? new int[][] {{0, h}} : content.getY();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug("ninepatch content(y) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(w + 1, start + 1, 1, size);
        }
        
        ImageIO.write(ninePatchImage, "png", new File(finalName));
    }

}
