package com.github.jelmerk;


import org.gradle.api.Project;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class ThemePluginExtension {

    private String parentThemeName;
    private String themeType = "vm";

    private String diffsDirName = "src/main/diffs";

    private Project project;

    public ThemePluginExtension(Project project) {
        this.project = project;
    }

    public String getParentThemeName() {
        return parentThemeName;
    }

    public void setParentThemeName(String parentThemeName) {
        this.parentThemeName = parentThemeName;
    }

    public String getThemeType() {
        return themeType;
    }

    public void setThemeType(String themeType) {
        this.themeType = themeType;
    }

    public String getDiffsDirName() {
        return diffsDirName;
    }

    public void setDiffsDirName(String diffsDirName) {
        this.diffsDirName = diffsDirName;
    }

    public File getDiffsDir() {
        return project.file(diffsDirName);
    }
}
