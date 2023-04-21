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
package fr.avianey.androidsvgdrawable.suite;

import fr.avianey.androidsvgdrawable.BoundsExtractionTest;
import fr.avianey.androidsvgdrawable.NinePatchGenerationTest;
import fr.avianey.androidsvgdrawable.OutputFormat;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Launch image generation tests with the JPG {@link OutputFormat}
 * @author antoine vianey
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BoundsExtractionTest.class,
	NinePatchGenerationTest.class
})
public class GenDrawableTestJPGSuite extends GenDrawableTestSuite {

	@BeforeClass
	public static void setup() {
		System.setProperty("outputFormat", OutputFormat.JPG.name());
	}

}
