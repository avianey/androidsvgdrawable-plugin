package fr.avianey.mojo.androidgendrawable.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import fr.avianey.mojo.androidgendrawable.BoundsExtractionTest;
import fr.avianey.mojo.androidgendrawable.NinePatchGenerationTest;

/**
 * Launch image generation tests with the PNG {@link OutputFormat}
 * @author avianey
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BoundsExtractionTest.class,
	NinePatchGenerationTest.class
})
public class GenDrawableTestPNGSuite extends GenDrawableTestSuite {}