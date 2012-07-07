package nl.orange11.liferay;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class LiferayBasePlugin implements Plugin<Project> {

    public static final String LIFERAY_GROUP = "liferay";

    public static final String DEPLOY = "deploy";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(WarPlugin.class);
        createLiferayExtension(project);

        configureDeployTaskRule(project);
        configureDeployTask(project);
    }

    protected void createLiferayExtension(Project project) {
        project.getExtensions().create("liferay", LiferayPluginExtension.class, project);
    }

    private void configureDeployTaskRule(final Project project) {
        project.getTasks().withType(Deploy.class, new Action<Deploy>() {
            @Override
            public void execute(Deploy task) {
                configureDeployTaskDefaults(project, task);
            }
        });
    }

    protected void configureDeployTaskDefaults(Project project, Deploy task) {
        final LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);

        task.getConventionMapping().map("autoDeployDir", new Callable<File>() {
            public File call() throws Exception {
                return liferayExtension.getAutoDeployDir();
            }
        });
    }

    private void configureDeployTask(Project project) {
        final War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        final Deploy deploy = project.getTasks().add(DEPLOY, Deploy.class);
        deploy.setDescription("Deploys the plugin");
        deploy.setGroup(LiferayBasePlugin.LIFERAY_GROUP);
        deploy.setWarFile(warTask.getArchivePath());

        deploy.dependsOn(warTask);
    }

}
