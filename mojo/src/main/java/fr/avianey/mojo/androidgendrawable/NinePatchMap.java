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
     * Get the {@link NinePatch} that match the desired name and all of the {@link Qualifier}
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
                    return ninePatch;
                } else if (!_qualifiers.isEmpty() && !ninePatch.getTypedQualifiers().isEmpty()) {
                    if (_qualifiers.keySet().containsAll(ninePatch.getTypedQualifiers().keySet())) {
                        boolean matches = true;
                        for (Type t : ninePatch.getTypedQualifiers().keySet()) {
                            if (!_qualifiers.get(t).equals(ninePatch.getTypedQualifiers().get(t))) {
                                matches = false;
                                break;
                            }
                        }
                        if (matches && (_ninePatch == null || ninePatch.getTypedQualifiers().keySet().containsAll(_ninePatch.getTypedQualifiers().keySet()))) {
                            // nine patch covers all of the requirements
                            // and no best nine patch was already discovered
                            _ninePatch = ninePatch;
                            if (_ninePatch.getTypedQualifiers().size() == _qualifiers.size()) {
                                // cannot be better
                                break;
                            }
                        }
                    } else {
                        // nine patch is more restrictive
                        continue;
                    }
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
		Entry<Pattern, Set<NinePatch>> e = new AbstractMap.SimpleEntry<Pattern, Set<NinePatch>>(Pattern.compile(regexp), value);
		entries.put(regexp, e);
		return value;
	}

	public Set<NinePatch> remove(final String regexp) {
		Entry<Pattern, Set<NinePatch>> e = entries.remove(regexp);
		return e == null ? null : e.getValue();
	}
    
}
