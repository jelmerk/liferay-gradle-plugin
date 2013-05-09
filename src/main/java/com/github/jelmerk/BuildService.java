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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that generates a liferay service
 * (java source files and associated configuration) from a xml service definition file.
 *
 * @author Jelmer Kuperus
 */
public class BuildService extends DefaultTask {

    private FileCollection classpath;

    private String pluginName;

    private File implSrcDir;
    private File apiSrcDir;
    private File resourceDir;
    private File webappSrcDir;

    private File jalopyInputFile;
    private File serviceInputFile;

    /**
     * Performs the build service task.
     */
    @TaskAction
    public void buildService() {
        File workingDir = prepareWorkingDir();
        createOutputDirectories();
        String processOutput = buildService(workingDir);
        echoOutput(processOutput);

        if (didNotExecuteSuccessfully(processOutput)) {
            throw new TaskExecutionException(this, null);
        }
    }

    private boolean didNotExecuteSuccessfully(String processOutput) {
        return processOutput != null && processOutput.contains("Error");
    }

    private void createOutputDirectories() {
        Mkdir mkServicebuilderMainSourceSetDir = new Mkdir();
        mkServicebuilderMainSourceSetDir.setDir(getImplSrcDir());
        mkServicebuilderMainSourceSetDir.execute();

        Mkdir mkSqlDir = new Mkdir();
        mkSqlDir.setDir(new File(getWebappSrcDir(), "/WEB-INF/sql"));
        mkSqlDir.execute();
    }

    private File prepareWorkingDir() {

        // the Jalopy file to use is not a parameter you can pass to service builder it just looks at a number
        // of predefined locations on the filesystem. So we set up a working dir where we mimic the layout
        // servicebuilder expects as a workaround

        File workingDir = getProject().mkdir(new File(getProject().getBuildDir(), "servicebuilder"));
        File miscDir = getProject().mkdir(new File(workingDir, "misc"));

        File jalopyFile = new File(miscDir, "jalopy.xml");

        if (getJalopyInputFile() != null) {
            Copy copy = new Copy();
            copy.setProject(getAnt().getProject());
            copy.setFile(getJalopyInputFile());
            copy.setTofile(jalopyFile);
            copy.setOverwrite(true);
            copy.execute();
        }

        return workingDir;
    }

