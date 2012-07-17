package com.github.jelmerk;

import org.apache.tools.ant.taskdefs.Copy;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Deploys a liferay plugin.
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
