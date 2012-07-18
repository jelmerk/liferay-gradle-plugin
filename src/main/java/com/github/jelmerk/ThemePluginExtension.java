package com.github.jelmerk;


import org.gradle.api.Project;

import java.io.File;

/**
 * Extension that holds theme specific configuration.
 *
 * @author Jelmer Kuperus
 */
public class ThemePluginExtension {

    private String parentThemeName;
    private String themeType = "vm";

    private String diffsDirName = "src/main/diffs";

    private final Project project;

    /**
     * Constructs a new ThemePluginExtension.
     *
     * @param project the project this extension is applied to
     */
    public ThemePluginExtension(Project project) {
        this.project = project;
    }

    /**
     * Returns the name of the parent theme this theme extends from.
     *
     * @return the name of the theme this theme extends from
     */
    public String getParentThemeName() {
        return parentThemeName;
    }

    /**
     * Set the name of the parent theme this theme extends from. Possible values
     * are "_unstyled", "_styled" and "classic".
     *
     * @param parentThemeName the name of the parent theme this theme extends from
     */
    public void setParentThemeName(String parentThemeName) {
        this.parentThemeName = parentThemeName;
    }

    /**
     * Returns the theme type.
     *
     * @return the theme type
     */
    public String getThemeType() {
        return themeType;
    }

    /**
     * Sets the theme type. Possible values are vm and ftl.
     * If unset this property defaults to "vm"
     *
     * @param themeType the theme type
     */
    public void setThemeType(String themeType) {
        this.themeType = themeType;
    }

    /**
     * Returns the path to the folder that contains the diffs to the selected parent theme. Diffs are files that
     * overwrite files contained in the parent theme.
     *
     * @return the path to the folder that contains the diffs
     */
    public String getDiffsDirName() {
        return diffsDirName;
    }

    /**
     * Sets the path to the folder that contains the diffs to the selected parent theme. Diffs are files that
     * overwrite files contained in the parent theme.
     * If unset this property defaults to "src/main/diffs"
     *
     * @param diffsDirName the path to the folder that contains the diffs
     */
    public void setDiffsDirName(String diffsDirName) {
        this.diffsDirName = diffsDirName;
    }

    /**
     * Returns a file pointing to the folder that contains the diffs to the selected parent theme. Diffs are files that
     * overwrite files contained in the parent theme.
     *
     * @return a file pointing to the folder that contains the diffs to the selected parent theme
     */
    public File getDiffsDir() {
        return project.file(diffsDirName);
    }
}
