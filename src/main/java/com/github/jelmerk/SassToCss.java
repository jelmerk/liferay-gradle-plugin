package com.github.jelmerk;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class SassToCss extends DefaultTask {

    private FileCollection classpath;

    private File appServerPortalDir;

    private File sassDir;

    @TaskAction
    public void sassToCss() {

        Java javaTask = new Java();

        javaTask.setTaskName("sass to css builder");
        javaTask.setClassname("com.liferay.portal.tools.SassToCssBuilder");


        Project antProject = getAnt().getAntProject();

        Path antClasspath = new Path(antProject);

        for (File dep : getClasspath()) {
            antClasspath.createPathElement()
                     .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(antClasspath);

        javaTask.setFork(true);
        javaTask.setNewenvironment(true);

        javaTask.createArg()
                .setLine("sass.dir=" + getSassDir());

        javaTask.createJvmarg().setLine("-Dliferay.lib.portal.dir=" + new File(getAppServerPortalDir(), "WEB-INF/lib"));

        javaTask.execute();
    }

    @InputDirectory
    public File getSassDir() {
        return sassDir;
    }

    public void setSassDir(File sassDir) {
        this.sassDir = sassDir;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    @InputDirectory
    public File getAppServerPortalDir() {
        return appServerPortalDir;
    }

    public void setAppServerPortalDir(File appServerPortalDir) {
        this.appServerPortalDir = appServerPortalDir;
    }
}
