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
