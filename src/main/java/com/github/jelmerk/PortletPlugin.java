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
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;

/**
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    /**
     * The name of the task that processes Syntactically Awesome StyleSheets (SASS) files
     */
    public static final String SASS_TO_CSS_TASK_NAME = "sassToCss";

    /**
     * The name of the configuration that holds the classes required to run sassToCss
     */
    public static final String SASS_CONFIGURATION_NAME = "sass";

    /**
     * {@inheritDoc}
     */
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

    private void configureSassToCssTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new SassToCssTaskDefaultsBuildListener(project));
    }

    private void configureSassToCssTask(Project project) {
        SassToCss task = project.getTasks().add(SASS_TO_CSS_TASK_NAME, SassToCss.class);

        project.getGradle().addBuildListener(new SassToCssTaskBuildListener(project, task));

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

    private static class SassToCssTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        public SassToCssTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

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

            project.getTasks().withType(SassToCss.class,
                    new SetSassToCssTaskDefaultsAction(sassConfiguration, liferayPluginExtension));
        }

        private static class SetSassToCssTaskDefaultsAction implements Action<SassToCss> {
            private final Configuration sassConfiguration;
            private final LiferayPluginExtension liferayPluginExtension;

            public SetSassToCssTaskDefaultsAction(Configuration sassConfiguration,
                                                  LiferayPluginExtension liferayPluginExtension) {
                this.sassConfiguration = sassConfiguration;
                this.liferayPluginExtension = liferayPluginExtension;
            }

            @Override
            public void execute(SassToCss task) {
                if (task.getClasspath() == null) {
                    task.setClasspath(sassConfiguration);
                }
                if (task.getAppServerPortalDir() == null) {
                    task.setAppServerPortalDir(liferayPluginExtension.getAppServerPortalDir());
                }
            }
        }
    }

    private static class SassToCssTaskBuildListener extends BuildAdapter {
        private final Project project;
        private final SassToCss task;

        public SassToCssTaskBuildListener(Project project, SassToCss task) {
            this.project = project;
            this.task = task;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

            if (task.getSassDir() == null) {
                task.setSassDir(warConvention.getWebAppDir());
            }
        }
    }
}
