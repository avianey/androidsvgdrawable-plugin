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

import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ColorizerTest {

	private static final String PATH_IN  = "./target/test-classes/" + ColorizerTest.class.getSimpleName() + "/";
    private static final String PATH_OUT_SVG = "./target/generated-svg/";
    private static final String PATH_OUT_PNG = "./target/generated-svg-png/";

	private static int RUN = 0;
    private static TestParameters params;

    private final File dir;

    private static SvgDrawablePlugin plugin;
    private static File output;

    private final String input;
    private final String fromColor;
    private final String toColor;

    @BeforeClass
    public static void setup() {
        params = new TestParameters();
        params.colorizeConfig = new File(PATH_IN, "colorize.json");
        plugin = new SvgDrawablePlugin(params, new TestLogger());
        //
        output = new File(PATH_OUT_PNG);
        output.mkdirs();
    }

    public ColorizerTest(final String input, final String fromColor, final String toColor) {
		RUN++;
		this.dir = new File(PATH_OUT_SVG, String.valueOf(RUN));
        this.input = input;
        this.fromColor = fromColor;
        this.toColor = toColor;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"circle_blue-mdpi.svg", "#ff0000", "#00ffff"},
                {"fill_property-mdpi.svg", "#000000", "#ffffff"},
        });
    }

    @Test
    public void test() throws TransformerException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, InstantiationException, IllegalAccessException, TranscoderException  {
        final File inputFile = new File(PATH_IN, this.input);
    	final QualifiedResource input = QualifiedResource.fromFile(inputFile);
        Assert.assertTrue("Expected " + inputFile + " to exist", input.exists());

        // now you see me...
        assertFileContains(this.input, new FileInputStream(inputFile), fromColor);
        assertFileNotContains(this.input, new FileInputStream(inputFile), toColor);

        ColorizerMap map = ColorizerMap.from(params.getColorizeConfig(), new TestLogger());
        Colorizer color = map.getBestMatch(input);
        Document doc = color.colorize(input);

        // just transform in memory for easy searching
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(bytes));

        bytes.flush();
        final byte[] output = bytes.toByteArray();

        assertFileNotContains(this.input, new ByteArrayInputStream(output), fromColor);
        assertFileContains(this.input, new ByteArrayInputStream(output), toColor);
    }

    static void assertFileContains(final String path, InputStream input, String needle)
            throws IOException {

        final String content = IOUtils.toString(input, "UTF-8");
        Assert.assertTrue("Expected " + path + " to contain " + needle
                        + ";\n contents=\n" + content,
                content.contains(needle));

    }

    static void assertFileNotContains(final String path, InputStream input, String needle)
            throws IOException {

        final String content = IOUtils.toString(input, "UTF-8");
        Assert.assertTrue("Expected " + path + " to NOT contain " + needle
                        + ";\n contents=\n" + content,
                !content.contains(needle));

    }

}
