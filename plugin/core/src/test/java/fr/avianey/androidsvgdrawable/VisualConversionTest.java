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

import fr.avianey.androidsvgdrawable.suite.GenDrawableTestSuite;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;

@RunWith(Parameterized.class)
public class VisualConversionTest {
	
    private static SvgDrawablePlugin plugin;

    private static final String PATH_IN  = "./target/test-classes/" + VisualConversionTest.class.getSimpleName() + "/";
    private static final File PATH_OUT  = new File("./target/generated-png/");
    
    // parameters
	private final String filename;
    
    public VisualConversionTest(String filename) {
    	this.filename = filename;
    }
    
    @BeforeClass
    public static void setup() {
        PATH_OUT.mkdirs();
        plugin = new SvgDrawablePlugin(new TestParameters(), new TestLogger());
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
    public void test() throws MalformedURLException, IOException, TranscoderException, InstantiationException, IllegalAccessException {
    	// verify bounds
        QualifiedResource svg = QualifiedResource.fromFile(new File(PATH_IN + filename + ".svg"));
        Rectangle rect = plugin.extractSVGBounds(svg);
        Assert.assertNotNull(rect);
        
        plugin.transcode(svg, svg.getDensity(), rect, PATH_OUT, null);
        BufferedImage transcoded = ImageIO.read(new FileInputStream(new File(PATH_OUT, svg.getName() + ".png")));
        BufferedImage original = ImageIO.read(new FileInputStream(new File(PATH_IN + svg.getName() + ".png")));
        Assert.assertTrue(bufferedImagesEqual(transcoded, original));
    }
 
    private static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
}
