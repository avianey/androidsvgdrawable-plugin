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
package fr.avianey.mojo.androidgendrawable.suite;

import java.io.File;

import org.junit.BeforeClass;

import fr.avianey.mojo.androidgendrawable.OutputFormat;

public abstract class GenDrawableTestSuite {
	
	public static String PATH_OUT;
	public static OutputFormat OUTPUT_FORMAT;
	
	@BeforeClass
	public static void setup() {
		String outputFormat = System.getProperty("outputFormat");
		OUTPUT_FORMAT = OutputFormat.valueOf(outputFormat);
		// clean output
		PATH_OUT = "./target/generated-" + OUTPUT_FORMAT.name().toLowerCase() + "/";
		File f = new File(PATH_OUT);
		if (f.exists()) {
			f.delete();
		}
		f.mkdirs();
	}
	
}
