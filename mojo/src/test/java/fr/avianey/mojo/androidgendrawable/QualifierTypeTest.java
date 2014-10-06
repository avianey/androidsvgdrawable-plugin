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
package fr.avianey.mojo.androidgendrawable;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.avianey.mojo.androidgendrawable.Qualifier.Type;

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
                        {"long", Type.aspect, true},
                        {"notlong", Type.aspect, true},
                        {"mdpi", Type.aspect, false}
                });
    }
    
    @Test
    public void parse() {
        Matcher m = Pattern.compile(type.getRegexp()).matcher(input);
        Assert.assertEquals(successExpected, m.matches());
        if (successExpected) {
            Assert.assertEquals(0, m.groupCount());
        }
    }
    
}
