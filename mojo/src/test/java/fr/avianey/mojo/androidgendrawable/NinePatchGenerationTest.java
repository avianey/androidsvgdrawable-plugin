package fr.avianey.mojo.androidgendrawable;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FilenameUtils;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.avianey.mojo.androidgendrawable.suite.GenDrawableTestSuite;

@RunWith(Parameterized.class)
public class NinePatchGenerationTest {
	
	private static class PixelTester {
		
		final int[][] on;
		final int[][] off;
		
		public PixelTester(int[][] on, int[][] off) {
			this.on = on;
			this.off = off;
		}
		
		public void test(BufferedImage image) {
			test(image, on, 0xFF000000, 0xFFFFFFFF);
			test(image, off, 0x00000000, 0xFF000000);
		}
		
		private void test(BufferedImage image, int[][] pixels, int argb, int mask) {
			for (int[] pixel : pixels) {
				int color = image.getRGB(pixel[0], pixel[1]);
				Assert.assertEquals("Bad pixel at (" + pixel[0] + "," + pixel[1] + ") pixel color is " + Integer.toHexString(color), argb & mask, color & mask);
			}
		}
		
	}

	private static final String PATH_IN  = "./target/test-classes/" + NinePatchGenerationTest.class.getSimpleName() + "/";

    private final String ninePatchConfig;
    private final String resourceName;
    private final Density targetDensity;
    private final PixelTester tester;

    private static Gen gen;
    
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

    public NinePatchGenerationTest(
    		String resourceName, String ninePatchConfig, 
    		Density targetDensity,
            int[][] pixelsOn, int[][] pixelsOff) {
         this.ninePatchConfig = ninePatchConfig;
         this.resourceName = resourceName;
         this.targetDensity = targetDensity;
         this.tester = new PixelTester(pixelsOn, pixelsOff);
    }
    
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {
                            "ninepatch-mdpi.svg", "ninepatch.json",
                            Density.mdpi,
                            new int[][] {
                                    {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, // content y
                                    {5, 13}, {6, 13}, // content x
                                    {11, 5}, {11, 6}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {9, 0}, {10, 0}, {11, 0}, // stretch x
                                    {0, 0}, {0, 11}, {0, 12}, {0, 13}, // content y
                                    {1, 13}, {2, 13}, {3, 13}, {4, 13}, {7, 13}, {8, 13}, {9, 13}, {10, 13}, // content x
                                    {11, 1}, {11, 2}, {11, 3}, {11, 4}, {11, 7}, {11, 8}, {11, 9}, {11, 10}, {11, 11}, {11, 12} // content y
                            }
                        },
                        {
                            "ninepatch-mdpi.svg", "ninepatch.json",
                            Density.hdpi,
                            new int[][] {
                                    {4, 0}, {12, 0}, // stretch x
                                    {0, 1}, {0, 15}, // content y
                                    {7, 19}, {9, 19}, // content x
                                    {16, 7}, {16, 9}  // content y
                            },
                            new int[][] {
                                    {3, 0}, {13, 0}, // stretch x
                                    {0, 0}, {0, 16}, // content y
                                    {6, 19}, {10, 19}, // content x
                                    {16, 6}, {16, 10}, // content y
                            }
                        },
                        
                    	// https://github.com/avianey/androidgendrawable-maven-plugin/issues/12
                    	// size can't be < 1 when scaling down svg to a lower density
                        {
                            "width_1-mdpi.svg", "width_1.json",
                            Density.ldpi,
                            new int[][] {},
                            new int[][] {}
                        },

                    	// https://github.com/avianey/androidgendrawable-maven-plugin/issues/14
                    	// corners must be transparents
                        {
                            "simple_square-mdpi.svg", "simple_square.json",
                            Density.mdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json",
                            Density.ldpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json",
                            Density.hdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json",
                            Density.xhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.mdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.ldpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.hdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.xhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.xxhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json",
                            Density.xxxhdpi,
                            new int[][] {},
                            new int[][] {}
                        }
                });
    }
    
    @Test
    public void fromJson() throws URISyntaxException, JsonIOException, JsonSyntaxException, IOException, TranscoderException, InstantiationException, IllegalAccessException {
        try (final Reader reader = new InputStreamReader(new FileInputStream(PATH_IN + ninePatchConfig))) {
            Type t = new TypeToken<Set<NinePatch>>() {}.getType();
            Set<NinePatch> ninePatchSet = new GsonBuilder().create().fromJson(reader, t);
            NinePatchMap ninePatchMap = NinePatch.init(ninePatchSet);
            QualifiedResource svg = QualifiedResource.fromFile(new File(PATH_IN + resourceName));
            NinePatch ninePatch = ninePatchMap.getBestMatch(svg);
            
            Assert.assertNotNull(ninePatch);

            final String name = svg.getName();
        	Reflect.on(svg).set("name", name + "_" + targetDensity.name());
        	Rectangle bounds = gen.extractSVGBounds(svg);
	        gen.transcode(svg, targetDensity, bounds, new File(GenDrawableTestSuite.PATH_OUT), ninePatch);
	        final File ninePatchFile = new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + ".9." + GenDrawableTestSuite.OUTPUT_FORMAT.name().toLowerCase());
            final File nonNinePatchFile = new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + "." + GenDrawableTestSuite.OUTPUT_FORMAT.name().toLowerCase());
            
            if (GenDrawableTestSuite.OUTPUT_FORMAT.hasNinePatchSupport()) {
            	Assert.assertTrue(FilenameUtils.getName(ninePatchFile.getAbsolutePath()) + " does not exists although the output format supports nine patch", ninePatchFile.exists());
            	Assert.assertTrue(FilenameUtils.getName(nonNinePatchFile.getAbsolutePath()) + " file does not exists although the output format supports nine patch", !nonNinePatchFile.exists());
	            BufferedImage image = ImageIO.read(new FileInputStream(ninePatchFile));
		        tester.test(image);
		        // test corner pixels
		        int w = image.getWidth();
		        int h = image.getHeight();
		        new PixelTester(new int[][] {}, new int[][] {
		        		{0, 0},
		        		{0, h - 1},
		        		{w - 1, 0},
		        		{w - 1, h - 1}
		        }).test(image);
            } else {
            	Assert.assertTrue(FilenameUtils.getName(ninePatchFile.getAbsolutePath()) + " exists although the output format does not support nine patch", !ninePatchFile.exists());
            	Assert.assertTrue(FilenameUtils.getName(nonNinePatchFile.getAbsolutePath()) + " does not exists although the output format does not support nine patch", nonNinePatchFile.exists());
            }
        }
    }
    
}
