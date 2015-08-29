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

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import fr.avianey.androidsvgdrawable.Qualifier.Type;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * A qualified {@link File} with a least a density qualifier.
 *
 * @author antoine vianey
 */
public class QualifiedResource extends File {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Density density;
    private final EnumMap<Type, String> typedQualifiers;

    public QualifiedResource(final File file, final String name, final EnumMap<Type, String> qualifiers) {
        super(file.getAbsolutePath());
        this.name = name;
        this.typedQualifiers = qualifiers;
        this.density = Density.from(typedQualifiers.get(Type.density));
    }

    public File getOutputFor(final Density.Value density, final File to, final OutputType outputType) {
        StringBuilder builder = new StringBuilder(outputType.name());
        EnumMap<Type, String> qualifiers = new EnumMap<>(typedQualifiers);
        qualifiers.remove(Type.density);
        qualifiers.put(Type.density, density.name());
        builder.append(Qualifier.toQualifiedString(qualifiers));
        return new File(to, builder.toString());
    }

    /**
     * Create a {@link QualifiedResource} from an input SVG file.
     * @param file
     * @return
     */
    public static final QualifiedResource fromFile(final File file) {
        checkNotNull(file);
        final String fileName = getBaseName(file.getAbsolutePath());
        checkArgument(fileName.length() > 0);
        checkArgument(fileName.indexOf("-") > 0, "No qualifier for input svg file " + fileName);

        // unqualified name
        final String unqualifiedName = fileName.substring(0, fileName.indexOf("-"));
        checkArgument(unqualifiedName != null && unqualifiedName.matches("\\w+"));

        // qualifiers
        final EnumMap<Type, String> typedQualifiers = Qualifier.fromQualifiedString(fileName.substring(fileName.indexOf("-") + 1));

        // a density qualifier must be provided
        checkNotNull(typedQualifiers.get(Type.density));

        return new QualifiedResource(file, unqualifiedName, typedQualifiers);
    }

    public Map<Type, String> getTypedQualifiers() {
        return typedQualifiers;
    }

    public String getName() {
        return name;
    }

    public Density getDensity() {
        return density;
    }

    public String toString() {
    	return FilenameUtils.getName(getAbsolutePath());
    }

}
