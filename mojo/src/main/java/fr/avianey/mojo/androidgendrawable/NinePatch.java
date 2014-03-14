package fr.avianey.mojo.androidgendrawable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;

import fr.avianey.mojo.androidgendrawable.Qualifier.Type;

/**
 * Describe the configuration for a 9-Patch drawable:
 * <dl>
 * <dt>Stretchable area</dt>
 * <dd>coordinates of start & stop points for segments along the x-axis</dd>
 * <dd>coordinates of start & stop points for segments along the y-axis</dd>
 * <dt>Content area</dt>
 * <dd>coordinates of start & stop points for segments along the x-axis</dd>
 * <dd>coordinates of start & stop points for segments along the y-axis</dd>
 * </dl>
 * If no segment defined for an area along an axis, the whole axis is used as a segment.
 * Coordinates must be include within the svg bounds (width and height).
 * 
 * @version 1
 * @author avianey
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
    public static final NinePatchMap init(Set<NinePatch> ninePatchSet) {
        NinePatchMap map = new NinePatchMap();
        for (NinePatch ninePatch : ninePatchSet) {
            // classify by name
            Set<NinePatch> set = map.get(ninePatch.name);
            if (set == null) {
                set = new HashSet<NinePatch>();
                map.put(ninePatch.name, set);
            }
            set.add(ninePatch);
            // extract qualifiers
            if (ninePatch.qualifiers != null) {
                ninePatch.typedQualifiers = Qualifier.fromQualifiedString(
                        Joiner.on("-").join(ninePatch.qualifiers));
            } else {
                ninePatch.typedQualifiers = Collections.EMPTY_MAP;
            }
        }
        return map;
    }

    /**
     * Normalized start of the NinePatch segment<br/>
     * The start cannot be higher than d...
     * @param start
     * @param stop
     * @param d : normalized dimension of the density specific drawable (ratio applied)
     * @param ratio
     * @return
     */
    public static final int start(int start, int stop, int d, double ratio) {
        return Math.max(
        		0, 
        		Math.min(
        				(int) Math.ceil(d * ratio) - 1, 
        				(int) Math.floor(start * ratio)));
    }
    
    /**
     * Normalized size of the NinePatch segment<br/>
     * The size cannot be lower than 1 and greater than (d - start)...
     * @param start
     * @param stop
     * @param d : normalized dimension of the density specific drawable (ratio applied)
     * @param ratio
     * @return
     */
    public static final int size(int start, int stop, int d, double ratio) {
        return Math.max(
        		1, Math.min(
		        		(int) Math.ceil(d * ratio) - start(start, stop, d, ratio), 
		        		Math.max(1, (int) Math.floor((stop - start + 1) * ratio))));
    }
    
}
