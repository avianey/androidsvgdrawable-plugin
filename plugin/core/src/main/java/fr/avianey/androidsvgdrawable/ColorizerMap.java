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
package fr.avianey.androidsvgdrawable;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.avianey.androidsvgdrawable.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dhleong
 */
public class ColorizerMap {

    private final Set<Colorizer> colorizers = new HashSet<>();

    public Colorizer getBestMatch(QualifiedResource qr) {
        for (Colorizer c : colorizers) {
            if (c.matches(qr))
                return c;
        }

        return null;
    }

    public static ColorizerMap from(final File file, final Log log) {

        final ColorizerMap map = new ColorizerMap();
        log.info("Loading ColorizeMap configuration file " + file);
        try (final Reader reader = new FileReader(file)) {
            Type t = new TypeToken<Set<Colorizer>>(){}.getType();
            Set<Colorizer> colorizerSet = (Set<Colorizer>) (new GsonBuilder().create().fromJson(reader, t));
            map.colorizers.addAll(colorizerSet);
        } catch (IOException e) {
            log.error(e);
        }

        return map;
    }
}
