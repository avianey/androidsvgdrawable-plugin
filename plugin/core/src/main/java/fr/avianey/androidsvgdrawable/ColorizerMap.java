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
