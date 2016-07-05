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
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.*;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;

/**
 * Implementation of plugin delegate class that adds tasks and configuration for compiling sass css files.
 *
 *
 * @author Alessandro Palumbo
 */
public class SassCompilationPluginDelegate {

    /**
     * The name of the task that processes Syntactically Awesome StyleSheets (SASS) files.
     */
    public static final String SASS_TO_CSS_TASK_NAME = "sassToCss";

    /**
     * The name of the task that copies css files to the the sass directory in the build folder
     * for processing by sassToCss.
     */
    public static final String COPY_SASS_FILES_TASK_NAME = "copySassFiles";

    /**
     * The name of the configuration that holds the classes required to run sassToCss.
     */
    public static final String SASS_CONFIGURATION_NAME = "sass";

    /**
     * The name of the task that copies the merged theme files in the build folder.
     */
    public static final String COPY_THEME_FILES_TASK_NAME = "copyThemeFiles";

    /**
     * The name of the task that copies the compiled css files from .sass-cache to the main directory.
     */
    public static final String COPY_CSS_FROM_CACHE_TASK_NAME = "copyCssFromCache";

    private static final String SASS_OUTPUT_DIR = "sass";


    public void doApply(Project project) {
        createSassConfiguration(project);

        configureSassToCssTaskDefaults(project);

        createCopyThemeFilesTask(project);
        createCopySassFilesTask(project);
        createSassToCssTask(project);
        createCopyCssFromCacheTask(project);
        addSassCompilationToWarTask(project);
    }

    private void createSassConfiguration(Project project) {
        project.getConfigurations().create(SASS_CONFIGURATION_NAME)
            .setVisible(false)
            .setDescription("The sass configuration");
    }

    private void configureSassToCssTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new SassToCssTaskDefaultsBuildListener(project));
    }

    private void createCopyThemeFilesTask(Project project) {
        project.getTasks().create(COPY_THEME_FILES_TASK_NAME, Copy.class)
                .from(new File(project.getBuildDir(), ThemePlugin.MERGE_TASK_PARENT_SOURCE_DIR))
                .setIncludes(asList("**/*"))
                .setExcludes(asList("**/.sass-cache/**/*"))
                .into(new File(project.getBuildDir(), SASS_OUTPUT_DIR));
    }

    private void createCopySassFilesTask(Project project) {
        Task copyThemeCssFilesTask = project.getTasks().getByName(COPY_THEME_FILES_TASK_NAME);

        AbstractCopyTask task = project.getTasks().create(COPY_SASS_FILES_TASK_NAME, Copy.class)
            .from(new WebAppDirCallable(project))
            .setIncludes(asList("**/*"))
            .into(new File(project.getBuildDir(), SASS_OUTPUT_DIR));

        task.dependsOn(copyThemeCssFilesTask);
    }

    private void createSassToCssTask(Project project) {
        Task prepareSassTask = project.getTasks().getByName(COPY_SASS_FILES_TASK_NAME);

        SassToCss task = project.getTasks().create(SASS_TO_CSS_TASK_NAME, SassToCss.class);
        task.setSassDir(new File(project.getBuildDir(), SASS_OUTPUT_DIR));
        task.dependsOn(prepareSassTask);
    }

    private void createCopyCssFromCacheTask(Project project) {
        Task sassToCssTask = project.getTasks().getByName(SASS_TO_CSS_TASK_NAME);

        AbstractCopyTask task = project.getTasks().create(COPY_CSS_FROM_CACHE_TASK_NAME, Copy.class)
                .from(new File(project.getBuildDir(), SASS_OUTPUT_DIR))
                .setIncludes(asList("**/.sass-cache/**/*"))
                .eachFile(new Action<FileCopyDetails>() {
                    @Override
                    public void execute(FileCopyDetails fileCopyDetails) {
                        RelativePath relativePath = fileCopyDetails.getRelativePath();
                        RelativePath parentPath = relativePath.getParent();
                        if (relativePath.isFile() &&
                                parentPath.getLastName().equals(".sass-cache")) {
                            parentPath = parentPath.replaceLastName(relativePath.getLastName());
                            RelativePath updatedPath = RelativePath.parse(true, parentPath.getPathString());
                            fileCopyDetails.setRelativePath(updatedPath);
                        }
                    }
                })
                .into(new File(project.getBuildDir(), SASS_OUTPUT_DIR));

        task.dependsOn(sassToCssTask);
    }

    private void addSassCompilationToWarTask(Project project) {
        Task copyCssFromCacheTask = project.getTasks().getByName(COPY_CSS_FROM_CACHE_TASK_NAME);

        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(copyCssFromCacheTask);

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("dir", new File(project.getBuildDir(), SASS_OUTPUT_DIR));
        args.put("include", "**/*");

        FileTree generatedSassCaches = project.fileTree(args);

        warTask.from(generatedSassCaches);
    }

    private static final class SassToCssTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        private SassToCssTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {

            LiferayPluginExtension liferayPluginExtension = project.getExtensions()
                    .findByType(LiferayPluginExtension.class);

            Configuration sassConfiguration = project.getConfigurations().getByName(SASS_CONFIGURATION_NAME);

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

        private static final class SetSassToCssTaskDefaultsAction implements Action<SassToCss> {
            private final Configuration sassConfiguration;
            private final LiferayPluginExtension liferayPluginExtension;

            private SetSassToCssTaskDefaultsAction(Configuration sassConfiguration,
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

    private static final class WebAppDirCallable implements Callable<File> {
        private final Project project;

        private WebAppDirCallable(Project project) {
            this.project = project;
        }

        @Override
        public File call() {
            WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
            return warConvention.getWebAppDir();
        }
    }
}
