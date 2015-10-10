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

import com.google.common.collect.ImmutableList;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static fr.avianey.androidsvgdrawable.Density.Value.hdpi;
import static fr.avianey.androidsvgdrawable.OutputFormat.PNG;
import static fr.avianey.androidsvgdrawable.OutputType.drawable;

/**
 * Complete scenario with generation of several drawables
 */
public class EndToEndTest {

    private static final String PATH_IN  = "./target/test-classes/" + EndToEndTest.class.getSimpleName() + "/";
    private static final String PATH_OUT_SVG = "./target/generated/" + EndToEndTest.class.getSimpleName() + "/svg/";
    private static final String PATH_OUT_PNG = "./target/generated-png/" + EndToEndTest.class.getSimpleName() + "/";

    private static SvgDrawablePlugin plugin;

	@Before
    public void setup() {
        TestParameters parameters = new TestParameters();
        parameters.targetedDensities = new Density.Value[] {hdpi};
        parameters.from = ImmutableList.of(
                // individual files
                new File(PATH_IN, "ecmascript/scripted_1-mdpi.svg"),
                new File(PATH_IN, "valid/square/square_red-mdpi.svg"),
                new File(PATH_IN, "valid/square/square_yellow-w16mdpi.svg"),
                new File(PATH_IN, "errors/missconfigured.svg"),
                // directory tree (recursive)
                new File(PATH_IN, "valid/square/color")
        );
        parameters.to = new File(PATH_OUT_PNG);
        parameters.svgMaskedSvgOutputDirectory = new File(PATH_OUT_SVG);
        parameters.svgMaskFiles = ImmutableList.of(
                new File(PATH_IN, "mask/square/squaremask-mdpi.svgmask"),
                new File(PATH_IN, "mask/standard")
        );
        parameters.svgMaskResourceFiles = ImmutableList.of(
                new File(PATH_IN, "masked")
        );
        parameters.outputFormat = PNG;
        parameters.outputType = drawable;
        // get a plugin instance
        plugin = new SvgDrawablePlugin(parameters, new TestLogger(System.out));
    }

    @Test
    public void fromJson() {
    	plugin.execute();
    }

}
