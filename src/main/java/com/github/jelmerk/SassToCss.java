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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that processes Syntactically Awesome StyleSheets (SASS) files.
 *
 * @author Jelmer Kuperus
 */
public class SassToCss extends DefaultTask {

    private FileCollection classpath;

    private File appServerPortalDir;

    private File sassDir;

    /**
     * Performs the sassToCss task.
     */
    @TaskAction
    public void sassToCss() {

        Java javaTask = new Java();

        javaTask.setTaskName("sass to css builder");
        javaTask.setClassname("com.liferay.portal.tools.SassToCssBuilder");


        Project antProject = getAnt().getAntProject();

        Path antClasspath = new Path(antProject);

        for (File dep : getClasspath()) {
            antClasspath.createPathElement()
                     .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(antClasspath);

        javaTask.setFork(true);
        javaTask.setNewenvironment(true);

        javaTask.createArg()
                .setLine("sass.dir=" + "/");

        javaTask.createArg()
                .setLine("sass.docroot.dir=" + getSassDir());

        javaTask.createArg()
                .setLine("sass.portal.common.dir=" + new File(getAppServerPortalDir(), "/html/css/common"));

        javaTask.createJvmarg().setLine("-Dliferay.lib.portal.dir=" + new File(getAppServerPortalDir(), "WEB-INF/lib"));

        javaTask.execute();
    }

    /**
     * Returns a file pointing to the folder that holds the sass files.
     *
     * @return a file pointing to the folder that holds the sass files
     */
    @OutputDirectory
    public File getSassDir() {
        return sassDir;
    }

    /**
     * Sets the file pointing to the folder that holds the sass files.
     *
     * @param sassDir the file pointing to the folder that holds the sass files
     */
    public void setSassDir(File sassDir) {
        this.sassDir = sassDir;
    }

    /**
     * Returns a file collection that contains the classes required to run liferay's SassToCssBuilder.
     *
     * @return a file collection that contains the classes required to run liferay's SassToCssBuilder
     */
    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the file collection that contains the classes required to run liferay's SassToCssBuilder.
     *
     * @param classpath the file collection that contains the classes required to run liferay's SassToCssBuilder
     */
    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Returns the appServerPortalDir. A file pointing to the exploded Liferay web application.
     *
     * @return a file pointing to the exploded Liferay web application
     */
    @InputDirectory
    public File getAppServerPortalDir() {
        return appServerPortalDir;
    }

    /**
     * Sets the appServerPortalDir. A file pointing to the exploded Liferay web application.
     *
     * @param appServerPortalDir file pointing to the exploded Liferay web application
     */
    public void setAppServerPortalDir(File appServerPortalDir) {
        this.appServerPortalDir = appServerPortalDir;
    }
}
