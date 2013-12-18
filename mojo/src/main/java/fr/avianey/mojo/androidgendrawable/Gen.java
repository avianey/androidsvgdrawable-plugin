package fr.avianey.mojo.androidgendrawable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
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
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.svg.SVGDocument;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.avianey.mojo.androidgendrawable.NinePatch.Zone;

/**
 * Goal which generates drawable from Scalable Vector Graphics (SVG) files.
 * 
 * @goal gen
 */
// TODO : delete PNG with the same name as target PNG
// TODO : JPEG or PNG
// TODO : 9-Patch config doit tenir compte des classifiers
// TODO : handle multiple output directories with no density qualifier
// TODO : ordered qualifiers (http://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules)
public class Gen extends AbstractMojo {
    
    /**
     * Directory of the svg resources to generate drawable from.
     * 
     * @parameter
     * @required
     */
    private File from;
    
    /**
     * Location of the Android "./res/drawable(...)" directories :
     * - drawable
     * - drawable-hdpi
     * - drawable-ldpi
     * - drawable-mdpi
     * - drawable-xhdpi
     * - drawable-xxhdpi
     * 
     * @parameter default-value="${project.basedir}/res"
     */
    private File to;
    
    /**
     * Create a drawable-density directory when no directory exists for the given qualifiers.
     * If set to false, the plugin will generate the drawable in the best matching directory :
     * <ul>
     * <li>match all of the qualifiers</li>
     * <li>no other matching directory with less qualifiers</li>
     * </ul>
     * 
     * @parameter default-value="true"
     */
    private boolean createMissingDirectories;

    /**
     * Enumeration of desired target densities.
     * If no density specified, PNG are only generated to existing directories.
     * If at least one density is specified, PNG are only generated in matching directories.
     * 
     * @parameter 
     */
    private Set<Density> targetedDensities;

    /**
     * Use alternatives names for PNG resources
     * Key = original svg name (without density prefix)
     * Value = target name
     * 
     * @parameter 
     */
    private Map<String, String> rename;

    /**
     * Density for drawable directories without density qualifier
     * 
     * @parameter default-value="mdpi"
     */
    private Density fallbackDensity;
    
    /**
     * Name of the input file to use to generate a 512x512 high resolution Google Play icon
     * 
     * @parameter default-value=""
     */
    private String highResIcon;
    
    /**
     * Path to the 9-patch drawable configuration file.
     * 
     * @parameter default-value=null
     */
    private File ninePatchConfig;
    
    /**
     * Override existing generated resources.
     * 
     * @parameter default-value="always"
     */
    private OverrideMode override;
    
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
        getLog().debug("Fallback density set to : " + fallbackDensity.toString());
        
        /********************************
         * Load NinePatch configuration *
         ********************************/
        
        NinePatchMap ninePatchMap = new NinePatchMap();
        if (ninePatchConfig != null) {
            try (final Reader reader = new FileReader(ninePatchConfig)) {
                Type t = new TypeToken<Set<NinePatch>>(){}.getType();
                Set<NinePatch> _ninePatchMap = (Set<NinePatch>) (new GsonBuilder().create().fromJson(reader, t));
                ninePatchMap = NinePatch.init(_ninePatchMap);
            } catch (IOException e) {
                getLog().error(e);
            }
        }
        
        /*****************************
         * List input svg to convert *
         *****************************/
        
