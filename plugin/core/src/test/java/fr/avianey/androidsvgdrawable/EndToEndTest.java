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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.FluentIterable.from;
import static fr.avianey.androidsvgdrawable.Density.Value.hdpi;
import static fr.avianey.androidsvgdrawable.OutputFormat.PNG;
import static fr.avianey.androidsvgdrawable.OutputType.drawable;

/**
 * Complete scenario with generation of several drawables
 */
@RunWith(Parameterized.class)
public class EndToEndTest {

    private static final String PATH_IN  = "./target/test-classes/" + EndToEndTest.class.getSimpleName() + "/";
    private static final String PATH_OUT_SVG = "./target/generated/" + EndToEndTest.class.getSimpleName() + "/svg/run";
    private static final String PATH_OUT_PNG = "./target/generated-png/" + EndToEndTest.class.getSimpleName() + "/run";

    private static AtomicInteger RUN = new AtomicInteger();

    private SvgDrawablePlugin plugin;

    private static final Function<? super String, File> toInFile = new Function<String, File>() {
        @Override
        public File apply(String input) {
            return new File(PATH_IN, input);
        }
    };

    public EndToEndTest(Iterable<String> from, Iterable<String> svgMaskFiles, Iterable<String> svgMaskResourceFiles) {
        String run = String.valueOf(RUN.incrementAndGet());
        TestParameters parameters = new TestParameters();
        parameters.targetedDensities = new Density.Value[] {hdpi};
        parameters.from = from(from).transform(toInFile);
        parameters.to = new File(PATH_OUT_PNG + "-" + run);
        parameters.svgMaskFiles = from(svgMaskFiles).transform(toInFile);
        parameters.svgMaskResourceFiles = from(svgMaskResourceFiles).transform(toInFile);
        parameters.svgMaskedSvgOutputDirectory = new File(PATH_OUT_SVG + "-" + run);
        parameters.outputFormat = PNG;
        parameters.outputType = drawable;
        // get a plugin instance well parametrized
        plugin = new SvgDrawablePlugin(parameters, new TestLogger());
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                        // everything in its right place
                        {
                                // from
                                ImmutableList.of(
                                        // individual files
                                        "ecmascript/scripted_1-mdpi.svg",
                                        "valid/square/square_red-mdpi.svg",
                                        "valid/square/square_yellow-w16mdpi.svg",
                                        "errors/missconfigured.svg",
                                        // directory tree (recursive)
                                        "valid/square/color"
                                ),
                                // svgmask
                                ImmutableList.of(
                                        "mask/square/squaremask-mdpi.svgmask",
                                        "mask/standard"
                                ),
                                // masked
                                ImmutableList.of(
                                        "masked"
                                )
                        },
                        // everything in the same place
                        {
                                // from
                                ImmutableList.of(
                                        // individual files
                                        "ecmascript/scripted_1-mdpi.svg",
                                        "valid/square/square_red-mdpi.svg",
                                        "valid/square/square_yellow-w16mdpi.svg",
                                        "errors/missconfigured.svg",
                                        // directory tree (recursive)
                                        "valid/square/color",
                                        // svgmask
                                        "mask/square/squaremask-mdpi.svgmask",
                                        "mask/standard",
                                        // masked
                                        "masked"
                                ),
                                // svgmask
                                ImmutableList.of(),
                                // masked
                                ImmutableList.of()
                        },
                        // nothing in from, only svgmask
                        {
                                // from
                                ImmutableList.of(),
                                // svgmask
                                ImmutableList.of(
                                        "mask/square/squaremask-mdpi.svgmask",
                                        "mask/standard"
                                ),
                                // masked
                                ImmutableList.of(
                                        "masked"
                                )
                        },

                }
        );
    }

    @Test
    public void fromJson() {
    	plugin.execute();
    }

}
