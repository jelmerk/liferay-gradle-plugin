package nl.orange11.liferay;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.TaskCollection;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    public static final String SASS_TO_CSS ="sas-to-css";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        configureSassToCss(project);
    }

    private void configureSassToCss(Project project) {
        final SassToCss task = project.getTasks().add(SASS_TO_CSS, SassToCss.class);

        final LiferayPluginExtension liferayPluginExtension = project.getExtensions()
                .findByType(LiferayPluginExtension.class);

        final WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        task.getConventionMapping().map("sassDir", new Callable<File>() {
            public File call() throws Exception {
                return warConvention.getWebAppDir();
            }
        });

        task.getConventionMapping().map("portalClasspath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() throws Exception {
                return liferayPluginExtension.getPortalClasspath();
            }
        });

        task.getConventionMapping().map("appServerPortalDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return liferayPluginExtension.getAppServerPortalDir();
            }
        });


        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

}
