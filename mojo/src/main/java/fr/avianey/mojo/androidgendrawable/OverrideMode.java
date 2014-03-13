package fr.avianey.mojo.androidgendrawable;

import java.io.File;


public enum OverrideMode {
    
    always, never, ifModified;
    
    public boolean override(QualifiedResource src, File dest, OutputFormat outputFormat, File ninePatchConfig, boolean isNinePatch) {
        if (!dest.exists() || always.equals(this)) {
            return true;
        } else if (never.equals(this)) {
            return false;
        } else {
            return src.lastModified() > dest.lastModified() 
                    || (outputFormat.hasNinePatchSupport() && isNinePatch && ninePatchConfig.lastModified() > dest.lastModified());
        }
    }
    
}
