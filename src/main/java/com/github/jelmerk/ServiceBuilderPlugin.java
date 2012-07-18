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
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPlugin implements Plugin<Project> {

    public static final String GENERATE_SERVICE = "generateService";

    public static final String JAVADOC_SERVICE = "javadocService";

    public static final String JAR_SERVICE = "jarService";

    public static final String SERVICE_BUILDER_CONFIGURATION_NAME = "serviceBuilder";

    public static final String SERVICE_SOURCE_SET_NAME = "service";

    public static final String SERVICE_CONFIGURATION_NAME = "service";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        configureSourceSets(project);
        configureConfigurations(project);
        createServiceBuilderExtension(project);

        configureArchives(project);

        configureServiceJavaDoc(project);

        configureBuildServiceTaskDefaults(project);
        configureBuildServiceTask(project);
    }

    private void configureConfigurations(Project project) {

        Configuration serviceBuilderConfiguration = project.getConfigurations().add(SERVICE_BUILDER_CONFIGURATION_NAME);
        serviceBuilderConfiguration.setVisible(false);
        serviceBuilderConfiguration.setDescription("The servicebuilder configuration");

        Configuration serviceConfiguration = project.getConfigurations().add(SERVICE_CONFIGURATION_NAME);
        serviceConfiguration.setDescription("The service configuration");
    }

    private void createServiceBuilderExtension(Project project) {
        project.getExtensions().create("servicebuilder", ServiceBuilderPluginExtension.class, project);
    }

    private void configureSourceSets(Project project) {
        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        pluginConvention.getSourceSets().add(SERVICE_SOURCE_SET_NAME);
    }

    private void configureArchives(Project project) {

        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        Jar jar = project.getTasks().add(JAR_SERVICE, Jar.class);

        jar.setDescription("Assembles a jar archive containing the servicebuilder classes.");
        jar.setGroup(BasePlugin.BUILD_GROUP);
        jar.from(pluginConvention.getSourceSets().getByName(SERVICE_SOURCE_SET_NAME).getOutput());
        jar.setAppendix("service");

        project.getArtifacts().add(SERVICE_CONFIGURATION_NAME, project.getTasks().getByName(JAR_SERVICE));
    }


    private void configureBuildServiceTaskDefaults(final Project project) {
        project.getGradle().addBuildListener(new BuildServiceTaskDefaultsBuildListener(project));
    }

    private void configureBuildServiceTask(final Project project) {
        final BuildService task = project.getTasks().add(GENERATE_SERVICE, BuildService.class);

        project.getGradle().addBuildListener(new BuildServiceTaskBuildListener(project, task));

        task.onlyIf(new BuildServiceTaskOnlyIfSpec());

        task.setDescription("Builds a liferay service");
        task.setGroup(LiferayBasePlugin.LIFERAY_GROUP);
    }

    private void configureServiceJavaDoc(Project project) {
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSet serviceSourceSet = javaConvention.getSourceSets().getByName(SERVICE_SOURCE_SET_NAME);
        Javadoc serviceJavadoc = project.getTasks().add(JAVADOC_SERVICE, Javadoc.class);
        serviceJavadoc.setDescription("Generates Javadoc API documentation for the servicebuilder service api.");
        serviceJavadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        serviceJavadoc.setClasspath(serviceSourceSet.getOutput().plus(serviceSourceSet.getCompileClasspath()));
        serviceJavadoc.setDestinationDir(new File(javaConvention.getDocsDir(), "serviceJavadoc"));
        serviceJavadoc.setSource(serviceSourceSet.getAllJava());

        Javadoc mainJavadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        mainJavadoc.dependsOn(serviceJavadoc);
    }


    private static class BuildServiceTaskBuildListener extends BuildAdapter {
        private final Project project;
        private final BuildService task;

        public BuildServiceTaskBuildListener(Project project, BuildService task) {
            this.project = project;
            this.task = task;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {

            WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

            ServiceBuilderPluginExtension serviceBuilderExtension = project.getExtensions()
                    .getByType(ServiceBuilderPluginExtension.class);

            if (task.getPluginName() == null) {
                task.setPluginName(project.getName());
            }

            if (task.getServiceInputFile() == null) {
                task.setServiceInputFile(serviceBuilderExtension.getServiceInputFile());
            }

            if (task.getJalopyInputFile() == null) {
                task.setJalopyInputFile(serviceBuilderExtension.getJalopyInputFile());
            }

            if (task.getImplSrcDir() == null) {
                task.setImplSrcDir(serviceBuilderExtension.getImplSrcDir());
            }

            if (task.getApiSrcDir() == null) {
                task.setApiSrcDir(serviceBuilderExtension.getApiSrcDir());
            }

            if (task.getResourceDir() == null) {
                task.setResourceDir(serviceBuilderExtension.getResourceDir());
            }

            if (task.getWebappSrcDir() == null) {
                task.setWebappDir(warConvention.getWebAppDir());
            }
        }
    }

    private static class BuildServiceTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        public BuildServiceTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {

            LiferayPluginExtension liferayExtension = project.getExtensions()
                    .getByType(LiferayPluginExtension.class);

            Configuration servicebuilderConfiguration = project.getConfigurations()
                    .getByName(SERVICE_BUILDER_CONFIGURATION_NAME);

            if (servicebuilderConfiguration.getDependencies().isEmpty()) {

                // the sdk dependencies : we will need to download those

                DependencyHandler projectDependencies = project.getDependencies();

                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "com.thoughtworks.qdox:qdox:1.12");
                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "jalopy:jalopy:1.5rc3");
                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.servlet:servlet-api:2.5");
                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.servlet.jsp:jsp-api:2.1");
                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.activation:activation:1.1");

                //  the portal classpath dependencies : we have those locally

                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, liferayExtension.getPortalClasspath());

                // the common classpath dependencies : we can get from the portal

                File appServerGlobalLibDirName = liferayExtension.getAppServerGlobalLibDir();

                FileCollection appserverClasspath = project.files(
                        new File(appServerGlobalLibDirName, "commons-digester.jar"),
                        new File(appServerGlobalLibDirName, "commons-lang.jar"),
                        new File(appServerGlobalLibDirName, "easyconf.jar")
                );

                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, appserverClasspath);
            }

            project.getTasks().withType(BuildService.class,
                    new SetBuildServiceTaskDefaultsAction(servicebuilderConfiguration));
        }

        private static class SetBuildServiceTaskDefaultsAction implements Action<BuildService> {

            private final Configuration servicebuilderConfiguration;

            public SetBuildServiceTaskDefaultsAction(Configuration servicebuilderConfiguration) {
                this.servicebuilderConfiguration = servicebuilderConfiguration;
            }

            @Override
            public void execute(BuildService task) {
                if (task.getClasspath() == null) {
                    task.setClasspath(servicebuilderConfiguration);
                }
            }
        }
    }

    private static class BuildServiceTaskOnlyIfSpec implements Spec<Task> {
        @Override
        public boolean isSatisfiedBy(Task element) {
            BuildService castTask = (BuildService) element; //NOSONAR
            return castTask.getServiceInputFile().exists();
        }
    }
}
