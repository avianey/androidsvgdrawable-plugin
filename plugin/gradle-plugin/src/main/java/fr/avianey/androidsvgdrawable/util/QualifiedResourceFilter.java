package fr.avianey.androidsvgdrawable.util;

import fr.avianey.androidsvgdrawable.QualifiedResource;
import fr.avianey.androidsvgdrawable.QualifiedSVGResourceFactory;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class QualifiedResourceFilter implements FileFilter, IOFileFilter {

    private final Log log;
    private final String extension;
    private final QualifiedSVGResourceFactory qualifiedSVGResourceFactory;
    private final Collection<QualifiedResource> resources = new ArrayList<>();

    public QualifiedResourceFilter(Log log, QualifiedSVGResourceFactory qualifiedSVGResourceFactory, String extension) {
        this.log = log;
        this.qualifiedSVGResourceFactory = qualifiedSVGResourceFactory;
        this.extension = extension;
    }

    @Override
    public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }

    @Override
    public boolean accept(File file) {
        if (file.isFile() && extension.equalsIgnoreCase(getExtension(file.getAbsolutePath()))) {
            try {
                resources.add(qualifiedSVGResourceFactory.fromSVGFile(file));
            } catch (Exception e) {
                log.warn("Invalid " + extension + " file : " + file.getAbsolutePath(), e);
                return false;
            }
        } else {
            log.debug("+ skipping " + file.getAbsolutePath());
            return false;
        }
        return true;
    }

    public Collection<QualifiedResource> filteredResources() {
        return resources;
    }
}
