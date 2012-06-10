package nl.orange11.liferay;

import org.apache.tools.ant.taskdefs.Copy;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class Deploy extends ConventionTask {

    private File autoDeployDir;
    private File warFile;

    @TaskAction
    public void deploy() {
        if (getAutoDeployDir() == null || !getAutoDeployDir().exists()) {
            throw new InvalidUserDataException("Please specify a valid autoDeployDir");
        }
        if (getWarFile() == null || !getWarFile().exists()) {
            throw new InvalidUserDataException("Please specify a valid warFile");
        }

        Copy copy = new Copy();

        copy.setFile(getWarFile());
        copy.setTodir(getAutoDeployDir());
        copy.execute();
    }

    public File getAutoDeployDir() {
        return autoDeployDir;
    }

    public void setAutoDeployDir(File autoDeployDir) {
        this.autoDeployDir = autoDeployDir;
    }

    public File getWarFile() {
        return warFile;
    }

    public void setWarFile(File warFile) {
        this.warFile = warFile;
    }
}
