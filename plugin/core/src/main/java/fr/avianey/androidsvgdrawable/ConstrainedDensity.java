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


import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConstrainedDensity extends Density {

    enum Side {
        h, w;
    }

    private final Side side;
    private final int size;

    public ConstrainedDensity(Value value, Side side, int size) {
        super(value);
        checkNotNull(side);
        checkNotNull(size);
        this.side = side;
        this.size = size;
    }

    @Override
    public double ratio(Rectangle bounds, Density.Value target) {
        switch (side) {
            case w:
                return ((double) size / bounds.getWidth()) * (double) target.getDpi() / (double) this.getDpi();
            case h:
            default:
                return ((double) size / bounds.getHeight()) * (double) target.getDpi() / (double) this.getDpi();
        }
    }

    @Override
    public String toString() {
        return side.name() + String.valueOf(size) + getValue().name();
    }
}
