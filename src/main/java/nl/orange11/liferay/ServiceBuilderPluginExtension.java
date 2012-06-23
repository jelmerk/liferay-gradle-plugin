package nl.orange11.liferay;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginExtension {

    private final Project project;

    private String implSrcDirName;
    private String apiSrcDirName;
    private String resourceDirName;

    private String jalopyInputFileName;
    private String serviceInputFileName;

    public ServiceBuilderPluginExtension(Project project) {
        this.project = project;
    }

    public String getJalopyInputFileName() {
        return jalopyInputFileName;
    }

    public void setJalopyInputFileName(String jalopyInputFileName) {
        this.jalopyInputFileName = jalopyInputFileName;
    }

    public String getServiceInputFileName() {
        return serviceInputFileName;
    }

    public void setServiceInputFileName(String serviceInputFileName) {
        this.serviceInputFileName = serviceInputFileName;
    }

    public String getImplSrcDirName() {
        return implSrcDirName;
    }

    public void setImplSrcDirName(String implSrcDirName) {
        this.implSrcDirName = implSrcDirName;
    }

    public String getApiSrcDirName() {
        return apiSrcDirName;
    }

    public void setApiSrcDirName(String apiSrcDirName) {
        this.apiSrcDirName = apiSrcDirName;
    }

    public String getResourceDirName() {
        return resourceDirName;
    }

    public void setResourceDirName(String resourceDirName) {
        this.resourceDirName = resourceDirName;
    }

    public File getServiceInputFile() {
        return project.file(getServiceInputFileName());
    }

    public File getJalopyInputFile() {
        return project.file(getJalopyInputFileName());
    }

    public void servicebuilder(Closure closure) {
        ConfigureUtil.configure(closure, this);
    }
}
