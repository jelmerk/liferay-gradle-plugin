package com.github.jelmerk;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class BuildThumbnail extends ConventionTask {

    private FileCollection classpath;

    private File originalFile;

    private File thumbnailFile;

    private int height = 0;

    private int width = 0;

    private boolean overwrite;

    @TaskAction
    public void buildThumbnail() {

        if (getOriginalFile() == null) {
            throw new InvalidUserDataException("Please specify a originalFile");
        }

        if (getClasspath() == null) {
            throw new InvalidUserDataException("Please specify the classpath");
        }

        if (getThumbnailFile() == null) {
            throw new InvalidUserDataException("Please specify a thumbnailFile");
        }

        if (getWidth() <= 0) {
            throw new InvalidUserDataException("Please specify a valid width");
        }

        if (getHeight() <= 0) {
            throw new InvalidUserDataException("Please specify a valid height");
        }

        // TODO: i guess we should not generate thumbnails if someone manually added one as well hmmm how do we do that..


        if (!getOriginalFile().exists()) {
            getLogger().log(LogLevel.INFO, getOriginalFile().getAbsolutePath() +
                    " does not exist, not generating thumbnail image.");
            return;
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

        javaTask.createJvmarg()
                .setLine("thumbnail.original.file=" + getOriginalFile().getAbsolutePath());

        javaTask.createJvmarg()
                .setLine("thumbnail.thumbnail.file=" + getThumbnailFile().getAbsolutePath());

        javaTask.createJvmarg()
                .setLine("thumbnail.height=" + getHeight());

        javaTask.createJvmarg()
                .setLine("thumbnail.width=" + getWidth());

        javaTask.createJvmarg()
                .setLine("thumbnail.overwrite=" + getHeight());

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
    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
