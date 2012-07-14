package com.github.jelmerk;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.*;

import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;


import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class BuildService extends ConventionTask { // TODO: meh conventiontask is an internal api too it seems..

    private FileCollection classpath;

    private String pluginName;

    private File implSrcDir;
    private File apiSrcDir;
    private File resourceDir;
    private File webappSrcDir;

    private File jalopyInputFile;
    private File serviceInputFile;

    @TaskAction
    public void buildService() {

        if (getPluginName() == null) {
            throw new InvalidUserDataException("Please specify a pluginName.");
        }

        if (getServiceInputFile() == null) {
            throw new InvalidUserDataException("Please specify a serviceInputFile.");
        }

        if (getClasspath() == null) {
            throw new InvalidUserDataException("Please specify a classpath.");
        }

        if (!getServiceInputFile().exists()) {
            throw new InvalidUserDataException("ServiceInputFile " + getServiceInputFile() + " does not exist.");
        }

        if (getJalopyInputFile() != null && !getJalopyInputFile().exists()) {
            throw new InvalidUserDataException("JalopyInputFile " + getJalopyInputFile() + " does not exist.");
        }

        // the Jalopy file to use is actually not a parameter you can pass to service builder it just looks at a number
        // of predefined locations on the filesystem. So we set up a working dir where we mimic the layout
        // servicebuilder expects as a workaround

        File workingDir = prepareWorkingDir();

        Mkdir mkServicebuilderMainSourceSetDir = new Mkdir();
        mkServicebuilderMainSourceSetDir.setDir(getImplSrcDir());
        mkServicebuilderMainSourceSetDir.execute();

        Mkdir mkSqlDir = new Mkdir();
        mkSqlDir.setDir(new File(getWebappSrcDir(), "sql"));
        mkSqlDir.execute();

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

        String processOutput = antProject.getProperty("service.test.output");

        System.out.println(processOutput);  // is there some sort of abstraction we should be using ?

        if (processOutput != null && processOutput.contains("Error")) {
            throw new TaskExecutionException(this, null);
        }
    }


    private File prepareWorkingDir() {
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

    @Input
    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    @InputFile
    public File getServiceInputFile() {
        return serviceInputFile;
    }

    public void setServiceInputFile(File serviceInputFile) {
        this.serviceInputFile = serviceInputFile;
    }

    @Optional
    @InputFile
    public File getJalopyInputFile() {
        return jalopyInputFile;
    }

    public void setJalopyInputFile(File jalopyInputFile) {
        this.jalopyInputFile = jalopyInputFile;
    }

    @OutputDirectory
    public File getImplSrcDir() {
        return implSrcDir;
    }

    public void setImplSrcDir(File implSrcDir) {
        this.implSrcDir = implSrcDir;
    }

    @OutputDirectory
    public File getApiSrcDir() {
        return apiSrcDir;
    }

    public void setApiSrcDir(File apiSrcDir) {
        this.apiSrcDir = apiSrcDir;
    }

    @OutputDirectory
    public File getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    @OutputDirectory
    public File getWebappSrcDir() {
        return webappSrcDir;
    }

    public void setWebappSrcDir(File webappSrcDir) {
        this.webappSrcDir = webappSrcDir;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }
}
