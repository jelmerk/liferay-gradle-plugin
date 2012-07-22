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
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.specs.Spec;

import java.io.File;

/**
 * Implementation of {@link Plugin} that adds tasks and configuration for creating Liferay themes.
 * When you configure this plugin {@link LiferayBasePlugin} is configured as well
 *
 * @author Jelmer Kuperus
 */
public class ThemePlugin implements Plugin<Project> {

    /**
     * The name of the task that builds the thumbnail out of a larger image.
     */
    public static final String BUILD_THUMBNAIL_TASK_NAME = "buildThumbnail";

    /**
     * The name of the task that merges the parent theme and the diffs.
     */
    public static final String MERGE_THEME_TASK_NAME = "mergeTheme";

    /**
     * The name of the theme extension.
     */
    public static final String THEME_EXTENSION_NAME = "theme";

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createThemeExtension(project);

        configureWar(project);

        configureMergeTemplateTaskDefaults(project);
        configureMergeTemplateTask(project);

        configureBuildThumbnailTaskDefaults(project);
        configureBuildThumbnailTask(project);
    }

    private void configureWar(Project project) {
        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        warConvention.setWebAppDirName(new File(project.getBuildDir(), "webapp").getAbsolutePath());
    }

    private void createThemeExtension(Project project) {
        project.getExtensions().create(THEME_EXTENSION_NAME, ThemePluginExtension.class, project);
    }

    private void configureMergeTemplateTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new MergeTemplateTaskDefaultsBuildListener(project));
    }

    private void configureMergeTemplateTask(Project project) {

        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        ThemePluginExtension themeExtension = project.getExtensions().getByType(ThemePluginExtension.class);

        MergeTheme task = project.getTasks().add(MERGE_THEME_TASK_NAME, MergeTheme.class);
        task.setThemeType(themeExtension.getThemeType());

        project.getGradle().addBuildListener(new MergeTemplateTaskBuildListener(task, themeExtension, warConvention));
    }

    private void configureBuildThumbnailTaskDefaults(Project project) {
        project.getGradle().addBuildListener(new BuildThumbnailTaskDefaultsBuildListener(project));
    }

    private void configureBuildThumbnailTask(Project project) {
        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        ThemePluginExtension themeExtension = project.getExtensions().getByType(ThemePluginExtension.class);

        Task mergeTask = project.getTasks().getByName(MERGE_THEME_TASK_NAME);

        BuildThumbnail task = project.getTasks().add(BUILD_THUMBNAIL_TASK_NAME, BuildThumbnail.class);

        project.getGradle().addBuildListener(new BuildThumbnailTaskBuildListener(task, themeExtension, warConvention));
        task.dependsOn(mergeTask);

        task.onlyIf(new ThumbnailTaskOnlyIfSpec(themeExtension));

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

    private static final class BuildThumbnailTaskBuildListener extends BuildAdapter {
        private final BuildThumbnail task;
        private final ThemePluginExtension themeExtension;
        private final WarPluginConvention warConvention;

        private BuildThumbnailTaskBuildListener(BuildThumbnail task,
                                                ThemePluginExtension themeExtension,
                                                WarPluginConvention warConvention) {
            this.task = task;
            this.themeExtension = themeExtension;
            this.warConvention = warConvention;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {

            // only set the default if nothing was changed from say a configuration closure

            if (task.getOriginalFile() == null) {
                task.setOriginalFile(new File(themeExtension.getDiffsDir(), "images/screenshot.png"));
            }

            if (task.getThumbnailFile() == null) {
                task.setThumbnailFile(new File(warConvention.getWebAppDir(), "images/thumbnail.png"));
            }
        }
    }

    private static final class BuildThumbnailTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        private BuildThumbnailTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);
            project.getTasks().withType(BuildThumbnail.class, new SetBuildThumbnailDefaultsAction(liferayExtension));
        }

        private static final class SetBuildThumbnailDefaultsAction implements Action<BuildThumbnail> {
            private final LiferayPluginExtension liferayExtension;

            private SetBuildThumbnailDefaultsAction(LiferayPluginExtension liferayExtension) {
                this.liferayExtension = liferayExtension;
            }

            @Override
            public void execute(BuildThumbnail task) {
                if (task.getClasspath() == null) {
                    task.setClasspath(liferayExtension.getPortalClasspath());
                }
            }
        }
    }

    private static final class MergeTemplateTaskBuildListener extends BuildAdapter {
        private final MergeTheme task;
        private final ThemePluginExtension themeExtension;
        private final WarPluginConvention warConvention;

        private MergeTemplateTaskBuildListener(MergeTheme task,
                                               ThemePluginExtension themeExtension,
                                               WarPluginConvention warConvention) {
            this.task = task;
            this.themeExtension = themeExtension;
            this.warConvention = warConvention;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            if (task.getThemeType() == null) {
                task.setThemeType(themeExtension.getThemeType());
            }

            if (task.getParentThemeName() == null) {
                task.setParentThemeName(themeExtension.getParentThemeName());
            }

            if (task.getParentThemeProjectName() == null) {
                task.setParentThemeProjectName(themeExtension.getParentThemeProjectName());
            }

            if (task.getDiffsDir() == null) {
                task.setDiffsDir(themeExtension.getDiffsDir());
            }

            if (task.getOutputDir() == null) {
                task.setOutputDir(warConvention.getWebAppDir());
            }
        }
    }

    private static final class MergeTemplateTaskDefaultsBuildListener extends BuildAdapter {
        private final Project project;

        private MergeTemplateTaskDefaultsBuildListener(Project project) {
            this.project = project;
        }

        @Override
        public void projectsEvaluated(Gradle gradle) {
            project.getTasks().withType(MergeTheme.class, new SetMergeThemeTaskDefaultsAction(project));
        }

        private static final class SetMergeThemeTaskDefaultsAction implements Action<MergeTheme> {
            private final Project project;

            private SetMergeThemeTaskDefaultsAction(Project project) {
                this.project = project;
            }

            @Override
            public void execute(MergeTheme mergeTheme) {
                LiferayPluginExtension liferayExtension = project.getExtensions()
                        .getByType(LiferayPluginExtension.class);

                if (mergeTheme.getAppServerPortalDir() == null) {
                    mergeTheme.setAppServerPortalDir(liferayExtension.getAppServerPortalDir());
                }
            }
        }
    }

    private static final class ThumbnailTaskOnlyIfSpec implements Spec<Task> {
        private final ThemePluginExtension themeExtension;

        private ThumbnailTaskOnlyIfSpec(ThemePluginExtension themeExtension) {
            this.themeExtension = themeExtension;
        }

        @Override
        public boolean isSatisfiedBy(Task element) {
            BuildThumbnail castTask = (BuildThumbnail) element; //NOSONAR

            return castTask.getOriginalFile().exists() &&
                    !new File(themeExtension.getDiffsDir(), "images/thumbnail.png").exists();
        }
    }
}
