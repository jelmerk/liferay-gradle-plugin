package nl.orange11.liferay;

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
        createLiferayConvention(project);
        configureDeploy(project);
    }

    private void createLiferayConvention(Project project) {
        project.getConvention().getPlugins().put("liferay", new LiferayPluginConvention(project));
    }

    private void configureDeploy(Project project) {
        final War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        final Deploy deploy = project.getTasks().add(DEPLOY, Deploy.class);
        deploy.setDescription("Deploys the plugin");
        deploy.setGroup(LiferayBasePlugin.LIFERAY_GROUP);


        final LiferayPluginConvention liferayConvention = project.getConvention()
                .getPlugin(LiferayPluginConvention.class);

        deploy.getConventionMapping().map("autoDeployDir", new Callable<File>() {
            public File call() throws Exception {
                return liferayConvention.getAutoDeployDir();
            }
        });
        deploy.getConventionMapping().map("warFile", new Callable<File>() {
            public File call() throws Exception {
                return warTask.getArchivePath();
            }
        });

        deploy.dependsOn(warTask);
    }

}