    private String buildService(File workingDir) {
        Java javaTask = new Java();
        javaTask.setTaskName("service builder");
        javaTask.setClassname("com.liferay.portal.tools.servicebuilder.ServiceBuilder");

        javaTask.setFork(true); // must fork or the working dir we set below is not picked up
        javaTask.setDir(workingDir);
        javaTask.setOutputproperty("service.test.output");

        Project antProject = getAnt().getAntProject();

        Path antClassPath = new Path(antProject);

        for (File dep : getClasspath()) {
            antClassPath.createPathElement()
                    .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(antClassPath);

        javaTask.createArg()
                .setLine("-Dexternal-properties=com/liferay/portal/tools/dependencies/portal-tools.properties");

        javaTask.createArg()
                .setLine("-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger");

        javaTask.createArg()
                .setLine("service.input.file=" + getServiceInputFile().getPath());

        javaTask.createArg()
                .setLine("service.hbm.file="
                        + new File(getResourceDir(), "META-INF/portlet-hbm.xml").getPath());

        javaTask.createArg()
                .setLine("service.orm.file="
                        + new File(getResourceDir(), "META-INF/portlet-orm.xml").getPath());

        javaTask.createArg()
                .setLine("service.model.hints.file="
                        + new File(getResourceDir(), "META-INF/portlet-model-hints.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.file="
                        + new File(getResourceDir(), "META-INF/portlet-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.base.file="
                        + new File(getResourceDir(), "META-INF/base-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.cluster.file="
                        + new File(getResourceDir(), "META-INF/cluster-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.dynamic.data.source.file="
                        + new File(getResourceDir(), "META-INF/dynamic-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.hibernate.file="
                        + new File(getResourceDir(), "META-INF/hibernate-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.infrastructure.file="
                        + new File(getResourceDir(), "META-INF/infrastructure-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.shard.data.source.file="
                        + new File(getResourceDir(), "META-INF/shard-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.api.dir=" + getApiSrcDir().getPath());

        javaTask.createArg()
                .setLine("service.impl.dir=" + getImplSrcDir().getPath());

        javaTask.createArg()
                .setLine("service.json.file=" + new File(getWebappSrcDir(), "js/service.js").getPath());

        javaTask.createArg()
                .setLine("service.sql.dir=" + new File(getWebappSrcDir(), "WEB-INF/sql").getPath());

        javaTask.createArg()
                .setLine("service.sql.file=tables.sql");

        javaTask.createArg()
                .setLine("service.sql.indexes.file=indexes.sql");

        javaTask.createArg()
                .setLine("service.sql.indexes.properties.file=indexes.properties");

        javaTask.createArg()
                .setLine("service.sql.sequences.file=sequences.sql");

        javaTask.createArg()
                .setLine("service.auto.namespace.tables=true");

        javaTask.createArg()
                .setLine("service.bean.locator.util=com.liferay.util.bean.PortletBeanLocatorUtil");

        javaTask.createArg()
                .setLine("service.props.util=com.liferay.util.service.ServiceProps");

        javaTask.createArg()
                .setLine("service.plugin.name=" + getPluginName());

        //javaTask.createJvmarg().setLine("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006");

        javaTask.execute();

        return antProject.getProperty("service.test.output");
    }

    private void echoOutput(String processOutput) {
        Echo echo = new Echo();
        //echo.setProject(getAnt().getAntProject());
        echo.setMessage(processOutput);
        echo.execute();
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    @Input
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Sets the plugin name.
     *
     * @param pluginName the plugin name
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Returns a file that points to the service input file. This declares the entity for which a service
     * is generated.
     *
     * @return the file that points to the service input file
     */
    @InputFile
    public File getServiceInputFile() {
        return serviceInputFile;
    }

    /**
     * Sets the file that points to the service input file. This file declares the entity for which a service
     * is generated. This property is required.
     *
     * @param serviceInputFile the path to the service input file
     */
    public void setServiceInputFile(File serviceInputFile) {
        this.serviceInputFile = serviceInputFile;
    }

    /**
     * Returns the file that points to the jalopy file. This file configures the formatter that formats the
     * generated code.
     *
     * @return the file that points to the jalopy file
     */
    @Optional
    @InputFile
    public File getJalopyInputFile() {
        return jalopyInputFile;
    }

    /**
     * Sets the file that points to the jalopy file. This file configures the formatter that formats the generated code.
     *
     * @param jalopyInputFile file that points to the jalopy file
     */
    public void setJalopyInputFile(File jalopyInputFile) {
        this.jalopyInputFile = jalopyInputFile;
    }


    /**
     * Returns the file that points to the folder where the generated service implementation source files will be
     * written to.
     *
     * @return the file that points to the folder where the generated service implementation source files will be
     *         written to
     */
    @OutputDirectory
    public File getImplSrcDir() {
        return implSrcDir;
    }

    /**
     * Sets the file that points to the folder where the generated service implementation source files will be
     * written to. This property is required.
     *
     * @param implSrcDir file that points to the folder where the generated service implementation source files
     *                   will be written to.
     */
    public void setImplSrcDir(File implSrcDir) {
        this.implSrcDir = implSrcDir;
    }

    /**
     * Returns the file that points to the folder where the generated service api source files will be written to.
     *
     * @return the file that points to the folder where the generated service api source files will be written to
     */
    @OutputDirectory
    public File getApiSrcDir() {
        return apiSrcDir;
    }

    /**
     * Sets the file that points to the folder where the generated service api source files will be written to.
     * This property is required.
     *
     * @param apiSrcDir file that points to the folder where the generated service api source files will be
     *                  written to.
     */
    public void setApiSrcDir(File apiSrcDir) {
        this.apiSrcDir = apiSrcDir;
    }

    /**
     * Returns the file that points to the folder where the generated resource files
     * (hibernate mappings / application contexts etc) will be written to.
     *
     * @return the file that points to the folder where the generated resource files will be written to
     */
    @OutputDirectory
    public File getResourceDir() {
        return resourceDir;
    }

    /**
     * Sets the file that points to the folder where the generated resource files
     * (hibernate mappings / application contexts etc) will be written to. This property is required.
     *
     * @param resourceDir file that points to the folder where the generated resource files will be written to
     */
    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    /**
     * Returns the web application folder where generated sql files etc will be written to.
     *
     * @return the web application folder where generated sql files etc will be written to
     */
    @OutputDirectory
    public File getWebappSrcDir() {
        return webappSrcDir;
    }

    /**
     * Sets the web application folder where generated sql files etc will be written to.
     *
     * @param webappSrcDir the web application folder where generated sql files etc will be written to
     */
    public void setWebappDir(File webappSrcDir) {
        this.webappSrcDir = webappSrcDir;
    }

    /**
     * Returns the file collection that contains the classes required to run liferay's ServiceBuilder.
     *
     * @return the file collection that contains the classes required to run liferay's ServiceBuilder
     */
    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the file collection that contains the classes required to run liferay's ServiceBuilder.
     * This property is required.
     *
     * @param classpath the file collection that contains the classes required to run liferay's ServiceBuilder
     */
    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }
}
