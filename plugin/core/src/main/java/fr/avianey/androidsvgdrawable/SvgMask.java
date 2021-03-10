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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author antoine vianey
 */
public class SvgMask {

	private static final Pattern REF_PATTERN = Pattern.compile("^#\\{(.*)\\}$");

	private final QualifiedResource svgMask;

	public SvgMask(final QualifiedResource svgMask) {
		this.svgMask = svgMask;
	}

    /**
     * Generates masked SVG files for each matching combination of available SVG.
	 *
     * @param qualifiedSVGResourceFactory
     * @param dest
     * @param availableResources
     * @param useSameSvgOnlyOnceInMask
     * @param overwriteMode
     * @return
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
	public Collection<QualifiedResource> generatesMaskedResources(
			QualifiedSVGResourceFactory qualifiedSVGResourceFactory,
	        File dest, final Collection<QualifiedResource> availableResources,
			final boolean useSameSvgOnlyOnceInMask,
			final OverwriteMode overwriteMode) throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// generates output directory
		dest.mkdirs();

		// parse mask
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		dfactory.setNamespaceAware(true);
		DocumentBuilder builder = dfactory.newDocumentBuilder();
		Document svgmaskDom = builder.parse(svgMask);
		final String svgNamespace = svgmaskDom.getDocumentElement().getNamespaceURI();

		// extract image node
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                String uri = null;
                if (prefix.equals("_svgdrawable")) {
                    uri = svgNamespace;
                }
                return uri;
            }
            @Override
            public Iterator<String> getPrefixes(String val) {
                throw new IllegalAccessError("Not implemented!");
            }
            @Override
            public String getPrefix(String uri) {
                throw new IllegalAccessError("Not implemented!");
            }
        });

        // use dummy '_svgdrawable' prefix which is unlikely to be set for the svg namespace
        NodeList value = (NodeList) xPath.evaluate("//_svgdrawable:image", svgmaskDom, XPathConstants.NODESET);
		List<MaskNode> maskNodes = new ArrayList<>();
		for (int i = 0; i < value.getLength(); i++) {
			Node imageNode = value.item(i);
			Node href = imageNode.getAttributes().getNamedItemNS("http://www.w3.org/1999/xlink", "href");
			if (href != null && href.getNodeValue() != null) {
				Matcher m = REF_PATTERN.matcher(href.getNodeValue());
				if (m.matches()) {
					// this is a regexp to use for masking available resources
					MaskNode maskNode = new MaskNode(href, m.group(1));
					if (maskNode.accepts(availableResources)) {
						maskNodes.add(maskNode);
					} else {
						// skip mask
					}
				}
			}
		}

		final Collection<QualifiedResource> maskedResources = new ArrayList<>();

		if (!maskNodes.isEmpty()) {
			// cartesian product
			// init
			List<Iterator<QualifiedResource>> iterators = new ArrayList<>(maskNodes.size());
			List<QualifiedResource> currents = new ArrayList<>(maskNodes.size());
			for (MaskNode maskNode : maskNodes) {
				Iterator<QualifiedResource> i = maskNode.matchingResources.iterator();
				iterators.add(i);
				currents.add(i.next());
			}
			// each
			boolean hasNext = false;
			final Set<File> usedSvg = new HashSet<>();
			do {

				usedSvg.clear();
				usedSvg.addAll(currents);
				if (!useSameSvgOnlyOnceInMask || usedSvg.size() == currents.size()) {
					// we don't care about using the same svg twice or more
					// or the current combination contains distinct svg files only

					final AtomicLong lastModified = new AtomicLong(svgMask.lastModified());
					final StringBuilder tmpFileName = new StringBuilder(svgMask.getName());
					final EnumMap<Type, String> qualifiers = new EnumMap<>(Type.class);
					boolean skip = false;
					for (int i = 0; i < maskNodes.size(); i++) {
						// replace href attribute with svg file path
						QualifiedResource current = currents.get(i);
						MaskNode maskNode = maskNodes.get(i);
						maskNode.imageNode.setNodeValue("file:///" + current.getAbsolutePath());
						// concat name
						tmpFileName.append("_");
						tmpFileName.append(currents.get(i).getName());
						// concat qualifiers & verify compatibility
						// if a mask applies to two or more QualifiedResource with same Type but different values, the combination is skipped
						for (Entry<Type, String> e : qualifiers.entrySet()) {
                            if (e.getKey() == Type.density) {
                                continue;
                            }
							String qualifierValue = current.getTypedQualifiers().get(e.getKey());
							if (qualifierValue != null && !qualifierValue.equals(e.getValue())) {
								// skip the current combination
								skip = true;
								break;
							}
						}
						// union the qualifiers
						qualifiers.putAll(current.getTypedQualifiers());
						// update lastModified
						if (current.lastModified() > lastModified.get()) {
							lastModified.set(current.lastModified());
						}
					}

					if (!skip) {
						// generates masked SVG for the current combination
						// - names against the mask name and the svg name
						// - combining qualifiers (union all except density)
						// - overwriteMode support via override of lastModified() in QualifiedResource
						// - ninePatch support via regexp in ninePatchConfig
						qualifiers.remove(Type.density);
						final String name = tmpFileName.toString();
						final File maskedFile = new File(dest, name + Qualifier.toQualifiedString(qualifiers) + "-" + svgMask.getDensity().toString() + ".svg") {
							@Override
							public long lastModified() {
								return lastModified.get();
							}
						};
						qualifiers.put(Type.density, svgMask.getDensity().getValue().name());

						// write masked svg
						if (overwriteMode.shouldOverride(maskedFile, new File(maskedFile.getAbsolutePath()), null)) {
						    TransformerFactory transformerFactory = TransformerFactory.newInstance();
							Transformer transformer = transformerFactory.newTransformer();
							DOMSource source = new DOMSource(svgmaskDom);
							if (!maskedFile.exists() && !maskedFile.createNewFile()) {
								// problem occurred
								continue;
							}
							StreamResult result = new StreamResult(new FileOutputStream(maskedFile));
							transformer.transform(source, result);
						} else {
						    // no need to re-generate masked file
						    // delegates override or not to final file generation process
						}
						maskedResources.add(qualifiedSVGResourceFactory.fromSVGFile(maskedFile));

					}
				}

				// fill next combination
				hasNext = false;
				for (int i = maskNodes.size() - 1; i >= 0; i--) {
					if (iterators.get(i).hasNext()) {
						currents.set(i, iterators.get(i).next());
						hasNext = true;
						break;
					} else if (i > 0) {
						iterators.set(i, maskNodes.get(i).matchingResources.iterator());
						currents.set(i, iterators.get(i).next());
					}
				}

			} while (hasNext);
		}

		return maskedResources;
	}

	private class MaskNode {

		private final Node imageNode;
		private final String regexp; // TODO use compiled pattern
		private final List<QualifiedResource> matchingResources;

		private MaskNode(Node imageNode, String regexp) {
			this.imageNode = imageNode;
			this.regexp = regexp;
			this.matchingResources = new ArrayList<>();
		}

		/**
		 * Find valid SVG resources to mask according to :
		 * <ul>
		 * <li>SVG {@link Qualifier} must contains all of the SVGMASK {@link Qualifier}</li>
		 * <li>SVN name must match the pattern of the &lt;image&gt; node "href" attribute</li>
		 * </ul>
		 * @param availableResources available resources to use as mask
		 * @return true if matching resources have been found
		 */
		public boolean accepts(final Collection<QualifiedResource> availableResources) {
			final Map<Type, String> maskQualifiers = new HashMap<>(svgMask.getTypedQualifiers());
			maskQualifiers.remove(Type.density);
            for (QualifiedResource r : availableResources) {
                if (r.getName().matches(regexp)) {
                    Map<Type, String> svgQualifiers = new HashMap<>(r.getTypedQualifiers());
					svgQualifiers.remove(Type.density);
					if (maskQualifiers.entrySet().containsAll(svgQualifiers.entrySet())) {
						// the mask is valid for this svg
						matchingResources.add(r);
					}
				}
			}
			return !matchingResources.isEmpty();
		}

	}

}
