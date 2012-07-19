/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Extension that holds servicebuilder specific configuration.
 *
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginExtension {

    private final Project project;

    private String implSrcDirName;
    private String apiSrcDirName;
    private String resourceDirName;

    private String jalopyInputFileName;
    private String serviceInputFileName;

    /**
     * Constructs a new ServiceBuilderPluginExtension.
     *
     * @param project the project this extension is applied to
     */
    public ServiceBuilderPluginExtension(Project project) {
        this.project = project;
    }

    /**
     * Returns the path to the jalopy file. This file configures the formatter that formats the generated code.
     *
     * @return the path to the jalopy file
     */
    public String getJalopyInputFileName() {
        return jalopyInputFileName;
    }

    /**
     * Sets the path to the jalopy file. This file configures the formatter that formats the generated code.
     *
     * @param jalopyInputFileName the path to the jalopy file
     */
    public void setJalopyInputFileName(String jalopyInputFileName) {
        this.jalopyInputFileName = jalopyInputFileName;
    }

    /**
     * Returns the path to the service input file. This file declares the entity for which a service
     * is generated.
     *
     * @return the path to the service input file
     */
    public String getServiceInputFileName() {
        if (serviceInputFileName != null) {
            return serviceInputFileName;
        }
        // TODO : change this to resources or the project root? I know this is what liferay uses but you really don't want to ship your source files
        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        return new File(warConvention.getWebAppDir(), "WEB-INF/service.xml").getPath();
    }

    /**
     * Sets the path to the service input file. This file declares the entity for which a service
     * is generated. If unset this value defaults to $webappdir/WEB-INF/service.xml
     *
     * @param serviceInputFileName the path to the service input file
     */
    public void setServiceInputFileName(String serviceInputFileName) {
        this.serviceInputFileName = serviceInputFileName;
    }

    /**
     * Returns the path to the directory where the service implementation source files will be written to.
     *
     * @return the path to the directory where the service implementation source files will be written to
     */
    public String getImplSrcDirName() {
        if (implSrcDirName != null) {
            return implSrcDirName;
        }
        return getSourceSetByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .getAllJava().getSrcDirs().iterator().next().toString();
    }

    /**
     * Sets the path to the directory where the service implementation source files will be written to.
     * If unset this value defaults to
     *
     * @param implSrcDirName the path to the directory where the service implementation source files will be written to.
     */
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
