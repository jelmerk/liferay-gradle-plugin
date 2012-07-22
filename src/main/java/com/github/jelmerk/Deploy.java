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
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that deploys a liferay plugin.
 *
 * @author Jelmer Kuperus
 */
public class Deploy extends DefaultTask {

    private File autoDeployDir;
    private File warFile;

    /**
     * Performs the deploy task.
     */
    @TaskAction
    public void deploy() {
        Copy copy = new Copy();

        copy.setFile(getWarFile());
        copy.setTodir(getAutoDeployDir());
        copy.execute();
    }

    /**
     * Returns the Liferay autodeploy dir. Warfiles placed in this folder will automatically be detected and deployed
     * by Liferay.
     *
     * @return the Liferay autodeploy dir
     */
    @OutputDirectory
    public File getAutoDeployDir() {
        return autoDeployDir;
    }

    /**
     * Sets the Liferay autodeploy dir. Warfiles placed in this folder will automatically be detected and deployed
     * by Liferay.
     *
     * @param autoDeployDir the liferay autodeploy dir
     */
    public void setAutoDeployDir(File autoDeployDir) {
        this.autoDeployDir = autoDeployDir;
    }

    /**
     * Returns the plugin artifact to deploy.
     *
     * @return the plugin artifact to deploy
     */
    @InputFile
    public File getWarFile() {
        return warFile;
    }

    /**
     * Sets the plugin artifact to deploy.
     *
     * @param warFile the plugin artifact to deploy
     */
    public void setWarFile(File warFile) {
        this.warFile = warFile;
    }
}
