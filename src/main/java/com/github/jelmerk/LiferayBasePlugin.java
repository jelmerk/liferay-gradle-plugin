/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jelmerk;

import org.gradle.BuildAdapter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

/**
 * Implementation of {@link Plugin} that registers the liferay extension and allows you to hot deploy plugins to
 * Liferay. Most of the time you should not add this plugin directly and you should use the Gradle plugin appropriate
 * to the Liferay plugin you are developing.
 *
 * @author Jelmer Kuperus
 */
public class LiferayBasePlugin implements Plugin<Project> {

    /**
     * The name of the Liferay extension.
     */
    public static final String LIFERAY_EXTENSION_NAME = "liferay";

    /**
     * The name of the group Liferay specific tasks will be grouped under.
     * (eg the heading you see when you run gradle tasks)
     */
    public static final String LIFERAY_GROUP_NAME = "liferay";

    /**
     * The name of the task that deploys your plugin to Liferay.
     */
    public static final String DEPLOY_TASK_NAME = "deploy";

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
        project.getExtensions().create(LIFERAY_EXTENSION_NAME, LiferayPluginExtension.class, project);
    }

    private void configureDeployTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new DeployTaskDefaultsBuildListener(project));
    }

    private void configureDeployTask(Project project) {
        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        Deploy deploy = project.getTasks().add(DEPLOY_TASK_NAME, Deploy.class);
        deploy.setDescription("Deploys the plugin");
        deploy.setGroup(LiferayBasePlugin.LIFERAY_GROUP_NAME);

        project.getGradle().addBuildListener(new DeployTaskBuildListener(deploy, warTask));
        deploy.dependsOn(warTask);
    }

    private static final class DeployTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        private DeployTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            project.getTasks().withType(Deploy.class, new SetDeployTaskDefaultsAction(project));
        }

        private static final class SetDeployTaskDefaultsAction implements Action<Deploy> {
            private final Project project;

            private SetDeployTaskDefaultsAction(Project project) {
                this.project = project;
            }

            @Override
            public void execute(Deploy task) {
                LiferayPluginExtension liferayExtension = project.getExtensions()
                        .getByType(LiferayPluginExtension.class);

                if (task.getAutoDeployDir() == null) {
                    task.setAutoDeployDir(liferayExtension.getAutoDeployDir());
                }
            }
        }
    }

    private static final class DeployTaskBuildListener extends BuildAdapter {
        private final Deploy deploy;
        private final War warTask;

        private DeployTaskBuildListener(Deploy deploy, War warTask) {
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
