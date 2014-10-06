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
package fr.avianey.mojo.androidgendrawable.batik;

import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.parser.UnitProcessor;

import fr.avianey.mojo.androidgendrawable.util.Constants;

/**
 * A {@link UserAgent} for {@link UnitProcessor} conversion
 * 
 * @author antoine vianey
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
