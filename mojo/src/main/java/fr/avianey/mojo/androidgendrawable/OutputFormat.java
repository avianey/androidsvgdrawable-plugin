package fr.avianey.mojo.androidgendrawable;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

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
    
}
