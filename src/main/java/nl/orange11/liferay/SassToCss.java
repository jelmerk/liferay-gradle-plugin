package nl.orange11.liferay;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Set;

/**
 * @author Jelmer Kuperus
 */
public class SassToCss extends ConventionTask {

    private File sassDir;

    @TaskAction
    public void sassToCss() {

        if (getSassDir() == null || !getSassDir().exists()) {
            throw new InvalidUserDataException("Please specify a valid sassDir");
        }

        // should we be doing this lookup here ?, if we set it as conventions then you would have to set a lot of
        // properties unrelated to the task at hand

        final LiferayPluginConvention liferayPluginConvention = getProject().getConvention()
                .getPlugin(LiferayPluginConvention.class);

        Java javaTask = new Java();

        javaTask.setTaskName("sass to css builder");
        javaTask.setClassname("com.liferay.portal.tools.SassToCssBuilder");


        Project antProject = getAnt().getAntProject();

        Path classPath = new Path(antProject);

        for (File dep : liferayPluginConvention.getPortalClasspath()) {
            classPath.createPathElement()
                     .setLocation(dep);
        }

        javaTask.setProject(antProject); // needed because the javatask calls getProject().getDefaultInputStream()
        javaTask.setClasspath(classPath);

        javaTask.setFork(true);
        javaTask.setNewenvironment(true);

        javaTask.createArg()
                .setLine("sass.dir=" + getSassDir());

        javaTask.createJvmarg().setLine("-Dliferay.lib.portal.dir=" +
                new File(liferayPluginConvention.getAppServerPortalDir(), "WEB-INF/lib"));

        javaTask.execute();
    }


    public File getSassDir() {
        return sassDir;
    }

    public void setSassDir(File sassDir) {
        this.sassDir = sassDir;
    }
}
