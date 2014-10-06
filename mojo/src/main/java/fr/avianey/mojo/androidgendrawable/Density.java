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
package fr.avianey.mojo.androidgendrawable;

/**
 * @author antoine vianey
 */
public enum Density {
    
    ldpi(120), mdpi(160), hdpi(240), xhdpi(320), tvdpi(213), xxhdpi(480), xxxhdpi(640);
    
    private final int dpi;

    private Density(int dpi) {
        this.dpi = dpi;
    }

    public double ratio(Density target) {
        return (double) target.dpi / (double) this.dpi;
    }

	public int getDpi() {
		return dpi;
	}
}
