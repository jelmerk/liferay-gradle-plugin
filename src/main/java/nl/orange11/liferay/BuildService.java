package nl.orange11.liferay;

import org.apache.tools.ant.taskdefs.Mkdir;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class BuildService extends AbstractTask {

    @TaskAction
    public void buildService() {

        WarPluginConvention warConvention = getProject().getConvention().getPlugin(WarPluginConvention.class);
        JavaPluginConvention javaConvention = getProject().getConvention().getPlugin(JavaPluginConvention.class);

        SourceSetContainer sourceSets = javaConvention.getSourceSets();
        SourceSet sourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        SourceDirectorySet allJava = sourceSet.getAllJava();
        SourceDirectorySet resources = sourceSet.getResources();

        File webappDir = warConvention.getWebAppDir();
        File javaSrcDir = allJava.getSingleFile();
        File resourceDir = resources.getSingleFile();

        Mkdir mkdir = new Mkdir();
        mkdir.setDir(new File(resourceDir, "META-INF"));
        mkdir.execute();

        System.out.println("Build service here.");
    }

}
