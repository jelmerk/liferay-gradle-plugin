package nl.orange11.liferay;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    public static final String SASS_TO_CSS ="sassToCss";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createConfiguration(project);

        configureTaskRule(project);
        configureSassToCssTask(project);
    }

    private void createConfiguration(Project project) {

        Configuration configuration = project.getConfigurations().add("sass");

        configuration.setVisible(false);
        configuration.setTransitive(true);
        configuration.setDescription("The sass configuration");
    }

    private void configureTaskRule(final Project project) {
        project.getTasks().withType(SassToCss.class, new Action<SassToCss>() {
            @Override
            public void execute(SassToCss sassToCss) {
                configureTask(project, sassToCss);
            }
        });
    }

    protected void configureTask(final Project project, SassToCss task) {

        final LiferayPluginExtension liferayPluginExtension = project.getExtensions()
                .findByType(LiferayPluginExtension.class);

        final WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        final LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);

        task.getConventionMapping().map("sassDir", new Callable<File>() {
            public File call() throws Exception {
                return warConvention.getWebAppDir();
            }
        });

        task.getConventionMapping().map("classpath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() throws Exception {

                Configuration sassConfiguration = project.getConfigurations().getByName("sass");

                if (sassConfiguration.getDependencies().isEmpty()) {

                    project.getDependencies().add("sass", "javax.servlet:servlet-api:2.5");
                    project.getDependencies().add("sass", "javax.servlet.jsp:jsp-api:2.1");
                    project.getDependencies().add("sass", "javax.activation:activation:1.1");

                    project.getDependencies().add("sass", liferayExtension.getPortalClasspath());
                }
                return sassConfiguration;
            }
        });

        task.getConventionMapping().map("appServerPortalDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return liferayPluginExtension.getAppServerPortalDir();
            }
        });

    }

    private void configureSassToCssTask(final Project project) {
        final SassToCss task = project.getTasks().add(SASS_TO_CSS, SassToCss.class);

        // should this go here or in the configure?
        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

}
