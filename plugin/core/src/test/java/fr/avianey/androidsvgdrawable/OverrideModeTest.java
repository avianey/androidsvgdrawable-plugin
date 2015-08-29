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

import static fr.avianey.androidsvgdrawable.OverrideMode.always;
import static fr.avianey.androidsvgdrawable.OverrideMode.ifModified;
import static fr.avianey.androidsvgdrawable.OverrideMode.never;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class OverrideModeTest {

    private static final long FILE_DOES_NOT_EXIST = 0;

    private final OverrideMode overrideMode;
    private final File dest;
    private final File src;
    private final File intermediate;
    private final boolean shouldOverride;

    public OverrideModeTest(OverrideMode overrideMode, long srcLastModified, long destLastModified, long intermediateLastModified, boolean shouldOverride) {
        this.overrideMode = overrideMode;
        this.src = Mockito.mock(File.class);
        this.dest = Mockito.mock(File.class);
        this.intermediate = Mockito.mock(File.class);
        this.shouldOverride = shouldOverride;
        Mockito.when(src.lastModified()).thenReturn(srcLastModified);
        Mockito.when(dest.lastModified()).thenReturn(destLastModified);
        Mockito.when(intermediate.lastModified()).thenReturn(intermediateLastModified);
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {ifModified, 1, FILE_DOES_NOT_EXIST, FILE_DOES_NOT_EXIST, true},
                {ifModified, 1, 2, FILE_DOES_NOT_EXIST, false},
                {ifModified, 1, 1, FILE_DOES_NOT_EXIST, false},
                {ifModified, 2, 1, FILE_DOES_NOT_EXIST, true},
                {ifModified, 1, FILE_DOES_NOT_EXIST, 1, true},
                {ifModified, 1, 2, 1, false},
                {ifModified, 1, 2, 2, false},
                {ifModified, 1, 2, 3, true},
                {ifModified, 2, 2, 1, false},
                {ifModified, 2, 2, 2, false},
                {ifModified, 2, 2, 3, true},
                {ifModified, 3, 2, 1, true},
                {ifModified, 3, 1, 2, true},
                {ifModified, 3, 1, 3, true},
                {always, 1, FILE_DOES_NOT_EXIST, FILE_DOES_NOT_EXIST, true},
                {always, 1, 2, FILE_DOES_NOT_EXIST, true},
                {always, 1, 1, FILE_DOES_NOT_EXIST, true},
                {always, 2, 1, FILE_DOES_NOT_EXIST, true},
                {always, 1, FILE_DOES_NOT_EXIST, 1, true},
                {always, 1, 2, 1, true},
                {always, 1, 2, 2, true},
                {always, 1, 2, 3, true},
                {always, 2, 2, 1, true},
                {always, 2, 2, 2, true},
                {always, 2, 2, 3, true},
                {always, 3, 2, 1, true},
                {always, 3, 1, 2, true},
                {always, 3, 1, 3, true},
                {never, 1, FILE_DOES_NOT_EXIST, FILE_DOES_NOT_EXIST, true},
                {never, 1, 2, FILE_DOES_NOT_EXIST, false},
                {never, 1, 1, FILE_DOES_NOT_EXIST, false},
                {never, 2, 1, FILE_DOES_NOT_EXIST, false},
                {never, 1, FILE_DOES_NOT_EXIST, 1, true},
                {never, 1, 2, 1, false},
                {never, 1, 2, 2, false},
                {never, 1, 2, 3, false},
                {never, 2, 2, 1, false},
                {never, 2, 2, 2, false},
                {never, 2, 2, 3, false},
                {never, 3, 2, 1, false},
                {never, 3, 1, 2, false},
                {never, 3, 1, 3, false}
        });
    }

    @Test
    public void shouldOverride() {
        assertEquals(shouldOverride, overrideMode.shouldOverride(src, dest, intermediate));
    }

}
