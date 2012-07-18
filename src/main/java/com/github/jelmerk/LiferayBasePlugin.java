package com.github.jelmerk;

import org.gradle.BuildAdapter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

/**
 * @author Jelmer Kuperus
 */
public class LiferayBasePlugin implements Plugin<Project> {

    public static final String LIFERAY_GROUP = "liferay";

    public static final String DEPLOY = "deploy";

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(WarPlugin.class);
        createLiferayExtension(project);

        configureDeployTaskDefaults(project);
        configureDeployTask(project);
    }

    private void createLiferayExtension(Project project) {
        project.getExtensions().create("liferay", LiferayPluginExtension.class, project);
    }

    private void configureDeployTaskDefaults(final Project project) {
        project.getGradle().addBuildListener(new DeployTaskDefaultsBuildListener(project));
    }

    private void configureDeployTask(Project project) {
        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        Deploy deploy = project.getTasks().add(DEPLOY, Deploy.class);
        deploy.setDescription("Deploys the plugin");
        deploy.setGroup(LiferayBasePlugin.LIFERAY_GROUP);

        project.getGradle().addBuildListener(new DeployTaskBuildListener(deploy, warTask));
        deploy.dependsOn(warTask);
    }

    private static class DeployTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        public DeployTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);
            project.getTasks().withType(Deploy.class, new SetDeployTaskDefaultsAction(liferayExtension));
        }

        private static class SetDeployTaskDefaultsAction implements Action<Deploy> {
            private final LiferayPluginExtension liferayExtension;

            public SetDeployTaskDefaultsAction(LiferayPluginExtension liferayExtension) {
                this.liferayExtension = liferayExtension;
            }

            @Override
            public void execute(Deploy task) {
                if (task.getAutoDeployDir() == null) {
                    task.setAutoDeployDir(liferayExtension.getAutoDeployDir());
                }
            }
        }
    }

    private static class DeployTaskBuildListener extends BuildAdapter {
        private final Deploy deploy;
        private final War warTask;

        public DeployTaskBuildListener(Deploy deploy, War warTask) {
            this.deploy = deploy;
            this.warTask = warTask;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            if (deploy.getWarFile() == null) {
                deploy.setWarFile(warTask.getArchivePath());
            }
        }
    }
}
