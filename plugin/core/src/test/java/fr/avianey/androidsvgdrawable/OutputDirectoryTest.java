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
import com.google.common.collect.ImmutableSet;
import fr.avianey.androidsvgdrawable.util.TestLogger;
import fr.avianey.androidsvgdrawable.util.TestParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.FluentIterable.from;
import static fr.avianey.androidsvgdrawable.Density.Value.*;
import static fr.avianey.androidsvgdrawable.OutputFormat.PNG;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertTrue;

/**
 * Complete scenario with generation of several drawables
 */
@RunWith(Parameterized.class)
public class OutputDirectoryTest {

    private static final String PATH_IN  = "./target/test-classes/" + OutputDirectoryTest.class.getSimpleName() + "/";
    private static final String PATH_OUT = "./target/generated/" + OutputDirectoryTest.class.getSimpleName() + "/";

    private static AtomicInteger RUN = new AtomicInteger();

    private final TestParameters parameters;
    private final SvgDrawablePlugin plugin;
    private final Set<File> expectedDirectories;

    public OutputDirectoryTest(Density.Value[] targetedDensities, OutputType outputType, Set<String> expectedDirectories) {
        String run = String.valueOf(RUN.incrementAndGet());
        parameters = new TestParameters();
        parameters.targetedDensities = targetedDensities;
        parameters.outputFormat = PNG;
        parameters.outputType = outputType;
        parameters.from = singleton(new File(PATH_IN));
        parameters.to = new File(PATH_OUT, "run-" + run);
        // get a plugin instance well parametrized
        plugin = new SvgDrawablePlugin(parameters, new TestLogger());
        // expected directories
        this.expectedDirectories = ImmutableSet.copyOf(from(expectedDirectories).transform(
                new Function<String, File>() {
                    @Nullable
                    @Override
                    public File apply(String path) {
                        return new File(parameters.to, path);
                    }
                }
        ).toList());
    }

    @Before
    public void cleanup() {
        for (File directory : expectedDirectories) {
            if (directory.exists()) {
                directory.delete();
            }
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(
                new Object[][]{
                        {
                                new Density.Value[]{hdpi},
                                OutputType.drawable,
                                ImmutableSet.of("drawable-hdpi")
                        },
                        {
                                new Density.Value[]{},
                                OutputType.drawable,
                                ImmutableSet.of(
                                        "drawable-ldpi",
                                        "drawable-mdpi",
                                        "drawable-hdpi",
                                        "drawable-xhdpi",
                                        "drawable-xxhdpi",
                                        "drawable-xxxhdpi",
                                        "drawable-tvdpi"
                                )
                        },
                        {
                                new Density.Value[]{xxxhdpi},
                                OutputType.mipmap,
                                ImmutableSet.of("mipmap-xxxhdpi")
                        },
                        {
                                new Density.Value[]{},
                                OutputType.mipmap,
                                ImmutableSet.of(
                                        "mipmap-ldpi",
                                        "mipmap-mdpi",
                                        "mipmap-hdpi",
                                        "mipmap-xhdpi",
                                        "mipmap-xxhdpi",
                                        "mipmap-xxxhdpi",
                                        "mipmap-tvdpi"
                                )
                        },
                        {
                                new Density.Value[]{},
                                OutputType.raw,
                                ImmutableSet.of()
                        },
                        {
                                null,
                                OutputType.raw,
                                ImmutableSet.of()
                        },
                        {
                                new Density.Value[]{ldpi, hdpi, xxxhdpi},
                                OutputType.raw,
                                ImmutableSet.of()
                        },
                }
        );
    }

    @Test
    public void fromJson() {
    	plugin.execute();
        // created
        Set<File> createdDirectories = new HashSet<>();
        createdDirectories.addAll(asList(parameters.to.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                }
        )));
        // expected
        HashSet<File> missing = new HashSet<>(expectedDirectories);
        missing.removeAll(createdDirectories);
        HashSet<File> unexpected = new HashSet<>(createdDirectories);
        unexpected.removeAll(expectedDirectories);
        assertTrue("Missing expected directory: " + Arrays.toString(missing.toArray()), missing.isEmpty());
        assertTrue("Unexpected directory found: " + Arrays.toString(unexpected.toArray()), unexpected.isEmpty());
    }

}
