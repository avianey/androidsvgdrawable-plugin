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
package fr.avianey.androidsvgdrawable.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.dom.svg.SVGContext;

import fr.avianey.androidsvgdrawable.util.Constants;

/**
 * @author antoine vianey
 */
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
