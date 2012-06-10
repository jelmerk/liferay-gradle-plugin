package nl.orange11.liferay;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginConvention {

    private final Project project;

    private String serviceInputFileName;

    public ServiceBuilderPluginConvention(Project project) {
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

}
