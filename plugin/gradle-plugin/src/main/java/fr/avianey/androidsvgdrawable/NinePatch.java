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

import com.google.common.base.Joiner;
import fr.avianey.androidsvgdrawable.Qualifier.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.*;
import static java.util.Collections.EMPTY_MAP;

/**
 * Describe the configuration for a 9-Patch drawable:
 * <dl>
 * <dt>Stretchable area</dt>
<<<<<<< HEAD
 * <dd>coordinates of start &amp; stop points for segments along the x-axis</dd>
 * <dd>coordinates of start &amp; stop points for segments along the y-axis</dd>
 * <dt>Content area</dt>
 * <dd>coordinates of start &amp; stop points for segments along the x-axis</dd>
 * <dd>coordinates of start &amp; stop points for segments along the y-axis</dd>
=======
 * <dd>coordinates of start & stop points for segments along the x-axis</dd>
 * <dd>coordinates of start & stop points for segments along the y-axis</dd>
 * <dt>Content area</dt>
 * <dd>coordinates of start & stop points for segments along the x-axis</dd>
 * <dd>coordinates of start & stop points for segments along the y-axis</dd>
>>>>>>> b687828 (Finish Gradle migration)
 * </dl>
 * If no segment defined for an area along an axis, the whole axis is used as a segment.
 * Coordinates must be include within the svg bounds (width and height).
 *
 * @version 1
 * @author antoine vianey
 */
public class NinePatch {

    private String name;
    private Zone stretch = new Zone();
    private Zone content = new Zone();

    // for applying nine-patch config only for some qualified inputs
    private Collection<String> qualifiers;
    private transient Map<Type, String> typedQualifiers;

    public static class Zone {

        private int[][] x;
        private int[][] y;

        /**
         * @return the x
         */
        public int[][] getX() {
            return x;
        }
        /**
         * @return the y
         */
        public int[][] getY() {
            return y;
        }

    }

    /**
     * @return the stretch
     */
    public Zone getStretch() {
        return stretch;
    }

    /**
     * @return the content
     */
    public Zone getContent() {
        return content;
    }

    /**
     * @return the typedQualifiers
     */
    public Map<Type, String> getTypedQualifiers() {
        return typedQualifiers;
    }

    @SuppressWarnings("unchecked")
    public static NinePatchMap init(Set<NinePatch> ninePatchSet) {
        NinePatchMap map = new NinePatchMap();
        for (NinePatch ninePatch : ninePatchSet) {
            // classify by name
            Set<NinePatch> set = map.get(ninePatch.name);
            if (set == null) {
                set = new HashSet<>();
                map.put(ninePatch.name, set);
            }
            set.add(ninePatch);
            // extract qualifiers
            if (ninePatch.qualifiers != null) {
                ninePatch.typedQualifiers = Qualifier.fromQualifiedString(Joiner.on("-").join(ninePatch.qualifiers));
            } else {
                ninePatch.typedQualifiers = EMPTY_MAP;
            }
        }
        return map;
    }

    /**
     * Normalized start of the NinePatch segment<br/>
     * The start cannot be higher than d...
     * @param start
     * @param d
     * @param ratio
     * @return
     */
    public static int start(int start, int d, double ratio) {
        return max(0, min(d - 1, (int) floor(start * ratio)));
    }

    /**
     * Normalized size of the NinePatch segment<br/>
     * The size cannot be lower than 1 and greater than (d - start)...
     * @param start
     * @param stop
     * @param d
     * @param ratio
     * @return
     */
    public static int size(int start, int stop, int d, double ratio) {
        return max(1, min(d - start(start, d, ratio), max(1, (int) floor((stop - start + 1) * ratio))));
    }

}
