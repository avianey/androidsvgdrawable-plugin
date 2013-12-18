package fr.avianey.mojo.androidgendrawable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.avianey.mojo.androidgendrawable.Qualifier.Type;

public class NinePatchMap extends HashMap<String, Set<NinePatch>> {

    private static final long serialVersionUID = 1L;
    
    /**
     * Get the {@link NinePatch} that match the desired name and all of the {@link Qualifier}
     * @param name
     * @param requiredQualifiers
     * @return
     */
    public NinePatch get(QualifiedResource svg) {
        Set<NinePatch> ninePatchSet = get(svg.getName());
        if (ninePatchSet == null) {
            // the resource is not a NinePatch
            return null;
        } else {
            Map<Type, String> _qualifiers = new HashMap<>(svg.getTypedQualifiers());
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
    
}
