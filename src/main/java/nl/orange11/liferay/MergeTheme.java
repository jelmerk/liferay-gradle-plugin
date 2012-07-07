package nl.orange11.liferay;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 */
public class MergeTheme extends ConventionTask {

    private String parentThemeName;

    private String themeType;

    private File appServerPortalDir;

    private File deltaDir;
    private File outputDir;

    @TaskAction
    public void mergeTheme() {

        if (getOutputDir() == null) {
            throw new InvalidUserDataException("Please specify a valid outputDir");
        }

        if (getAppServerPortalDir() == null) {
            throw new InvalidUserDataException("Please specify a valid appServerPortalDir");
        }

        if (getParentThemeName() == null) {
            throw new InvalidUserDataException("Please specify a valid parentThemeName");
        }

        if (getThemeType() == null) {
            throw new InvalidUserDataException("Please specify a valid theeType");
        }

        if ("_unstyled".equals(getParentThemeName())) {
            copyUnstyledTheme();
        } else if ("_styled".equals(getParentThemeName())) {
            copyStyledTheme();
        } else if ("classic".equals(getParentThemeName())) {
            copyClassicTheme();
        } else  {
            // TODO : basically we're depending on a theme we created ourself, need to sort out how to do this, since we need to build the theme in that case
        }
    }

    protected void copyDelta() {
        Copy copy = new Copy();

    }

    protected void copyUnstyledTheme() {

        FileSet mainFileSet = new FileSet();
        mainFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/_unstyled"));
        mainFileSet.setExcludes("templates/**");

        Copy mainCopy = new Copy();
        mainCopy.setTodir(getOutputDir());
        mainCopy.setOverwrite(true);
        mainCopy.add(mainFileSet);
        mainCopy.execute();

        FileSet templatesFileSet = new FileSet();
        templatesFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/_unstyled/templates"));
        templatesFileSet.setExcludes("init." + getThemeType());
        templatesFileSet.setIncludes("*." + getThemeType());

        Copy templatesCopy = new Copy();
        templatesCopy.setTodir(new File(getOutputDir(), "templates"));
        templatesCopy.setOverwrite(true);
        templatesCopy.add(templatesFileSet);
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

    protected void copyStyledTheme() {

        copyUnstyledTheme();

        FileSet fileset = new FileSet();
        fileset.setDir(new File(getAppServerPortalDir(), "html/themes/_styled"));

        Copy copy = new Copy();
        copy.setTodir(getOutputDir());
        copy.setOverwrite(true);
        copy.add(fileset);
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

    protected void copyClassicTheme() {


        FileSet mainFileSet = new FileSet();
        mainFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/classic"));
        mainFileSet.setExcludes("_diffs/**,templates/**");

        Copy mainCopy = new Copy();
        mainCopy.setTodir(getOutputDir());
        mainCopy.setOverwrite(true);
        mainCopy.add(mainFileSet);
        mainCopy.execute();

        FileSet templatesFileSet = new FileSet();
        templatesFileSet.setDir(new File(getAppServerPortalDir(), "html/themes/classic/templates"));
        templatesFileSet.setIncludes("*." + getThemeType());

        Copy templatesCopy = new Copy();
        templatesCopy.setTodir(new File(getOutputDir(), "templates"));
        templatesCopy.setOverwrite(true);
        templatesCopy.add(templatesFileSet);
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
    public File getDeltaDir() {
        return deltaDir;
    }

    public void setDeltaDir(File deltaDir) {
        this.deltaDir = deltaDir;
    }

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

}
