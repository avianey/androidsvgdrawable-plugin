package fr.avianey.mojo.androidgendrawable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import fr.avianey.mojo.androidgendrawable.Qualifier.Type;

public class NinePatchMap {
    
    private final Map<String, Entry<Pattern, Set<NinePatch>>> entries = new HashMap<String, Entry<Pattern, Set<NinePatch>>>(); 
    
    /**
     * Get the {@link NinePatch} that <strong>best match</strong> the desired {@link QualifiedResource} :
     * <ol>
     * <li>The {@link QualifiedResource} name match the {@link NinePatch} regexp</li>
     * <li>The {@link QualifiedResource} qualifiers contains all the {@link NinePatch} ones</li>
     * <li>No other {@link NinePatch} verifying 1) and 2) and containing more qualifiers</li>
     * </ol>
     * @param name
     * @param requiredQualifiers
     * @return
     */
    public NinePatch getBestMatch(QualifiedResource svg) {
        Set<NinePatch> ninePatchSet = getMatching(svg.getName());
        if (ninePatchSet == null) {
            // the resource is not a NinePatch
            return null;
        } else {
            Map<Type, String> _qualifiers = new HashMap<Type, String>(svg.getTypedQualifiers());
            _qualifiers.remove(Type.density);
            NinePatch _ninePatch = null;
            for (NinePatch ninePatch : ninePatchSet) {
                if (_qualifiers.isEmpty() && ninePatch.getTypedQualifiers().isEmpty()) {
                	// no qualifiers in resource
                	// no qualifier in ninepatch
                	// => exact match (first one found)
                    return ninePatch;
                } else if (!_qualifiers.isEmpty()) {
                	// resource qualifiers list is not empty
                	// ensure that all ninepatch qualifier types are covered
                    if (_qualifiers.keySet().containsAll(ninePatch.getTypedQualifiers().keySet())) {
                    	// resource qualifier types are compatible
                    	// check the qualifier values
                        boolean matches = true;
                        for (Type t : ninePatch.getTypedQualifiers().keySet()) {
                            if (!ninePatch.getTypedQualifiers().get(t).equals(_qualifiers.get(t))) {
                                matches = false;
                                break;
                            }
                        }
                        // if values are OK, check if the current ninepatch covers more qualifiers than the previously matching ninePatch 
                        if (matches && (_ninePatch == null || ninePatch.getTypedQualifiers().keySet().containsAll(_ninePatch.getTypedQualifiers().keySet()))) {
                            // nine patch covers all of the requirements 1) and 2)
                            // and no best (containing more resource qualifier types) nine patch was already discovered
                            _ninePatch = ninePatch;
                            if (_ninePatch.getTypedQualifiers().size() == _qualifiers.size()) {
                                // cannot be better
                            	// => exact match (first one found)
                                break;
                            }
                        }
                    } else {
                        // ninepatch is more restrictive as it contains at least one qualifier type
                    	// the resource does not contains
                    	// => skip it
                        continue;
                    }
                } else {
                	// ninepatch has no qualifier requirement
                	// resource is qualified so the ninepatch cannot apply
                	// => skip it
                	continue;
                }
            }
            return _ninePatch;
        }
    }

	private Set<NinePatch> getMatching(final String svgName) {
		final Set<NinePatch> ninePatchSet = new HashSet<NinePatch>();
		for (Entry<Pattern, Set<NinePatch>> e : entries.values()) {
			if (e.getKey().matcher(svgName).matches()) {
				ninePatchSet.addAll(e.getValue());
			}
		}
		return ninePatchSet;
	}

	public Set<NinePatch> get(final String regexp) {
		Entry<Pattern, Set<NinePatch>> e = entries.get(regexp);
		return e == null ? null : e.getValue();
	}

	public Set<NinePatch> put(final String regexp, Set<NinePatch> value) {
		Entry<Pattern, Set<NinePatch>> e = entries.get(regexp);
		if (e == null) {
		    e = new AbstractMap.SimpleEntry<Pattern, Set<NinePatch>>(Pattern.compile(regexp), value);
		} else {
		    e.getValue().addAll(value);
		}
		entries.put(regexp, e);
		return value;
	}

	public Set<NinePatch> remove(final String regexp) {
		Entry<Pattern, Set<NinePatch>> e = entries.remove(regexp);
		return e == null ? null : e.getValue();
	}
    
}
