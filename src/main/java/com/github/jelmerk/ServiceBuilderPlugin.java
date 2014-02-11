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
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
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
 * Implementation of {@link Plugin} that adds tasks and configuration for generating services using
 * <a href="http://www.liferay.com/community/wiki/-/wiki/Main/Service+Builder">Liferay servicebuilder</a>.
 * It is implemented as a separate plugin so it can be added to both hooks and portlets
 * When you configure this plugin {@link LiferayBasePlugin} is configured as well.
 *
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPlugin implements Plugin<Project> {

    /**
     * The name of the task that generates the servicebuilder service.
     */
    public static final String GENERATE_SERVICE_TASK_NAME = "generateService";

    /**
     * The name of the task that generates the javadoc for the service sourceset.
     */
    public static final String JAVADOC_SERVICE_TASK_NAME = "javadocService";

    /**
     * The name of the task that jars up the classes created from the service sourceset.
     */
    public static final String JAR_SERVICE_TASK_NAME = "jarService";

    /**
     * The name of the configuration that holds the classes required to run servicebuilder.
     */
    public static final String SERVICE_BUILDER_CONFIGURATION_NAME = "serviceBuilder";

    /**
     * The name of the sourceset that holds the classes that represent the public api of your servicebuilder service.
     */
    public static final String SERVICE_SOURCE_SET_NAME = "service";

    /**
     * The name of the configuration that holds the classes that are available when the classes of the service sourceset
     * are compiled.
     */
    public static final String SERVICE_CONFIGURATION_NAME = "service";

    /**
     * The name of the servicebuilder extension.
     */
    public static final String SERVICEBUILDER_EXTENSION_NAME = "servicebuilder";

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createServiceSourceSets(project);

        createServiceBuilderConfiguration(project);
        createServiceConfiguration(project);

        createServiceBuilderExtension(project);

        configureArchives(project);

        configureServiceJavaDoc(project);

        configureBuildServiceTaskDefaults(project);
        createBuildServiceTask(project);
    }

    private void createServiceBuilderConfiguration(Project project) {
        project.getConfigurations().create(SERVICE_BUILDER_CONFIGURATION_NAME)
            .setVisible(false)
            .setDescription("The servicebuilder configuration");
    }

    private void createServiceConfiguration(Project project) {
        project.getConfigurations().create(SERVICE_CONFIGURATION_NAME)
                .setDescription("The service configuration");
    }

    private void createServiceBuilderExtension(Project project) {
        project.getExtensions().create(SERVICEBUILDER_EXTENSION_NAME, ServiceBuilderPluginExtension.class, project);
    }

    private void createServiceSourceSets(Project project) {
        project.getConvention().getPlugin(JavaPluginConvention.class)
                .getSourceSets().create(SERVICE_SOURCE_SET_NAME);
    }

    private void configureArchives(Project project) {

        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        Jar jar = project.getTasks().create(JAR_SERVICE_TASK_NAME, Jar.class);

        jar.setDescription("Assembles a jar archive containing the servicebuilder classes.");
        jar.setGroup(BasePlugin.BUILD_GROUP);
        jar.from(pluginConvention.getSourceSets().getByName(SERVICE_SOURCE_SET_NAME).getOutput());
        jar.setAppendix("service");

        project.getArtifacts().add(SERVICE_CONFIGURATION_NAME, project.getTasks().getByName(JAR_SERVICE_TASK_NAME));
    }


    private void configureBuildServiceTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new BuildServiceTaskDefaultsBuildListener(project));
    }

    private void createBuildServiceTask(Project project) {
        BuildService task = project.getTasks().create(GENERATE_SERVICE_TASK_NAME, BuildService.class);

        project.getGradle().addBuildListener(new BuildServiceTaskBuildListener(project, task));

        task.onlyIf(new BuildServiceTaskOnlyIfSpec());

        task.setDescription("Builds a liferay service");
        task.setGroup(LiferayBasePlugin.LIFERAY_GROUP_NAME);
    }

    private void configureServiceJavaDoc(Project project) {
        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSet serviceSourceSet = javaConvention.getSourceSets().getByName(SERVICE_SOURCE_SET_NAME);
        Javadoc serviceJavadoc = project.getTasks().create(JAVADOC_SERVICE_TASK_NAME, Javadoc.class);
        serviceJavadoc.setDescription("Generates Javadoc API documentation for the servicebuilder service api.");
        serviceJavadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        serviceJavadoc.setClasspath(serviceSourceSet.getOutput().plus(serviceSourceSet.getCompileClasspath()));
        serviceJavadoc.setDestinationDir(new File(javaConvention.getDocsDir(), "serviceJavadoc"));
        serviceJavadoc.setSource(serviceSourceSet.getAllJava());

        Javadoc mainJavadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        mainJavadoc.dependsOn(serviceJavadoc);
    }

    private static final class BuildServiceTaskBuildListener extends BuildAdapter {
        private final Project project;
        private final BuildService task;

        private BuildServiceTaskBuildListener(Project project, BuildService task) {
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

    private static final class BuildServiceTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        private BuildServiceTaskDefaultsBuildListener(Project project) {
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
                projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "log4j:log4j:1.2.9");

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

        private static final class SetBuildServiceTaskDefaultsAction implements Action<BuildService> {

            private final Configuration servicebuilderConfiguration;

            private SetBuildServiceTaskDefaultsAction(Configuration servicebuilderConfiguration) {
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

    private static final class BuildServiceTaskOnlyIfSpec implements Spec<Task> {
        @Override
        public boolean isSatisfiedBy(Task element) {
            BuildService castTask = (BuildService) element; //NOSONAR
            return castTask.getServiceInputFile().exists();
        }
    }
}
