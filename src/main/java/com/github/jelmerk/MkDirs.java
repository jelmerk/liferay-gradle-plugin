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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that creates a directory.
 *
 * @author Jelmer Kuperus
 */
public class MkDirs extends DefaultTask {

    private File dir;

    /**
     * Performs the mkdirs task.
     */
    @TaskAction
    public void mkdirs() {
        dir.mkdirs();
    }

    /**
     * Returns the file pointing to the folder to create.
     *
     * @return the file pointing to the folder to create
     */
    @OutputDirectory
    public File getDir() {
        return dir;
    }

    /**
     * Sets the file pointing to the folder to create.
     *
     * @param dir the file pointing to the folder to create
     */
    public void setDir(File dir) {
        this.dir = dir;
    }
}
