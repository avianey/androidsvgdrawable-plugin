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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
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

import fr.avianey.androidsvgdrawable.NinePatch;
import fr.avianey.androidsvgdrawable.NinePatchMap;
import fr.avianey.androidsvgdrawable.QualifiedResource;
import fr.avianey.androidsvgdrawable.Qualifier;
import fr.avianey.androidsvgdrawable.NinePatch.Zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class NinePatchParsingTest {

	private static final String PATH_IN  = "./target/test-classes/" + NinePatchParsingTest.class.getSimpleName() + "/";

    private final String fileName;
    private final String resourceName;
    private final Integer[][] stretchX, stretchY;
    private final Integer[][] contentX, contentY;
    private final Map<Qualifier.Type, String> typedQualifiers;
    private final boolean resultExpected;

    public NinePatchParsingTest(String fileName, String resourceName,
            Object[][] stretchX, Object[][] stretchY, Object[][] contentX, Object[][] contentY,
            String typedQualifiers, boolean resultExpected) {
         this.fileName = fileName;
         this.resourceName = resourceName;
         this.stretchX = (Integer[][]) stretchX;
         this.stretchY = (Integer[][]) stretchY;
         this.contentX = (Integer[][]) contentX;
         this.contentY = (Integer[][]) contentY;
         this.typedQualifiers = Qualifier.fromQualifiedString(typedQualifiers);
         this.resultExpected = resultExpected;
    }

	@Parameters
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
                            "land-mdpi",
                            true
                        },
                        {"9patch-4-land.json", "input_name4",
                            new Integer[][] {{10, 20}, {30, 40}},
                            new Integer[][] {{10, 40}},
                            null,
                            null,
                            "port-mdpi",
                            false
                        },
                        {"9patch-5-land-vs-port.json", "input_name5",
                            new Integer[][] {{10, 20}, {30, 40}},
                            new Integer[][] {{10, 40}},
                            null,
                            null,
                            "land-mdpi",
                            true
                        },
                        {"9patch-5-land-vs-port.json", "input_name5",
                            new Integer[][] {{11, 21}, {31, 41}},
                            new Integer[][] {{11, 41}},
                            null,
                            null,
                            "port-hdpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{11, 21}, {31, 41}},
                            new Integer[][] {{11, 41}},
                            null,
                            null,
                            "port-ldpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{12, 22}, {32, 42}},
                            new Integer[][] {{12, 42}},
                            null,
                            null,
                            "port-television-ldpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{13, 23}, {33, 43}},
                            new Integer[][] {{13, 43}},
                            null,
                            null,
                            "port-television-v18-ldpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{14, 24}, {34, 44}},
                            new Integer[][] {{14, 44}},
                            null,
                            null,
                            "port-car-v18-xxxhdpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{11, 21}, {31, 41}},
                            new Integer[][] {{11, 41}},
                            null,
                            null,
                            "port-v18-xxxhdpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            null,
                            null,
                            null,
                            null,
                            "car-v18-xxxhdpi",
                            false
                        },
                        {"9patch-6-best-match.json", "input_name6",
                            new Integer[][] {{14, 24}, {34, 44}},
                            new Integer[][] {{14, 44}},
                            null,
                            null,
                            "port-car-v18-notouch-navhidden-sw400dp-ldpi",
                            true
                        },
                        {"9patch-6-best-match.json", "input_name6",
                        	null,
                            null,
                            null,
                            null,
                            "car-ldpi",
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
            assertTrue(resultExpected ^ (ninePatch == null));

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
            assertNull(expected);
        } else {
            assertNotNull(expected);
            assertEquals(actual.length, expected.length);
            for (int i = 0; i < actual.length; i++) {
                assertNotNull(expected[i]);
                assertNotNull(actual[i]);
                assertEquals(expected[i].length, actual[i].length);
                for (int j = 0; j < actual.length; j++) {
                    assertNotNull(expected[i][j]);
                    assertEquals(expected[i][j].intValue(), actual[i][j]);
                }
            }
        }
    }

}
