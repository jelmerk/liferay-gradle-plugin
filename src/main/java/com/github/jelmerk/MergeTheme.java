/*
 * Copyright 2002-2010 the original author or authors.
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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 */
public class MergeTheme extends DefaultTask {

    private String parentThemeName;

    private String themeType;

    private File appServerPortalDir;

    private File diffsDir;
    private File outputDir;

    @TaskAction
    public void mergeTheme() {

        if ("_unstyled".equals(getParentThemeName())) {
            copyUnstyledTheme();
        } else if ("_styled".equals(getParentThemeName())) {
            copyStyledTheme();
        } else if ("classic".equals(getParentThemeName())) {
            copyClassicTheme();
        }

        // else  {
            // TODO : basically we're depending on a theme we created ourself, need to sort out how to do this, since we need to build the theme in that case
        //}

        copyDiffs();
    }

    private void copyDiffs() {
        Project antProject = getAnt().getProject();

        FileSet mainFileSet = new FileSet();
        mainFileSet.setDir(getDiffsDir());

        Copy mainCopy = new Copy();
        mainCopy.setTodir(getOutputDir());
        mainCopy.setOverwrite(true);
        mainCopy.add(mainFileSet);
        mainCopy.setProject(antProject);
        mainCopy.execute();
    }

    private void copyUnstyledTheme() {

        Project antProject = getAnt().getProject();

        FileSet mainFileSet = new FileSet();
        mainFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/_unstyled"));
        mainFileSet.setExcludes("templates/**");

        Copy mainCopy = new Copy();
        mainCopy.setTodir(getOutputDir());
        mainCopy.setOverwrite(true);
        mainCopy.add(mainFileSet);
        mainCopy.setProject(antProject);
        mainCopy.execute();

        FileSet templatesFileSet = new FileSet();
        templatesFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/_unstyled/templates"));
        templatesFileSet.setExcludes("init." + getThemeType());
        templatesFileSet.setIncludes("*." + getThemeType());

        Copy templatesCopy = new Copy();
        templatesCopy.setTodir(new File(getOutputDir(), "templates"));
        templatesCopy.setOverwrite(true);
        templatesCopy.add(templatesFileSet);
        templatesCopy.setProject(antProject);
        templatesCopy.execute();

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


        Project antProject = getAnt().getProject();

        FileSet fileset = new FileSet();
        fileset.setDir(new File(getAppServerPortalDir(), "html/themes/_styled"));

        Copy copy = new Copy();
        copy.setTodir(getOutputDir());
        copy.setOverwrite(true);
        copy.add(fileset);
        copy.setProject(antProject);
        copy.execute();

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

        Project antProject = getAnt().getProject();

        FileSet mainFileSet = new FileSet();
        mainFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/classic"));
        mainFileSet.setExcludes("_diffs/**,templates/**");

        Copy mainCopy = new Copy();
        mainCopy.setTodir(getOutputDir());
        mainCopy.setOverwrite(true);
        mainCopy.add(mainFileSet);
        mainCopy.setProject(antProject);
        mainCopy.execute();

        FileSet templatesFileSet = new FileSet();
        templatesFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/classic/templates"));
        templatesFileSet.setIncludes("*." + getThemeType());

        Copy templatesCopy = new Copy();
        templatesCopy.setTodir(new File(getOutputDir(), "templates"));
        templatesCopy.setOverwrite(true);
        templatesCopy.add(templatesFileSet);
        templatesCopy.setProject(antProject);
        templatesCopy.execute();


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


    @Input
    public String getParentThemeName() {
        return parentThemeName;
    }

    public void setParentThemeName(String parentThemeName) {
        this.parentThemeName = parentThemeName;
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

    @InputDirectory
    public File getDiffsDir() {
        return diffsDir;
    }

    public void setDiffsDir(File diffsDir) {
        this.diffsDir = diffsDir;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

}
