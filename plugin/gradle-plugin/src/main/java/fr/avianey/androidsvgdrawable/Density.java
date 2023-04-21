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

import fr.avianey.androidsvgdrawable.ConstrainedDensity.Side;
import fr.avianey.androidsvgdrawable.Qualifier.Type;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Density {

    private static final Pattern PATTERN = Pattern.compile(Type.density.getRegexp());

    public static Density from(String value) {
        Matcher m = PATTERN.matcher(value);
        checkArgument(m.matches());
        if (m.group(1) != null && m.group(2) != null) {
            // constrained density
            return new ConstrainedDensity(Value.valueOf(m.group(3)), Side.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
        } else {
            // relative density
            return new RelativeDensity(Value.valueOf(value));
        }
    }

    public enum Value {

        ldpi(120), mdpi(160), hdpi(240), xhdpi(320), tvdpi(213), xxhdpi(480), xxxhdpi(640);

        private final int dpi;

        Value(int dpi) {
            this.dpi = dpi;
        }

        public int getDpi() {
            return dpi;
        }
    }

    private final Density.Value value;

    public Density(Value value) {
        checkNotNull(value);
        this.value = value;
    }

    public int getDpi() {
        return value.dpi;
    }

    public Density.Value getValue() {
        return value;
    }

    /**
     * Compute the scale ratio for the output drawable
     * @param bounds the input svg bounds
     * @param target the targeted density
     * @return
     */
    public abstract double ratio(Rectangle bounds, Density.Value target);

    public abstract String toString();
}
