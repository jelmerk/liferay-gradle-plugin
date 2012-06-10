package nl.orange11.liferay;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.ConfigureUtil;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginExtension {

    private final Project project;

    private String serviceInputFileName;

    private SourceSetContainer sourceSets;

    public ServiceBuilderPluginExtension(Project project) {
        this.project = project;
    }

    public String getServiceInputFileName() {
        return serviceInputFileName;
    }

    public void setServiceInputFileName(String serviceInputFileName) {
        this.serviceInputFileName = serviceInputFileName;
    }

    public File getServiceInputFile() {

        // so we want this in this configuration or make a special servicebuilder plugin...
        throw new UnsupportedOperationException("Need to figure out how to resolve this");
    }

    public void servicebuilder(Closure closure) {
        ConfigureUtil.configure(closure, this);
    }

    public SourceSetContainer getSourceSets() {
        return sourceSets;
    }

    public void setSourceSets(SourceSetContainer sourceSets) {
        this.sourceSets = sourceSets;
    }
}
