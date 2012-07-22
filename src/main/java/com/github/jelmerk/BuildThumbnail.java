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
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Implementation of {@link org.gradle.api.Task} that creates a thumbnail image from a (larger) original image.
 *
 * @author Jelmer Kuperus
 */
public class BuildThumbnail extends DefaultTask {

    /**
     * The default value for the height property.
     */
    public static final int DEFAULT_HEIGHT = 120;

    /**
     * The default value for the width property.
     */
    public static final int DEFAULT_WIDTH = 160;

    private FileCollection classpath;

    private File originalFile;

    private File thumbnailFile;

    private int height = DEFAULT_HEIGHT;

    private int width = DEFAULT_WIDTH;

    private boolean overwrite = true;

    /**
     * Performs the build thumbnail task.
     */
    @TaskAction
    public void buildThumbnail() {

        if (getWidth() <= 0) {
            throw new InvalidUserDataException("Please specify a valid width");
        }

        if (getHeight() <= 0) {
            throw new InvalidUserDataException("Please specify a valid height");
        }

        Java javaTask = new Java();
        javaTask.setClassname("com.liferay.portal.tools.ThumbnailBuilder");

        Project antProject = getAnt().getAntProject();

        Path antClasspath = new Path(antProject);

        for (File dep : getClasspath()) {
            antClasspath.createPathElement()
                    .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(antClasspath);

        javaTask.createArg()
                .setLine("thumbnail.original.file=" + getOriginalFile().getAbsolutePath());

        javaTask.createArg()
                .setLine("thumbnail.thumbnail.file=" + getThumbnailFile().getAbsolutePath());

        javaTask.createArg()
                .setLine("thumbnail.height=" + getHeight());

        javaTask.createArg()
                .setLine("thumbnail.width=" + getWidth());

        javaTask.createArg()
                .setLine("thumbnail.overwrite=" + getOverwrite());

        javaTask.execute();


//						<java
//							classname="com.liferay.portal.tools.ThumbnailBuilder"
//							classpathref="portal.classpath"
//						>
//							<arg value="thumbnail.original.file=@{file}" />
//							<arg value="thumbnail.thumbnail.file=${thumbnail.file}" />
//							<arg value="thumbnail.height=120" />
//							<arg value="thumbnail.width=160" />
//							<arg value="thumbnail.overwrite=false" />
//						</java>
    }

    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Returns a file pointing to the original image based on which the thumbnail will be created.
     *
     * @return a file pointing to the original image based on which the thumbnail will be created
     */
    @InputFile
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * Sets the file pointing to the original image based on which the thumbnail will be created.
     * Setting this field is mandatory.
     *
     * @param originalFile a file pointing to the original image based on which the thumbnail will be created
     */
    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    /**
     * Returns the file pointing to the location to which the generated thumbnail will be written.
     *
     * @return the file pointing to the location to which the generated thumbnail will be written
     */
    @OutputFile
    public File getThumbnailFile() {
        return thumbnailFile;
    }

    /**
     * Set the file pointing to the location to which the generated thumbnail will be written.
     *
     * @param thumbnailFile the file pointing to the location to which the generated thumbnail will be written
     */
    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    /**
     * Returns the height the generated thumbnail will get.
     *
     * @return the height the generated thumbnail will get
     */
    @Input
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height the generated thumbnail will get.
     * If unset this value defaults to 120
     *
     * @param height the height the generated thumbnail will get
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the width the generated thumbnail will get.
     *
     * @return the width the generated thumbnail will get
     */
    @Input
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width the generated thumbnail will get.
     * If unset this value defaults to 160
     *
     * @param width the width the generated thumbnail will get
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns true if generated thumbnail images should overwrite images at the output location. False otherwise
     *
     * @return true if generated thumbnail images should overwrite images at the output location. False otherwise
     */
    @Input
    public boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Set to true if generated thumbnail images should overwrite images at the output location. To False otherwise
     *
     * @param overwrite true if generated thumbnail images should overwrite images at the output location.
     *                  False otherwise
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
