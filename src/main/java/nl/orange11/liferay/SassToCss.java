package nl.orange11.liferay;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class SassToCss extends ConventionTask {

    @InputFiles
    private FileCollection classpath;

    private File appServerPortalDir;

    private File sassDir;

    @TaskAction
    public void sassToCss() {

        if (getSassDir() == null || !getSassDir().exists()) {
            throw new InvalidUserDataException("Please specify a valid sassDir");
        }

        if (getClasspath() == null) {
            throw new InvalidUserDataException("Please specify the classpath");
        }

        if (getAppServerPortalDir() == null) {
            throw new InvalidUserDataException("Please specify the appServerPortalDir");
        }

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


    public File getSassDir() {
        return sassDir;
    }

    public void setSassDir(File sassDir) {
        this.sassDir = sassDir;
    }

    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    public File getAppServerPortalDir() {
        return appServerPortalDir;
    }

    public void setAppServerPortalDir(File appServerPortalDir) {
        this.appServerPortalDir = appServerPortalDir;
    }
}
