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

import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.apache.batik.transcoder.TranscoderException;
import org.junit.Assert;
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

@RunWith(Parameterized.class)
// TODO generate reference PNG with the targeted jdk instead of approximate the result
public class VisualConversionTest {

    private static final String PATH_IN  = "./target/test-classes/" + VisualConversionTest.class.getSimpleName() + "/";
    private static final File PATH_OUT  = new File("./target/generated-png/");

    private static SvgDrawablePlugin plugin;
    private static QualifiedSVGResourceFactory qualifiedSVGResourceFactory;

    // parameters
	private final String filename;

    public VisualConversionTest(String filename) {
    	this.filename = filename;
    }

    @BeforeClass
    public static void setup() {
        PATH_OUT.mkdirs();
        plugin = new SvgDrawablePlugin(new TestParameters(), new TestLogger());
        qualifiedSVGResourceFactory = plugin.getQualifiedSVGResourceFactory();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
        				new Object[][] {
                                {"ic_screen_rotation-mdpi"},   // https://github.com/google/material-design-icons
                                {"issue_29-mdpi"} // https://github.com/avianey/androidsvgdrawable-plugin/issues/29
        				}
        		);
    }

    @Test
    public void test() throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
    	// verify bounds
        QualifiedResource svg = qualifiedSVGResourceFactory.fromSVGFile(new File(PATH_IN + filename + ".svg"));
        Rectangle rect = svg.getBounds();
        Assert.assertNotNull(rect);

        plugin.transcode(svg, svg.getDensity().getValue(), PATH_OUT, null);
        BufferedImage transcoded = ImageIO.read(new FileInputStream(new File(PATH_OUT, svg.getName() + ".png")));
        BufferedImage original = ImageIO.read(new FileInputStream(new File(PATH_IN + svg.getName() + ".pngtest")));
        Assert.assertEquals(0, bufferedImagesEqual(transcoded, original), 0.1);
    }

    private static double bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            double inequals = 0;
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (pixelDistance(img1.getRGB(x, y),img2.getRGB(x, y)) > 255) {
                        inequals++;;
                    }
                }
            }
            return inequals / (img1.getWidth() * img1.getHeight());
        } else {
            throw new RuntimeException("Image size are not equals");
        }
    }

    private static double pixelDistance(int argb1, int argb2) {
        long dist = 0;
        dist += Math.abs((long) ((argb1 & 0xFF000000) - (argb2 & 0xFF000000)) >>> 24);
        dist += Math.abs((long) ((argb1 & 0x00FF0000) - (argb2 & 0x00FF0000)) >>> 16);
        dist += Math.abs((long) ((argb1 & 0x0000FF00) - (argb2 & 0x0000FF00)) >>> 8);
        dist += Math.abs((long) ((argb1 & 0x000000FF) - (argb2 & 0x000000FF)));
        return dist;
    }

}
