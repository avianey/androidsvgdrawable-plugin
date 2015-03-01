package fr.avianey.androidsvgdrawable;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dhleong
 */
public class Colorizer {

    private static final Pattern FILL_STYLE = Pattern.compile("fill:([^;]+);");

    static class ColorMap {
        String from, to;
    }

    String[] matches;
    ColorMap[] colors;

    private List<Pattern> patterns;

    public boolean matches(QualifiedResource qr) {
        for (Pattern pattern : getPatterns()) {
            if (pattern.matcher(qr.getName()).find())
                return true;
        }

        return false;
    }

    public Document colorize(final QualifiedResource svg) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(svg);

            colorize(doc);
            return doc;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void colorize(final Node doc) {
        final NamedNodeMap attrs = doc.getAttributes();
        if (attrs != null) {
            colorize(attrs);
        }

        final NodeList kids = doc.getChildNodes();
        if (kids == null)
            return;

        final int len = kids.getLength();
        for (int i=0; i < len; i++) {
            final Node node = kids.item(i);
            colorize(node);
        }
    }

    private void colorize(final NamedNodeMap attrs) {
        final Node fill = attrs.getNamedItem("fill");
        if (fill != null) {
            final String color = fill.getTextContent();
            final String replacement = replace(color);
            if (replacement != null) {
                fill.setTextContent(replacement);
                return;
            }
        }

        final Node style = attrs.getNamedItem("style");
        if (style != null) {
            final String content = style.getTextContent();
            Matcher m = FILL_STYLE.matcher(content);
            if (m == null || !m.find()) {
                return;
            }

            final String color = m.group(1);
            final String replacement = replace(color);
            if (replacement != null) {
                final String fixed = m.replaceFirst("fill:" + replacement + ";");
                style.setTextContent(fixed);
                return;
            }
        }
    }

    private String replace(final String color) {
        if (color == null) {
            return null;
        }

        for (ColorMap map : colors) {
            if (color.equals(map.from)
                    && null != map.to) {
                return map.to;
            }
        }

        return null;
    }

    private List<Pattern> getPatterns() {
        if (patterns != null)
            return patterns;

        // lazy init
        patterns = new ArrayList<Pattern>(matches.length);
        for (final String pattern : matches) {
            patterns.add(Pattern.compile(pattern));
        }

        return patterns;
    }
}
