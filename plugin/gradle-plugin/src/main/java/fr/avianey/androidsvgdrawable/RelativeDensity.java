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

public class RelativeDensity extends Density {

    public RelativeDensity(Value value) {
        super(value);
    }

    @Override
    public double ratio(Rectangle bounds, Density.Value target) {
        return (double) target.getDpi() / (double) this.getDpi();
    }

    @Override
    public String toString() {
        return getValue().name();
    }

}
