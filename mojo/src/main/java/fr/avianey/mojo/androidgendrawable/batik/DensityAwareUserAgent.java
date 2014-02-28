package fr.avianey.mojo.androidgendrawable.batik;

import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.parser.UnitProcessor;

import fr.avianey.mojo.androidgendrawable.util.Constants;

/**
 * A {@link UserAgent} for {@link UnitProcessor} conversion
 * 
 * @author avianey
 */
public class DensityAwareUserAgent extends UserAgentAdapter {
	private float dpi;
	
	public DensityAwareUserAgent(float dpi) {
		this.dpi = dpi;
	}
	
	public float getPixelUnitToMillimeter() {
		return Constants.MM_PER_INCH / dpi;
	}
	
	public float getPixelToMM() {
		return getPixelUnitToMillimeter();
	}
	
}