        final List<QualifiedResource> svgToConvert = new ArrayList<QualifiedResource>();
        if (from.isDirectory()) {
            for (File f : from.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && "svg".equalsIgnoreCase(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                        try {
                            svgToConvert.add(QualifiedResource.fromSvgFile(file));
                            return true;
                        } catch (Exception e) {
                            getLog().error(e);
                        }
                    }
                    getLog().warn("Invalid svg input : " + file.getAbsolutePath());
                    return false;
                }
            })) {
                // log matching svg inputs
                getLog().debug("Found svg file to convert : " + f.getAbsolutePath());
            }
        } else {
            throw new MojoExecutionException(from.getAbsolutePath() + " is not a valid input directory");
        }

        QualifiedResource _highResIcon = null;
        Rectangle2D _highResIconBounds = null;
        
        /*********************************
         * Create svg in res/* folder(s) *
         *********************************/
        
        for (QualifiedResource svg : svgToConvert) {
            try {
                getLog().info("Handling " + FilenameUtils.getName(svg.getAbsolutePath()));
                Rectangle2D bounds = extractSVGBounds(svg);
                if (getLog().isDebugEnabled()) {
                    getLog().debug(">> Parsing : bounds [width=" + bounds.getWidth() + " - height=" + bounds.getHeight() + "]");
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
                        getLog().info("Transcoding " + svg.getName() + " to " + destination.getName());
                        transcode(svg, d, bounds, destination, ninePatchMap.get(svg));
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
            }
        }
        
        /******************************************
         * Generates the play store high res icon *
         ******************************************/
        
        if (_highResIcon != null) {
            try {
                // TODO : add a garbage density (NO_DENSITY) for the highResIcon
                getLog().info("Handling high resolution icon");
                transcode(_highResIcon, Density.mdpi, _highResIconBounds, new File("."), 512, 512, null);
            } catch (IOException e) {
                getLog().error(e);
            } catch (TranscoderException e) {
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
    private Rectangle2D extractSVGBounds(QualifiedResource svg) throws MalformedURLException, IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        SVGDocument doc = (SVGDocument) f.createDocument(svg.toURI().toURL().toString());
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        GVTBuilder builder = new GVTBuilder();
        GraphicsNode rootGN = builder.build(ctx, doc);
        return rootGN.getGeometryBounds();
    }
    
    /**
     * Given it's bounds, transcodes a svg file to a PNG for the desired density
     * @param svg
     * @param targetDensity 
     * @param bounds
     * @param destination
     * @throws IOException
     * @throws TranscoderException
     */
    private void transcode(QualifiedResource svg, Density targetDensity, Rectangle2D bounds, File destination, NinePatch ninePatch) throws IOException, TranscoderException {
        transcode(svg, targetDensity, bounds, destination, 
                new Float(bounds.getWidth() * svg.getDensity().ratio(targetDensity)), 
                new Float(bounds.getHeight() * svg.getDensity().ratio(targetDensity)),
                ninePatch);
    }
    
    /**
     * Given a desired width and height, transcodes a svg file to a PNG for the desired density
     * @param svg
     * @param targetDensity 
     * @param bounds
     * @param dest
     * @param targetWidth
     * @param targetHeight
     * @throws IOException
     * @throws TranscoderException
     */
    // TODO : center inside option
    // TODO : preserve aspect ratio
    private void transcode(QualifiedResource svg, Density targetDensity, Rectangle2D bounds, File dest, float targetWidth, float targetHeight, NinePatch ninePatch) throws IOException, TranscoderException {
        Float width = new Float(Math.floor(targetWidth));
        Float height = new Float(Math.floor(targetHeight));
        if (getLog().isDebugEnabled()) {
            getLog().debug(">> Transcoding : dimensions [width=" + width + " - length=" + height +"]");
        }
        PNGTranscoder t = new PNGTranscoder();
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        TranscoderInput input = new TranscoderInput(svg.toURI().toURL().toString());
        String outputName = svg.getName();
        if (rename != null && rename.containsKey(outputName)) {
            if (rename.get(outputName) != null && rename.get(outputName).matches("\\w+")) {
                outputName = rename.get(outputName);
            } else {
                getLog().warn(rename.get(outputName) + " is not a valid replacment name for " + outputName);
            }
        }
        final String finalName = new StringBuilder(dest.getAbsolutePath())
            .append(System.getProperty("file.separator"))
            .append(outputName)
            .append(ninePatch != null ? ".9" : "")
            .append(".png")
            .toString();
        if (override.override(svg, new File(finalName), ninePatchConfig, ninePatch != null)) {
            if (ninePatch == null) {
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
            getLog().info(finalName + " already exists and is up to date... skiping generation!");
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
                getLog().debug(">> NinePatch : stretch(x) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(start + 1, 0, size, 1);
        }
        segment = stretch.getY() == null ? new int[][] {{0, h}} : stretch.getY();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug(">> NinePatch : stretch(y) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(0, start + 1, 1, size);
        }
        Zone content = ninePatch.getContent();
        segment = content.getX() == null ? new int[][] {{0, w}} : content.getX();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], w, ratio);
            final int size = NinePatch.size(seg[0], seg[1], w, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug(">> NinePatch : content(x) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(start + 1, h + 1, size, 1);
        }
        segment = content.getY() == null ? new int[][] {{0, h}} : content.getY();
        for (int[] seg : segment) {
            final int start = NinePatch.start(seg[0], seg[1], h, ratio);
            final int size = NinePatch.size(seg[0], seg[1], h, ratio);
            if (getLog().isDebugEnabled()) {
                getLog().debug(">> NinePatch : content(y) [start=" + start + " - size=" + size + "]");
            }
            g.fillRect(w + 1, start + 1, 1, size);
        }
        
        ImageIO.write(ninePatchImage, "png", new File(finalName));
    }

}
