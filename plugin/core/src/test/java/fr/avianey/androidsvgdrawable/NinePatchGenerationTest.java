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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import fr.avianey.androidsvgdrawable.suite.GenDrawableTestSuite;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.FilenameUtils;
import org.joor.Reflect;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static fr.avianey.androidsvgdrawable.Density.Value.*;
import static fr.avianey.androidsvgdrawable.suite.GenDrawableTestSuite.OUTPUT_FORMAT;
import static org.junit.Assert.*;

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
				assertEquals("Bad pixel at (" + pixel[0] + "," + pixel[1] + ") pixel color is " + Integer.toHexString(color), argb & mask, color & mask);
			}
		}

	}

    private static final String PATH_IN  = "./target/test-classes/" + NinePatchGenerationTest.class.getSimpleName() + "/";

    private static SvgDrawablePlugin plugin;
    private static QualifiedSVGResourceFactory qualifiedSVGResourceFactory;

    private final String ninePatchConfig;
    private final String resourceName;
    private final Density.Value targetDensity;

    private final PixelTester tester;

    @BeforeClass
    public static void setup() {
        TestParameters parameters = new TestParameters();
        parameters.outputFormat = OUTPUT_FORMAT;
        plugin = new SvgDrawablePlugin(parameters, new TestLogger());
        qualifiedSVGResourceFactory = plugin.getQualifiedSVGResourceFactory();
    }

    public NinePatchGenerationTest(
    		String resourceName, String ninePatchConfig, Density.Value targetDensity,
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
                            "ninepatch-mdpi.svg", "ninepatch.json", mdpi,
                            new int[][] {
                                    {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, // stretch y
                                    {5, 13}, {6, 13}, // content x
                                    {11, 5}, {11, 6}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {9, 0}, {10, 0}, {11, 0}, // stretch x
                                    {0, 0}, {0, 11}, {0, 12}, {0, 13}, // stretch y
                                    {1, 13}, {2, 13}, {3, 13}, {4, 13}, {7, 13}, {8, 13}, {9, 13}, {10, 13}, // content x
                                    {11, 1}, {11, 2}, {11, 3}, {11, 4}, {11, 7}, {11, 8}, {11, 9}, {11, 10}, {11, 11}, {11, 12} // content y
                            }
                        },
                        {
                            "ninepatch-w10mdpi.svg", "ninepatch.json", mdpi,
                            new int[][] {
                                    {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, // stretch y
                                    {5, 13}, {6, 13}, // content x
                                    {11, 5}, {11, 6}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {9, 0}, {10, 0}, {11, 0}, // stretch x
                                    {0, 0}, {0, 11}, {0, 12}, {0, 13}, // stretch y
                                    {1, 13}, {2, 13}, {3, 13}, {4, 13}, {7, 13}, {8, 13}, {9, 13}, {10, 13}, // content x
                                    {11, 1}, {11, 2}, {11, 3}, {11, 4}, {11, 7}, {11, 8}, {11, 9}, {11, 10}, {11, 11}, {11, 12} // content y
                            }
                        },
                        {
                            "ninepatch-h12mdpi.svg", "ninepatch.json", mdpi,
                            new int[][] {
                                    {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, // stretch y
                                    {5, 13}, {6, 13}, // content x
                                    {11, 5}, {11, 6}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {9, 0}, {10, 0}, {11, 0}, // stretch x
                                    {0, 0}, {0, 11}, {0, 12}, {0, 13}, // stretch y
                                    {1, 13}, {2, 13}, {3, 13}, {4, 13}, {7, 13}, {8, 13}, {9, 13}, {10, 13}, // content x
                                    {11, 1}, {11, 2}, {11, 3}, {11, 4}, {11, 7}, {11, 8}, {11, 9}, {11, 10}, {11, 11}, {11, 12} // content y
                            }
                        },
                        {
                            "ninepatch-h24mdpi.svg", "ninepatch.json", mdpi,
                            new int[][] {
                                    {5, 0}, {6, 0}, {7, 0}, {8, 0}, {9, 0}, {10, 0}, {11, 0}, {12, 0}, {13, 0}, {14, 0}, {15, 0}, {16, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, {0, 11}, {0, 12}, {0, 13}, {0, 14}, {0, 15}, {0, 16}, {0, 17}, {0, 18}, {0, 19}, {0, 20}, // stretch y
                                    {9, 25}, {10, 25}, {11, 25}, {12, 25}, // content x
                                    {21, 9}, {21, 10}, {21, 11}, {21, 12}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {17, 0}, {18, 0}, {19, 0}, {20, 0}, {21, 0}, // stretch x
                                    {0, 0}, {0, 22}, {0, 23}, {0, 24}, // stretch y
                                    {0, 25}, {1, 25}, {2, 25}, {3, 25}, {4, 25}, {5, 25}, {6, 25}, {7, 25}, {8, 25}, {13, 25}, {14, 25}, {15, 25}, {16, 25}, {17, 25}, {18, 25}, {19, 25}, {20, 25}, {21, 25}, // content x
                                    {21, 0}, {21, 1}, {21, 2}, {21, 3}, {21, 4}, {21, 5}, {21, 6}, {21, 7}, {21, 8}, {21, 13}, {21, 14}, {21, 15}, {21, 16}, {21, 17}, {21, 18}, {21, 19}, {21, 20}, {21, 21}, {21, 22}, {21, 23}, {21, 24} // content y
                            }
                        },
                        {
                            "ninepatch-w20mdpi.svg", "ninepatch.json", mdpi,
                            new int[][] {
                                    {5, 0}, {6, 0}, {7, 0}, {8, 0}, {9, 0}, {10, 0}, {11, 0}, {12, 0}, {13, 0}, {14, 0}, {15, 0}, {16, 0}, // stretch x
                                    {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, 8}, {0, 9}, {0, 10}, {0, 11}, {0, 12}, {0, 13}, {0, 14}, {0, 15}, {0, 16}, {0, 17}, {0, 18}, {0, 19}, {0, 20}, // stretch y
                                    {9, 25}, {10, 25}, {11, 25}, {12, 25}, // content x
                                    {21, 9}, {21, 10}, {21, 11}, {21, 12}  // content y
                            },
                            new int[][] {
                                    {0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {17, 0}, {18, 0}, {19, 0}, {20, 0}, {21, 0}, // stretch x
                                    {0, 0}, {0, 22}, {0, 23}, {0, 24}, // stretch y
                                    {0, 25}, {1, 25}, {2, 25}, {3, 25}, {4, 25}, {5, 25}, {6, 25}, {7, 25}, {8, 25}, {13, 25}, {14, 25}, {15, 25}, {16, 25}, {17, 25}, {18, 25}, {19, 25}, {20, 25}, {21, 25}, // content x
                                    {21, 0}, {21, 1}, {21, 2}, {21, 3}, {21, 4}, {21, 5}, {21, 6}, {21, 7}, {21, 8}, {21, 13}, {21, 14}, {21, 15}, {21, 16}, {21, 17}, {21, 18}, {21, 19}, {21, 20}, {21, 21}, {21, 22}, {21, 23}, {21, 24} // content y
                            }
                        },
                        {
                            "ninepatch-mdpi.svg", "ninepatch.json", hdpi,
                            new int[][] {
                                    {4, 0}, {12, 0}, // stretch x
                                    {0, 1}, {0, 15}, // stretch y
                                    {7, 19}, {9, 19}, // content x
                                    {16, 7}, {16, 9}  // content y
                            },
                            new int[][] {
                                    {3, 0}, {13, 0}, // stretch x
                                    {0, 0}, {0, 16}, // stretch y
                                    {6, 19}, {10, 19}, // content x
                                    {16, 6}, {16, 10}, // content y
                            }
                        },

                    	// https://github.com/avianey/androidsvgdrawable-plugin/issues/12
                    	// size can't be < 1 when scaling down svg to a lower density
                        {
                            "width_1-mdpi.svg", "width_1.json", ldpi,
                            new int[][] {},
                            new int[][] {}
                        },

                    	// https://github.com/avianey/androidsvgdrawable-plugin/issues/14
                    	// corners must be transparents
                        {
                            "simple_square-mdpi.svg", "simple_square.json", mdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json", ldpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json", hdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "simple_square-mdpi.svg", "simple_square.json", xhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", mdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", ldpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", hdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", xhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", xxhdpi,
                            new int[][] {},
                            new int[][] {}
                        },
                        {
                            "width_too_large-mdpi.svg", "width_too_large.json", xxxhdpi,
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
            QualifiedResource svg = qualifiedSVGResourceFactory.fromSVGFile(new File(PATH_IN + resourceName));
            NinePatch ninePatch = ninePatchMap.getBestMatch(svg);

            assertNotNull(ninePatch);

            final String name = svg.getName();
        	Reflect.on(svg).set("name", name + "_" + targetDensity.name());
	        plugin.transcode(svg, targetDensity, new File(GenDrawableTestSuite.PATH_OUT), ninePatch);
	        final File ninePatchFile = new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + ".9." + OUTPUT_FORMAT.name().toLowerCase());
            final File nonNinePatchFile = new File(GenDrawableTestSuite.PATH_OUT + svg.getName() + "." + OUTPUT_FORMAT.name().toLowerCase());

            if (OUTPUT_FORMAT.hasNinePatchSupport()) {
            	assertTrue(FilenameUtils.getName(ninePatchFile.getAbsolutePath()) + " does not exists although the output format supports nine patch", ninePatchFile.exists());
            	assertTrue(FilenameUtils.getName(nonNinePatchFile.getAbsolutePath()) + " file does not exists although the output format supports nine patch", !nonNinePatchFile.exists());
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
            	assertTrue(FilenameUtils.getName(ninePatchFile.getAbsolutePath()) + " exists although the output format does not support nine patch", !ninePatchFile.exists());
            	assertTrue(FilenameUtils.getName(nonNinePatchFile.getAbsolutePath()) + " does not exists although the output format does not support nine patch", nonNinePatchFile.exists());
            }
        }
    }

}
