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
 * @author Jelmer Kuperus
 */
public class BuildThumbnail extends DefaultTask {

    private FileCollection classpath;

    private File originalFile;

    private File thumbnailFile;

    private int height = 120; // NOSONAR

    private int width = 160; // NOSONAR

    private boolean overwrite = true;

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

    @InputFile
    public File getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(File originalFile) {
        this.originalFile = originalFile;
    }

    @OutputFile
    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    @Input
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Input
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Input
    public boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
