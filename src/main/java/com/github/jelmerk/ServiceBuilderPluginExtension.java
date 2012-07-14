package com.github.jelmerk;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
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
        if (serviceInputFileName != null) {
            return serviceInputFileName;
        }
        // TODO : change this to resources or the project root? I know this is what liferay uses but you really don't want to ship your source files
        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        return new File(warConvention.getWebAppDir(), "WEB-INF/service.xml").getPath();
    }

    public void setServiceInputFileName(String serviceInputFileName) {
        this.serviceInputFileName = serviceInputFileName;
    }

    public String getImplSrcDirName() {
        if (implSrcDirName != null) {
            return implSrcDirName;
        }
        return getSourceSetByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .getAllJava().getSrcDirs().iterator().next().toString();
    }

    public void setImplSrcDirName(String implSrcDirName) {
        this.implSrcDirName = implSrcDirName;
    }

    public String getApiSrcDirName() {
        if (apiSrcDirName != null) {
            return apiSrcDirName;
        }
        return getSourceSetByName(ServiceBuilderPlugin.SERVICE_SOURCE_SET_NAME)
                .getAllJava().getSrcDirs().iterator().next().getPath();
    }

    public void setApiSrcDirName(String apiSrcDirName) {
        this.apiSrcDirName = apiSrcDirName;
    }

    public String getResourceDirName() {
        if (resourceDirName != null) {
            return resourceDirName;
        }
        return getSourceSetByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .getResources().getSrcDirs().iterator().next().getPath();
    }

    public void setResourceDirName(String resourceDirName) {
        this.resourceDirName = resourceDirName;
    }

    public File getServiceInputFile() {
        return project.file(getServiceInputFileName());
    }

    public File getJalopyInputFile() {
        if (getJalopyInputFileName() == null) {
            return null;
        }
        return project.file(getJalopyInputFileName());
    }

    public File getImplSrcDir() {
        return project.file(getImplSrcDirName());
    }

    public File getApiSrcDir() {
        return project.file(getApiSrcDirName());
    }

    public File getResourceDir() {
        return project.file(getResourceDirName());
    }

    public void servicebuilder(Closure closure) {
        ConfigureUtil.configure(closure, this);
    }

    private SourceSet getSourceSetByName(String name) {
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        SourceSetContainer sourceSets = javaConvention.getSourceSets();
        return sourceSets.getByName(name);
    }
}
