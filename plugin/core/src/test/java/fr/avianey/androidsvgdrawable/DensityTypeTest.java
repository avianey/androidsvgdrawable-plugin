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

import org.apache.batik.transcoder.TranscoderException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static fr.avianey.androidsvgdrawable.Density.Value.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DensityTypeTest {

    private final String input;
    private final Class<? extends Density> c;
    private final Density.Value density;
    private final int w;
    private final int h;
    private final Density.Value ratioDensity;
    private final double expectedRatio;

    public DensityTypeTest(String input, Class<? extends Density> c, Density.Value density, int w, int h, Density.Value ratioDensity, double expectedRatio) {
        this.input = input;
        this.c = c;
        this.density = density;
        this.w = w;
        this.h = h;
        this.ratioDensity = ratioDensity;
        this.expectedRatio = expectedRatio;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
        				new Object[][] {
                                // 3:4:6:8:12:16
                                {"ldpi", RelativeDensity.class, ldpi, 3, 3, mdpi, 4.0 / 3.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, mdpi, 1.0},
                                {"hdpi", RelativeDensity.class, hdpi, 6, 6, mdpi, 4.0 / 6.0},
                                {"xhdpi", RelativeDensity.class, xhdpi, 8, 8, mdpi, 4.0 / 8.0},
                                {"xxhdpi", RelativeDensity.class, xxhdpi, 12, 12, mdpi, 4.0 / 12.0},
                                {"xxxhdpi", RelativeDensity.class, xxxhdpi, 16, 16, mdpi, 4.0 / 16.0},
                                // reverse 3:4:6:8:12:16
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, ldpi, 3.0 / 4.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, mdpi, 1.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, hdpi, 6.0 / 4.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, xhdpi, 8.0 / 4.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, xxhdpi, 12.0 / 4.0},
                                {"mdpi", RelativeDensity.class, mdpi, 4, 4, xxxhdpi, 16.0 / 4.0},
                                // constrained density
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, ldpi, 32.0 / 4.0 * 3.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 8, 4, ldpi, 32.0 / 8.0 * 3.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 8, ldpi, 32.0 / 4.0 * 3.0 / 4.0},
                                {"h32mdpi", ConstrainedDensity.class, mdpi, 4, 8, ldpi, 32.0 / 8.0 * 3.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, mdpi, 32.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 8, 4, mdpi, 32.0 / 8.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 8, mdpi, 32.0 / 4.0},
                                {"h32mdpi", ConstrainedDensity.class, mdpi, 4, 8, mdpi, 32.0 / 8.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, hdpi, 32.0 / 4.0 * 6.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, xhdpi, 32.0 / 4.0 * 8.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, xxhdpi, 32.0 / 4.0 * 12.0 / 4.0},
                                {"w32mdpi", ConstrainedDensity.class, mdpi, 4, 4, xxxhdpi, 32.0 / 4.0 * 16.0 / 4.0},
                        }
        		);
    }

    @Test
    public void test() throws IOException, TranscoderException, InstantiationException, IllegalAccessException {
        // parsing
    	Density d = Density.from(input);
        assertEquals(d.getClass(), c);
        assertEquals(d.getValue(), density);

        // scaling
        Rectangle inputBounds = new Rectangle(w, h);
        double ratio = d.ratio(inputBounds, ratioDensity);
        assertEquals(ratio, expectedRatio, 0);
    }

}
