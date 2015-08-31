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

import fr.avianey.androidsvgdrawable.suite.GenDrawableTestSuite;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.apache.batik.transcoder.TranscoderException;
import org.joor.Reflect;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static fr.avianey.androidsvgdrawable.util.Constants.MM_PER_INCH;
import static java.lang.Math.ceil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class BoundsExtractionTest {

	private static final int   DPI  = Density.Value.mdpi.getDpi();
	private static final float DPMM = DPI / MM_PER_INCH;
	private static final float DPCM = DPMM * 10;
	private static final String PATH_IN  = "./target/test-classes/" + BoundsExtractionTest.class.getSimpleName() + "/";

	private static SvgDrawablePlugin plugin;
	private static QualifiedSVGResourceFactory qualifiedSVGResourceFactory;

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
        TestParameters parameters = new TestParameters();
        parameters.outputFormat = GenDrawableTestSuite.OUTPUT_FORMAT;
        plugin = new SvgDrawablePlugin(parameters, new TestLogger());
		qualifiedSVGResourceFactory = plugin.getQualifiedSVGResourceFactory();
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
        						{"square_cm-mdpi.svg", 10 * DPCM, 10 * DPCM}		// height and width in cm (usefull ???)
        				}
        		);
    }

    @Test
    public void test() throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
    	// verify bounds
        QualifiedResource svg = qualifiedSVGResourceFactory.fromSVGFile(new File(PATH_IN + filename));
        Rectangle svgBounds = svg.getBounds();
        assertNotNull(svgBounds);
        assertEquals(ceil(expectedWidth), svgBounds.getWidth(), 0);
        assertEquals(ceil(expectedHeight), svgBounds.getHeight(), 0);

        // verify generated png (width, height) for each target density
        final String name = svg.getName();
        for (Density.Value d : Density.Value.values()) {
        	Reflect.on(svg).set("name", name + "_" + d.name());
	        plugin.transcode(svg, d, new File(GenDrawableTestSuite.PATH_OUT), null);
	        BufferedImage image = ImageIO.read(new FileInputStream(new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + "." + GenDrawableTestSuite.OUTPUT_FORMAT.name().toLowerCase())));
			Rectangle expectedBounds = svg.getScaledBounds(d);
			assertEquals(expectedBounds.getWidth(), image.getWidth(), 0);
	        assertEquals(expectedBounds.getHeight(), image.getHeight(), 0);
        }
    }

}
