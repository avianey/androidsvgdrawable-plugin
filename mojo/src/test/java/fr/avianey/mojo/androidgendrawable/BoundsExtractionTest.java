package fr.avianey.mojo.androidgendrawable;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.avianey.mojo.androidgendrawable.suite.GenDrawableTestSuite;
import fr.avianey.mojo.androidgendrawable.util.Constants;

@RunWith(Parameterized.class)
public class BoundsExtractionTest {

	private static final int   	DPI 		   	= Density.mdpi.getDpi();
	private static final float 	DPMM 			= DPI / (float) Constants.MM_PER_INCH;
	private static final float 	DPCM	 		= DPMM * 10;
	
    private static Gen gen;

	private static final String PATH_IN  = "./target/test-classes/" + BoundsExtractionTest.class.getSimpleName() + "/";
	
    // parameters
	private final String filename;
	private final float expectedWidth;
	private final float expectedHeight;
    
    public BoundsExtractionTest(String filename, float expectedWidth, float expectedHeight) {
    	this.filename = filename;
    	this.expectedWidth = expectedWidth;
    	this.expectedHeight = expectedHeight;
    }
    
    @BeforeClass
    public static void setup() {
        gen = new Gen();
        // setup
        Reflect.on(gen).set("outputFormat", GenDrawableTestSuite.OUTPUT_FORMAT);
        Reflect.on(gen).set("jpgQuality", 85);
        Reflect.on(gen).set("jpgBackgroundColor", -1);
        Reflect.on(gen).set("overrideMode", OverrideMode.always);
        Reflect.on(gen).set("svgBoundsType", BoundsType.sensitive);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
        				new Object[][] {
        						{"circle_clipped-mdpi.svg", 10, 10},	// shape too big for ROI (height,width) but clipped to (height,width)
        						{"circle_too_big-mdpi.svg", 10, 10}, 	// shape too big for ROI (height,width)
        						{"square_effect-mdpi.svg", 10, 10},  	// shape included in ROI (height,width) but with effect outside of the ROI
        						{"square_stroke-mdpi.svg", 10, 10},  	// shape included in ROI (height,width) but with stroke path outside of the ROI
        						{"square-mdpi.svg", 10, 10},		 	// shape included in ROI (height,width) but smaller than the ROI
        						//
        						{"square_no_height_width_stroke-mdpi.svg", 6, 6}, 	// no height and width provided by the <SVG> element
        						{"square_translated-mdpi.svg", 10, 10},       		// x and y != 0 (square outside)
        						{"square_cm-mdpi.svg", 10 * DPCM, 10 * DPCM} 		// height and width in cm (usefull ???)
        				}
        		);
    }

    @Test
    public void test() throws MalformedURLException, IOException, TranscoderException, InstantiationException, IllegalAccessException {
    	// verify bounds
        QualifiedResource svg = QualifiedResource.fromFile(new File(PATH_IN + filename));
        Rectangle rect = gen.extractSVGBounds(svg);
        Assert.assertNotNull(rect);
        Assert.assertEquals(Math.ceil(expectedWidth), rect.getWidth(), 0);
        Assert.assertEquals(Math.ceil(expectedHeight), rect.getHeight(), 0);
        
        // verify generated png (width, height) for each target density
        final String name = svg.getName();
        for (Density d : Density.values()) {
        	Reflect.on(svg).set("name", name + "_" + d.name());
	        gen.transcode(svg, d, rect, new File(GenDrawableTestSuite.PATH_OUT), null);
	        BufferedImage image = ImageIO.read(new FileInputStream(new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + "." + GenDrawableTestSuite.OUTPUT_FORMAT.name().toLowerCase())));
	        Assert.assertEquals(Math.floor(svg.getDensity().ratio(d) * Math.ceil(expectedWidth)), image.getWidth(), 0);
	        Assert.assertEquals(Math.floor(svg.getDensity().ratio(d) * Math.ceil(expectedHeight)), image.getHeight(), 0);
        }
    }
    
}
