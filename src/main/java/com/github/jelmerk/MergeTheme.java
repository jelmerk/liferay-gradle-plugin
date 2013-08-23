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

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that merges a parent theme and the diffs defined in a theme plugin.
 * When creating a theme in Liferay you usually extend from a parent theme and overwrite only certain files in the
 * childs (the diff)
 *
 * @author Jelmer Kuperus
 */
public class MergeTheme extends DefaultTask {

    private String parentThemeName;

    private String parentThemeProjectName;

    private String themeType;

    private File appServerPortalDir;

    private File outputDir;

    /**
     * Performs the merge theme task.
     */
    @TaskAction
    public void mergeTheme() {
        if (getParentThemeName() != null && getParentThemeProjectName() != null) {
            throw new InvalidUserDataException("Please specify either parentThemeName or parentThemeProjectName " +
                    "but not both.");
        }
        if (getParentThemeName() != null) {
            copyLiferayTheme();
        }
        if (getParentThemeProjectName() != null) {
            copyProjectTheme();
        }

    }

    private void copyLiferayTheme() {
        if ("_unstyled".equals(getParentThemeName())) {
            copyUnstyledTheme();
        } else if ("_styled".equals(getParentThemeName())) {
            copyStyledTheme();
        } else if ("classic".equals(getParentThemeName())) {
            copyClassicTheme();
        }
    }

    private void copyProjectTheme() {
        MergeTheme mergeTask = (MergeTheme) getProject().project(parentThemeProjectName)
                .getTasks().getByName(ThemePlugin.MERGE_THEME_TASK_NAME);

        mergeTask.execute(); // TODO does not work

        File parentThemeOutputDir = mergeTask.getOutputDir();
        copy(parentThemeOutputDir, null, "WEB-INF", getOutputDir());
    }



    private void copyUnstyledTheme() {

        copy(new File(getAppServerPortalDir(), "html/themes/_unstyled"), null, "templates/**", getOutputDir());
        copy(new File(getAppServerPortalDir(), "html/themes/_unstyled/templates"), "*." + getThemeType(),
                "init." + getThemeType(), new File(getOutputDir(), "templates"));

        /*
            <copy todir="docroot" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/_unstyled"
                    excludes="templates/**"
                />
            </copy>

            <copy todir="docroot/templates" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/_unstyled/templates"
                    excludes="init.${theme.type}"
                    includes="*.${theme.type}"
                />
            </copy>
         */
    }

    private void copyStyledTheme() {
        copyUnstyledTheme();
        copy(new File(getAppServerPortalDir(), "html/themes/_styled"), null, null, getOutputDir());

        /*
            <copy todir="docroot" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/_unstyled"
                    excludes="templates/**"
                />
            </copy>

            <copy todir="docroot/templates" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/_unstyled/templates"
                    excludes="init.${theme.type}"
                    includes="*.${theme.type}"
                />
            </copy>

            <copy todir="docroot" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/_styled"
                />
            </copy>
         */
    }

    private void copyClassicTheme() {
        copy(new File(getAppServerPortalDir(), "html/themes/classic"), null, "_diffs/**,templates/**",
                getOutputDir());

        copy(new File(getAppServerPortalDir(), "html/themes/classic/templates"), "*." + getThemeType(), null,
                new File(getOutputDir(), "templates"));


        /*
            <copy todir="docroot" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/classic"
                    excludes="_diffs/**,templates/**"
                />
            </copy>

            <copy todir="docroot/templates" overwrite="true">
                <fileset
                    dir="${app.server.portal.dir}/html/themes/classic/templates"
                    includes="*.${theme.type}"
                />
            </copy>
         */
    }


    private void copy(File dir, String includes, String excludes, File toDir) {

        FileSet fileSet = new FileSet();
        fileSet.setDir(dir);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);

        Copy copy = new Copy();
        copy.setTodir(toDir);
        copy.setOverwrite(true);
        copy.add(fileSet);
        copy.setProject(getAnt().getProject());
        copy.execute();
    }

    @Input
    @Optional
    public String getParentThemeName() {
        return parentThemeName;
    }

    public void setParentThemeName(String parentThemeName) {
        this.parentThemeName = parentThemeName;
    }

    @Input
    @Optional
    public String getParentThemeProjectName() {
        return parentThemeProjectName;
    }

    public void setParentThemeProjectName(String parentThemeProjectName) {
        this.parentThemeProjectName = parentThemeProjectName;
    }

    @Input
    public String getThemeType() {
        return themeType;
    }

    public void setThemeType(String themeType) {
        this.themeType = themeType;
    }

    @InputDirectory
    public File getAppServerPortalDir() {
        return appServerPortalDir;
    }

    public void setAppServerPortalDir(File appServerPortalDir) {
        this.appServerPortalDir = appServerPortalDir;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

}
