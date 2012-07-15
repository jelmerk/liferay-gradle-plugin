package com.github.jelmerk;

import org.gradle.BuildAdapter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;

/**
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    public static final String SASS_TO_CSS = "sassToCss";

    private static final String SASS_CONFIGURATION_NAME = "sass";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createConfiguration(project);

        configureSassToCssTaskDefaults(project);
        configureSassToCssTask(project);
    }

    private void createConfiguration(Project project) {
        Configuration configuration = project.getConfigurations().add(SASS_CONFIGURATION_NAME);

        configuration.setVisible(false);
        configuration.setDescription("The sass configuration");
    }

    private void configureSassToCssTaskDefaults(final Project project) {
        project.getGradle().addBuildListener(new BuildAdapter() {
            @Override
            public void projectsEvaluated(Gradle gradle) {

                final LiferayPluginExtension liferayPluginExtension = project.getExtensions()
                        .findByType(LiferayPluginExtension.class);

                final Configuration sassConfiguration = project.getConfigurations().getByName("sass");

                if (sassConfiguration.getDependencies().isEmpty()) {

                    project.getDependencies().add(SASS_CONFIGURATION_NAME, "javax.servlet:servlet-api:2.5");
                    project.getDependencies().add(SASS_CONFIGURATION_NAME, "javax.servlet.jsp:jsp-api:2.1");
                    project.getDependencies().add(SASS_CONFIGURATION_NAME, "javax.activation:activation:1.1");

                    project.getDependencies().add(SASS_CONFIGURATION_NAME,
                            liferayPluginExtension.getPortalClasspath());
                }

                project.getTasks().withType(SassToCss.class, new Action<SassToCss>() {
                    @Override
                    public void execute(SassToCss task) {
                        if (task.getClasspath() == null) {
                            task.setClasspath(sassConfiguration);
                        }
                        if (task.getAppServerPortalDir() == null) {
                            task.setAppServerPortalDir(liferayPluginExtension.getAppServerPortalDir());
                        }
                    }
                });
            }
        });
    }

    private void configureSassToCssTask(final Project project) {
        final SassToCss task = project.getTasks().add(SASS_TO_CSS, SassToCss.class);

        project.getGradle().addBuildListener(new BuildAdapter() {
            @Override
            public void projectsEvaluated(Gradle gradle) {
                WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

                if (task.getSassDir() == null) {
                    task.setSassDir(warConvention.getWebAppDir());
                }
            }
        });

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

}
