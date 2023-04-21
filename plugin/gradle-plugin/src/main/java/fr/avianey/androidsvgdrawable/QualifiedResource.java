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

import java.awt.*;
import java.io.File;
import java.util.Map;

public abstract class QualifiedResource extends File {

    protected QualifiedResource(String path) {
        super(path);
    }

    public abstract File getOutputFor(final Density.Value density, final File to, final OutputType outputType, final Density.Value noDpiDensity);

    public abstract Map<Type, String> getTypedQualifiers();

    public abstract String getName();

    public abstract Density getDensity();

    public abstract Rectangle getBounds();

    public abstract Rectangle getScaledBounds(Density.Value targetDensity);

}
