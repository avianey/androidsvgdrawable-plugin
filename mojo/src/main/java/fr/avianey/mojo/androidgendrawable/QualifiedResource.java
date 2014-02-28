package fr.avianey.mojo.androidgendrawable;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Preconditions;

import fr.avianey.mojo.androidgendrawable.Qualifier.Acceptor;
import fr.avianey.mojo.androidgendrawable.Qualifier.Type;

public class QualifiedResource extends File {
    
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Density density;
    private final Map<Type, String> typedQualifiers;
    
    private QualifiedResource(final File file, final String name, final Map<Type, String> qualifiers) {
        super(file.getAbsolutePath());
        this.name = name;
        this.typedQualifiers = qualifiers;
        this.density = Density.valueOf(typedQualifiers.get(Type.density));
    }
    
    public File getOutputFor(final Density density, final File to, final Density fallback) {
        StringBuilder builder = new StringBuilder("drawable");
        for (Type type : EnumSet.allOf(Type.class)) {
            if (Type.density.equals(type)) {
                if (fallback == null || !fallback.equals(density)) {
                    // skip qualifier for fallback density
                    builder.append("-");
                    builder.append(density.toString());
                }
            } else if (typedQualifiers != null && typedQualifiers.containsKey(type)) {
                builder.append("-");
                builder.append(typedQualifiers.get(type));
            }
        }
        return new File(to, builder.toString());
    }
    
    /**
     * Create a {@link QualifiedResource} from an input SVG file.
     * @param file
     * @return
     */
    public static final QualifiedResource fromSvgFile(final File file) {
        
        Preconditions.checkNotNull(file);
        final String extension = FilenameUtils.getExtension(file.getAbsolutePath());
        final String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
        Preconditions.checkArgument(extension.toLowerCase().equals("svg"));
        Preconditions.checkArgument(fileName.length() > 0);
        Preconditions.checkArgument(fileName.indexOf("-") > 0);
        
        // unqualified name
        final String unqualifiedName = fileName.substring(0, fileName.indexOf("-"));
        Preconditions.checkArgument(unqualifiedName != null && unqualifiedName.matches("\\w+"));
        
        // qualifiers
        final Map<Type, String> typedQualifiers = new EnumMap<>(Type.class);
        String qualifiers = fileName.substring(fileName.indexOf("-") + 1);
        Preconditions.checkArgument(qualifiers.length() > 0);
        
        while (qualifiers.length() > 0) {
            // remove leading "-"
            int i = -1;
            while (qualifiers.indexOf("-", i) == i + 1) {
                i++;
            }
            if (i >= 0) {
                qualifiers = qualifiers.substring(i + 1);
            }
            
            String qualifier = null;
            for (Type type : EnumSet.allOf(Type.class)) {
                Acceptor a = new Acceptor(type);
                qualifier = a.accept(qualifiers);
                if (qualifier != null) {
                    qualifiers = qualifiers.substring(qualifier.length());
                    typedQualifiers.put(type, qualifier);
                    break;
                }
            }
            
            if (qualifier == null) {
                if (qualifiers.indexOf("-") < 0) {
                    break;
                } else {
                    qualifiers = qualifiers.substring(qualifiers.indexOf("-") + 1);
                }
            }
            
        }
        
        // a density qualifier must be provided
        Preconditions.checkNotNull(typedQualifiers.get(Type.density));
        
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