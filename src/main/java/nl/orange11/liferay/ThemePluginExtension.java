package nl.orange11.liferay;

/**
 * @author Jelmer Kuperus
 */
public class ThemePluginExtension {

    private String parentThemeName;
    private String themeType = "vm";

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
}
