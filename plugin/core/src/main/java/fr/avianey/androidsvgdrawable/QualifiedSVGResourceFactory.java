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

import fr.avianey.androidsvgdrawable.Qualifier.Type;
import fr.avianey.androidsvgdrawable.batik.DensityAwareUserAgent;
import fr.avianey.androidsvgdrawable.util.Log;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.parser.UnitProcessor;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.*;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public class QualifiedSVGResourceFactory {

    private final Log log;
    private final BoundsType boundsType;

    public QualifiedSVGResourceFactory(Log log, BoundsType boundsType) {
        this.log = log;
        this.boundsType = boundsType;
    }

    public QualifiedResource fromSVGFile(final File file) throws IOException {
        checkNotNull(file);
        final String fileName = getBaseName(file.getAbsolutePath());
        checkArgument(fileName.length() > 0);
        checkArgument(fileName.indexOf("-") > 0, "No qualifier for input svg file " + fileName);

        // unqualified name
        final String unqualifiedName = fileName.substring(0, fileName.indexOf("-"));
        checkArgument(unqualifiedName.matches("\\w+"));

        // qualifiers
        final Map<Type, String> typedQualifiers = Qualifier.fromQualifiedString(fileName.substring(fileName.indexOf("-") + 1));

        // a density qualifier must be provided
        checkNotNull(typedQualifiers.get(Type.density));

        return new QualifiedSVGResource(file, unqualifiedName, typedQualifiers);
    }

    /**
     * Extract the viewbox of the input SVG
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private Rectangle extractSVGBounds(QualifiedSVGResource svg) throws IOException {
        // check <svg> attributes first : x, y, width, height
        SVGDocument svgDocument = getSVGDocument(svg);
        SVGSVGElement svgElement = svgDocument.getRootElement();
        if (svgElement.getAttributeNode("width") != null && svgElement.getAttribute("height") != null) {

            UserAgent userAgent = new DensityAwareUserAgent(svg.getDensity().getDpi());
            UnitProcessor.Context context = org.apache.batik.bridge.UnitProcessor.createContext(
                    new BridgeContext(userAgent), svgElement);

            float width = svgLengthInPixels(svgElement.getWidth().getBaseVal(), context);
            float height = svgLengthInPixels(svgElement.getHeight().getBaseVal(), context);
            float x = 0;
            float y = 0;
            // check x and y attributes
            if (svgElement.getX() != null && svgElement.getX().getBaseVal() != null) {
                x = svgLengthInPixels(svgElement.getX().getBaseVal(), context);
            }
            if (svgElement.getY() != null && svgElement.getY().getBaseVal() != null) {
                y = svgLengthInPixels(svgElement.getY().getBaseVal(), context);
            }

            return new Rectangle((int) floor(x), (int) floor(y), (int) ceil(width), (int) ceil(height));
        }

        // use computed bounds
        log.warn("Take time to fix desired width and height attributes of the root <svg> node for this file... " +
                "ROI will be computed by magic using Batik " + boundsType.name() + " bounds");
        return boundsType.getBounds(getGraphicsNode(svgDocument, svg.getDensity().getDpi()));
    }

    private float svgLengthInPixels(SVGLength length, UnitProcessor.Context context) {
        return UnitProcessor.svgToUserSpace(length.getValueAsString(), "px", UnitProcessor.OTHER_LENGTH, context);
    }

    private GraphicsNode getGraphicsNode(SVGDocument svgDocument, int dpi) throws IOException {
        UserAgent userAgent = new DensityAwareUserAgent(dpi);
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        GVTBuilder builder = new GVTBuilder();
        GraphicsNode rootGN = builder.build(ctx, svgDocument);
        return rootGN;
    }

    private SVGDocument getSVGDocument(QualifiedSVGResource svg) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        return (SVGDocument) f.createDocument(svg.toURI().toURL().toString());
    }

    private class QualifiedSVGResource extends QualifiedResource {

        private static final long serialVersionUID = 1L;

        private final String name;
        private final Density density;
        private final Map<Type, String> typedQualifiers;
        private final Rectangle bounds;

        private QualifiedSVGResource(final File file, final String name, final Map<Type, String> qualifiers) throws IOException {
            super(file.getAbsolutePath());
            this.name = name;
            this.typedQualifiers = qualifiers;
            this.density = Density.from(typedQualifiers.get(Type.density));
            this.bounds = extractSVGBounds(this);
        }

        @Override
        public File getOutputFor(final Density.Value density, final File to, final OutputType outputType) {
            StringBuilder builder = new StringBuilder(outputType.name());
            EnumMap<Type, String> qualifiers = new EnumMap<>(typedQualifiers);
            qualifiers.remove(Type.density);
            qualifiers.put(Type.density, density.name());
            builder.append(Qualifier.toQualifiedString(qualifiers));
            return new File(to, builder.toString());
        }

        @Override
        public Map<Type, String> getTypedQualifiers() {
            return typedQualifiers;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Density getDensity() {
            return density;
        }

        @Override
        public Rectangle getBounds() {
            return bounds;
        }

        @Override
        public Rectangle getScaledBounds(Density.Value targetDensity) {
            double ratio = getDensity().ratio(getBounds(), targetDensity);
            final int width = max((int) floor(bounds.getWidth() * ratio), 1);
            final int height = max((int) floor(bounds.getHeight() * ratio), 1);
            return new Rectangle(0, 0, width, height);
        }

        public String toString() {
            return FilenameUtils.getName(getAbsolutePath());
        }

    }

}
