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

import fr.avianey.androidsvgdrawable.Qualifier.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static fr.avianey.androidsvgdrawable.Qualifier.fromQualifiedString;
import static fr.avianey.androidsvgdrawable.Qualifier.toQualifiedString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class QualifiedStringTest {

    private final String inputName;
    private final String outputName;
    private final Map<Type, String> typedQualifiers;

    public QualifiedStringTest(String name, Object[][] qualifiers, String outputName) {
        this.inputName = name;
        this.typedQualifiers = new EnumMap<>(Type.class);
        if (qualifiers != null) {
            for (Object[] o : qualifiers) {
                typedQualifiers.put((Type) o[0], (String) o[1]);
            }
        }
        this.outputName = outputName == null ? "" : "-" + outputName;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        // passing cases
                        {"ldpi", new Object[][] {{Type.density, "ldpi"}}, "ldpi"},
                        {"mdpi", new Object[][] {{Type.density, "mdpi"}}, "mdpi"},
                        {"hdpi", new Object[][] {{Type.density, "hdpi"}}, "hdpi"},
                        {"xhdpi", new Object[][] {{Type.density, "xhdpi"}}, "xhdpi"},
                        {"xxhdpi", new Object[][] {{Type.density, "xxhdpi"}}, "xxhdpi"},
                        {"xxxhdpi", new Object[][] {{Type.density, "xxxhdpi"}}, "xxxhdpi"},
                        {"tvdpi", new Object[][] {{Type.density, "tvdpi"}}, "tvdpi"},
                        {"ldpi", new Object[][] {{Type.density, "ldpi"}}, "ldpi"},
                        {"mdpi", new Object[][] {{Type.density, "mdpi"}}, "mdpi"},
                        {"nodpi", new Object[][] {{Type.density, "nodpi"}}, "nodpi"},
                        {"h32mdpi", new Object[][] {{Type.density, "h32mdpi"}}, "h32mdpi"},
                        {"w32mdpi", new Object[][] {{Type.density, "w32mdpi"}}, "w32mdpi"},
                        {"h0mdpi", new Object[][] {{Type.density, "h0mdpi"}}, "h0mdpi"},
                        {"w0mdpi", new Object[][] {{Type.density, "w0mdpi"}}, "w0mdpi"},
                        {"mcc310-mnc004-en-rUS-xxxhdpi-land", new Object[][] {
                                {Type.density, "xxxhdpi"},
                                {Type.locale, "en-rUS"},
                                {Type.orientation, "land"},
                                {Type.mcc_mnc, "mcc310-mnc004"}
                        }, "mcc310-mnc004-en-rUS-land-xxxhdpi"},
                        {"en-rUS-mcc310-mnc004-xxhdpi-land", new Object[][] {
                                {Type.density, "xxhdpi"},
                                {Type.locale, "en-rUS"},
                                {Type.orientation, "land"},
                                {Type.mcc_mnc, "mcc310-mnc004"}
                        }, "mcc310-mnc004-en-rUS-land-xxhdpi"},
                        {"ldpi-sw100dp-h400dp-w731dp-v19-port-xlarge", new Object[][] {
                                {Type.density, "ldpi"},
                                {Type.smallestWidth, "sw100dp"},
                                {Type.availableHeight, "h400dp"},
                                {Type.availableWidth, "w731dp"},
                                {Type.platformVersion, "v19"},
                                {Type.orientation, "port"},
                                {Type.screenSize, "xlarge"}
                        }, "sw100dp-w731dp-h400dp-xlarge-port-ldpi-v19"},
                        {"mdpi-fr-land", new Object[][] {
                                {Type.density, "mdpi"},
                                {Type.locale, "fr"},
                                {Type.orientation, "land"}
                        }, "fr-land-mdpi"},
                        {"mdpi-fr-land", new Object[][] {
                                {Type.density, "mdpi"},
                                {Type.locale, "fr"},
                                {Type.orientation, "land"}
                        }, "fr-land-mdpi"},
                        // error cases
                        {"mdpilandfr", null, null},
                        {"32mdpi", null, null},
                        {"a32mdpi", null, null},
                        {"", null, null},
                        {null, null, null},
                        {"name", null, null}
                });
    }

    @Test
    public void test() {
        Map<Qualifier.Type, String> parsedQualifiers = fromQualifiedString(inputName);
        assertTrue(parsedQualifiers.keySet().containsAll(typedQualifiers.keySet()) && typedQualifiers.keySet().containsAll(parsedQualifiers.keySet()));
        for (Type t : typedQualifiers.keySet()) {
            assertEquals(typedQualifiers.get(t), parsedQualifiers.get(t));
        }
        // verify inputName
        assertEquals(outputName, toQualifiedString(parsedQualifiers));
    }

}
