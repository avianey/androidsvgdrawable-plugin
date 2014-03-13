package fr.avianey.mojo.androidgendrawable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.avianey.mojo.androidgendrawable.NinePatch.Zone;

@RunWith(Parameterized.class)
public class NinePatchParsingTest {

	private static final String PATH_IN  = "./target/test-classes/" + NinePatchParsingTest.class.getSimpleName() + "/";

    private final String fileName;
    private final String resourceName;
    private final Integer[][] stretchX, stretchY;
    private final Integer[][] contentX, contentY;
    private final Map<Qualifier.Type, String> typedQualifiers;
    private final boolean resultExpected;

    @SuppressWarnings("unchecked")
    public NinePatchParsingTest(String fileName, String resourceName, 
            Object[][] stretchX, Object[][] stretchY, Object[][] contentX, Object[][] contentY, 
            Map<Qualifier.Type, String> typedQualifiers, boolean resultExpected) {
         this.fileName = fileName;
         this.resourceName = resourceName;
         this.stretchX = (Integer[][]) stretchX;
         this.stretchY = (Integer[][]) stretchY;
         this.contentX = (Integer[][]) contentX;
         this.contentY = (Integer[][]) contentY;
         this.typedQualifiers = typedQualifiers != null ? typedQualifiers : Collections.EMPTY_MAP;
         this.resultExpected = resultExpected;
    }
    
	@Parameters
    @SuppressWarnings("serial")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {"9patch-1.json", "input_name1",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            new Integer[][] {{10, 40}}, 
                            new Integer[][] {{10, 41}}, 
                            new Integer[][] {{11, 40}},
                            null,
                            true
                        },
                        {"9patch-2.json", "input_name2",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            null, 
                            new Integer[][] {{10, 41}}, 
                            new Integer[][] {{11, 40}},
                            null,
                            true
                        },
                        {"9patch-3.json", "input_name3",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            new Integer[][] {{10, 40}}, 
                            null, 
                            null,
                            null,
                            true
                        },
                        {"9patch-4-land.json", "input_name4",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            new Integer[][] {{10, 40}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "land");
                                put(Qualifier.Type.density, "mdpi");
                            }},
                            true
                        },
                        {"9patch-4-land.json", "input_name4",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            new Integer[][] {{10, 40}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.density, "mdpi");
                            }},
                            false
                        },
                        {"9patch-5-land-vs-port.json", "input_name5",
                            new Integer[][] {{10, 20}, {30, 40}}, 
                            new Integer[][] {{10, 40}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "land");
                                put(Qualifier.Type.density, "mdpi");
                            }},
                            true
                        },
                        {"9patch-5-land-vs-port.json", "input_name5",
                            new Integer[][] {{11, 21}, {31, 41}}, 
                            new Integer[][] {{11, 41}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.density, "hdpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{11, 21}, {31, 41}}, 
                            new Integer[][] {{11, 41}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.density, "ldpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{12, 22}, {32, 42}}, 
                            new Integer[][] {{12, 42}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.uiMode, "television");
                                put(Qualifier.Type.density, "ldpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{13, 23}, {33, 43}}, 
                            new Integer[][] {{13, 43}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.uiMode, "television");
                                put(Qualifier.Type.plateformVersion, "v18");
                                put(Qualifier.Type.density, "ldpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{14, 24}, {34, 44}}, 
                            new Integer[][] {{14, 44}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.uiMode, "car");
                                put(Qualifier.Type.plateformVersion, "v18");
                                put(Qualifier.Type.density, "xxxhdpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{14, 24}, {34, 44}}, 
                            new Integer[][] {{14, 44}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.orientation, "port");
                                put(Qualifier.Type.uiMode, "car");
                                put(Qualifier.Type.plateformVersion, "v18");
                                put(Qualifier.Type.touchScreen, "notouch");
                                put(Qualifier.Type.navigationKey, "navhidden");
                                put(Qualifier.Type.smallestWidth, "sw400dp");
                                put(Qualifier.Type.density, "ldpi");
                            }},
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{14, 24}, {34, 44}}, 
                            new Integer[][] {{14, 44}}, 
                            null, 
                            null,
                            new HashMap<Qualifier.Type, String>() {{
                                put(Qualifier.Type.uiMode, "car");
                                put(Qualifier.Type.density, "ldpi");
                            }},
                            false
                        }
                });
    }
    
    @Test
    public void fromJson() throws URISyntaxException, JsonIOException, JsonSyntaxException, IOException {
        try (final Reader reader = new InputStreamReader(new FileInputStream(PATH_IN + fileName))) {
            Type t = new TypeToken<Set<NinePatch>>() {}.getType();
            Set<NinePatch> ninePatchSet = new GsonBuilder().create().fromJson(reader, t);
            NinePatchMap ninePatchMap = NinePatch.init(ninePatchSet);
            
            QualifiedResource mockedResource = Mockito.mock(QualifiedResource.class);
            Mockito.when(mockedResource.getName()).thenReturn(resourceName);
            Mockito.when(mockedResource.getTypedQualifiers()).thenReturn(typedQualifiers);
            
            NinePatch ninePatch = ninePatchMap.getBestMatch(mockedResource);
            Assert.assertTrue(resultExpected ^ (ninePatch == null));
            
            if (ninePatch != null) {
                verifyZone(ninePatch.getStretch(), stretchX, stretchY);
                verifyZone(ninePatch.getContent(), contentX, contentY);
            }
            
        }
    }

    private void verifyZone(Zone zone, Integer[][] x, Integer[][] y) {
        if (zone == null) {
            // zone cannot be null
            Assert.fail();
        } else {
            verifyDirection(zone.getX(), x);
            verifyDirection(zone.getY(), y);
        }
    }

    private void verifyDirection(int[][] actual, Integer[][] expected) {
        if (actual == null) {
            Assert.assertNull(expected);
        } else {
            Assert.assertNotNull(expected);
            Assert.assertEquals(actual.length, expected.length);
            for (int i = 0; i < actual.length; i++) {
                Assert.assertNotNull(expected[i]);
                Assert.assertNotNull(actual[i]);
                Assert.assertEquals(actual[i].length, expected[i].length);
                for (int j = 0; j < actual.length; j++) {
                    Assert.assertNotNull(expected[i][j]);
                    Assert.assertEquals(actual[i][j], expected[i][j].intValue());
                }
            }
        }
    }
    
}
