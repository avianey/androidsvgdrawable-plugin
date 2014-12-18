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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import fr.avianey.androidsvgdrawable.Qualifier.Type;

/**
 * @author antoine vianey
 */
public class NinePatchMap {
    
    private final Map<String, Entry<Pattern, Set<NinePatch>>> entries = new HashMap<>(); 
    
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
        Set<NinePatch> matchingNinePatches = getMatching(svg.getName());
        if (matchingNinePatches == null) {
            // the resource is not a NinePatch
            return null;
        } else {
            Map<Type, String> svgQualifiers = new HashMap<>(svg.getTypedQualifiers());
            svgQualifiers.remove(Type.density);
            NinePatch bestMatchingNinePatch = null;
            for (NinePatch ninePatch : matchingNinePatches) {
                if (svgQualifiers.isEmpty() && ninePatch.getTypedQualifiers().isEmpty()) {
                	// no qualifiers in resource
                	// no qualifier in ninepatch
                	// => exact match (first one found)
                    return ninePatch;
                } else if (!svgQualifiers.isEmpty()) {
                	// resource qualifiers list is not empty
                	// ensure that all ninepatch qualifier types are covered
                    if (svgQualifiers.keySet().containsAll(ninePatch.getTypedQualifiers().keySet())) {
                    	// resource qualifier types are compatible
                    	// check the qualifier values
                        boolean matches = true;
                        for (Type t : ninePatch.getTypedQualifiers().keySet()) {
                            if (!ninePatch.getTypedQualifiers().get(t).equals(svgQualifiers.get(t))) {
                                matches = false;
                                break;
                            }
                        }
                        // if values are OK, check if the current ninepatch covers more qualifiers than the previously matching ninePatch 
                        if (matches && (bestMatchingNinePatch == null || ninePatch.getTypedQualifiers().keySet().containsAll(bestMatchingNinePatch.getTypedQualifiers().keySet()))) {
                            // nine patch covers all of the requirements 1) and 2)
                            // and no best (containing more resource qualifier types) nine patch was already discovered
                            bestMatchingNinePatch = ninePatch;
                            if (bestMatchingNinePatch.getTypedQualifiers().size() == svgQualifiers.size()) {
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
            return bestMatchingNinePatch;
        }
    }

	private Set<NinePatch> getMatching(final String svgName) {
		final Set<NinePatch> ninePatchSet = new HashSet<>();
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
		    e = new AbstractMap.SimpleEntry<>(Pattern.compile(regexp), value);
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
