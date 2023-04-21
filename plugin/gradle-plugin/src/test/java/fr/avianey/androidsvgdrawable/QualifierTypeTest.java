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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class QualifierTypeTest {

    private String input;
    private Type type;
    private final boolean successExpected;

    public QualifierTypeTest(String input, Type type, boolean successExpected) {
        this.input = input;
        this.successExpected = successExpected;
        this.type = type;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        {"v26", Type.platformVersion, true},
                        {"long", Type.aspect, true},
                        {"notlong", Type.aspect, true},
                        {"mdpi", Type.aspect, false},
                        {"round", Type.round, true},
                        {"notround", Type.round, true},
                        {"ldpi", Type.density, true},
                        {"hldpi", Type.density, false},
                        {"wldpi", Type.density, false},
                        {"w32ldpi", Type.density, true},
                        {"w32ldpi", Type.density, true},
                        {"w32mdpi", Type.density, true},
                        {"w32hdpi", Type.density, true},
                        {"w32xhdpi", Type.density, true},
                        {"w32xxhdpi", Type.density, true},
                        {"w32xxxhdpi", Type.density, true},
                        {"h32mdpi", Type.density, true}
                });
    }

    @Test
    public void parse() {
        Matcher m = Pattern.compile(type.getRegexp()).matcher(input);
        assertEquals(successExpected, m.matches());
    }

}
