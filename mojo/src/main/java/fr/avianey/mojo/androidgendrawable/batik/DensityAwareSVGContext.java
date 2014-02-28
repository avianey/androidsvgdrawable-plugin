package fr.avianey.mojo.androidgendrawable.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.dom.svg.SVGContext;

import fr.avianey.mojo.androidgendrawable.util.Constants;

public class DensityAwareSVGContext implements SVGContext {
	
	private SVGContext wrappedContext;
	private float dpi;

	public DensityAwareSVGContext(SVGContext wrappedContext, float dpi) {
		this.wrappedContext = wrappedContext;
		this.dpi = dpi;
	}

	@Override
	public Rectangle2D getBBox() {
		return wrappedContext.getBBox();
	}

	@Override
	public AffineTransform getCTM() {
		return wrappedContext.getCTM();
	}

	@Override
	public float getFontSize() {
		return wrappedContext.getFontSize();
	}

	@Override
	public AffineTransform getGlobalTransform() {
		return wrappedContext.getGlobalTransform();
	}

	@Override
	public float getPixelToMM() {
		return getPixelUnitToMillimeter();
	}

	@Override
	public float getPixelUnitToMillimeter() {
		return Constants.MM_PER_INCH / dpi;
	}

	@Override
	public AffineTransform getScreenTransform() {
		return wrappedContext.getScreenTransform();
	}

	@Override
	public float getViewportHeight() {
		return wrappedContext.getViewportHeight();
	}

	@Override
	public float getViewportWidth() {
		return wrappedContext.getViewportWidth();
	}

	@Override
	public void setScreenTransform(AffineTransform transform) {
		wrappedContext.setScreenTransform(transform);
	}

}
