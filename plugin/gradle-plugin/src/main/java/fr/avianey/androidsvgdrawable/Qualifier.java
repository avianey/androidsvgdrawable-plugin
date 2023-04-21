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

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that parse or generates qualified resource names
 *
 * @author antoine vianey
 */
public final class Qualifier {

    private static class Acceptor {

        private final String regexp;

        public Acceptor(Type type) {
            // (capturingregexp)(-.*)*
            this.regexp = new StringBuilder("(")
                    .append(type.getRegexp())
                    .append(")")
                    .append("(-.*)?")
                    .toString();
        }

        /**
         * Return the {@link Qualifier} if found at the <u>beginning</u> of the input {@link String}.
         * If the {@link Qualifier} exists but is not at the beginning of the input {@link String},
         * then the input is not a valid resource directory name...
         * @param input
         * @return
         *      The qualifier value or null if no qualifier of the desired {@link Type} is found
         *      at the <u>beginning</u> of the input {@link String}.
         */
        public String accept(String input) {
            Pattern p = Pattern.compile(regexp());
            Matcher m = p.matcher(input);
            String qualifier = null;
            if (m.matches() && m.groupCount() > 0) {
                qualifier = m.group(1);
            }
            return qualifier;
        }

        /**
         * Return the {@link Pattern} {@link String} for the desired {@link Qualifier.Type}
         * @return
         *      the Regexp
         */
        public String regexp() {
            return regexp;
        }

    }

    /**
     * Qualifier types in order of precedence.<br/>
     * <a href="http://developer.android.com/guide/topics/resources/providing-resources.html">Providing Resources</a>
     * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/content/res/Configuration.java
     */
    public enum Type {
        mcc_mnc("mcc\\d+(?:-mnc\\d+)?"),
        locale("[a-zA-Z]{2}(?:-r[a-zA-Z]{2})?"), // TODO : verify from Locale.class
        layoutDirection("ldrtl|ldltr"),
        smallestWidth("sw\\d+dp"),
        availableWidth("w\\d+dp"),
        availableHeight("h\\d+dp"),
        screenSize("small|normal|large|xlarge"),
        aspect("(?:not)?long"),
        round("(?:not)?round"),
        colorGamut("(?:no)?widecg"),
        hdr("highdr|lowdr"),
        orientation("port|land"),
        uiMode("car|desk|television|appliance|watch|vrheadset"),
        nightMode("(?:not)?night"),
        /**
         * Regular qualifier part is (?:l|m|x{0,3}h|tv|no)dpi
         * 3 groups :
         * - w|h
         * - pixel size
         * - density
         */
        density("(?:(w|h)(\\d+))?((?:l|m|x{0,3}h|tv|no)dpi)"),
        touchScreen("notouch|finger"),
        keyboard("keysexposed|keyshidden|keyssoft"),
        textInputMethod("nokeys|qwerty|12key"),
        navigationKey("nav(?:exposed|hidden)"),
        nonTouchNavigationMethod("nonav|dpad|trackball|wheel"),
        platformVersion("v\\d+"); // TODO : verify validity version code numbers

        private final String regexp;

        private Type(String regexp) {
            this.regexp = regexp;
        }

        public String getRegexp() {
            return regexp;
        }

    }

    /**
     * Returns the String representing the qualifiers in the Android platform expected order
     * @param qualifiers
     * @return
     */
    static String toQualifiedString(final Map<Type, String> qualifiers) {
    	StringBuilder builder = new StringBuilder("");
        for (Type type : qualifiers.keySet()) {
            builder.append("-");
            builder.append(qualifiers.get(type));
        }
        return builder.toString();
    }

    /**
     * Parse a qualified String into a {@link Map} of qualified values
     * @param qualifiedString
     * @return
     */
    static Map<Type, String> fromQualifiedString(final String qualifiedString) {
        final Map<Type, String> typedQualifiers = new EnumMap<>(Type.class);
        if (qualifiedString == null) {
            return typedQualifiers;
        }
        String qualifiers = qualifiedString;
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
        return typedQualifiers;
    }

}
