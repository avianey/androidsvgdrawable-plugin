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

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Format of the generated resources
 *
 * @author antoine vianey
 */
public enum OutputFormat {

    PNG(PNGTranscoder.class),
    JPG(JPEGTranscoder.class);

    private final Class<? extends ImageTranscoder> transcoderClass;

    private OutputFormat(Class<? extends ImageTranscoder> c) {
        this.transcoderClass = c;
    }

    public Class<? extends ImageTranscoder> getTranscoderClass() {
        return transcoderClass;
    }

	public boolean hasNinePatchSupport() {
		return PNG.equals(this);
	}

}
