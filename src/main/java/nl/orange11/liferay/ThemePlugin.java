package nl.orange11.liferay;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class ThemePlugin implements Plugin<Project> {

    private static final String BUILD_THUMBNAIL = "buildThumbnail";
    private static final String MERGE_THEME = "mergeTheme";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createThemeExtension(project);

        configureBuildThumbnailRule(project);

        configureMergeTemplateTask(project);
        configureBuildThumbnailTask(project);
    }

    private void createThemeExtension(Project project) {
        project.getExtensions().create("theme", ThemePluginExtension.class, project);
    }

    private void configureMergeTemplateTask(Project project) {

        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        ThemePluginExtension themeExtension = project.getExtensions().getByType(ThemePluginExtension.class);

        LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);

        MergeTheme task = project.getTasks().add(MERGE_THEME, MergeTheme.class);
        task.setThemeType(themeExtension.getThemeType());
        task.setParentThemeName(themeExtension.getParentThemeName());
        task.setDeltaDir(warConvention.getWebAppDir());
        task.setAppServerPortalDir(liferayExtension.getAppServerPortalDir());
        // task.setOutputDir();  // TODO what would be the output dir

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(task);
    }

    private void configureBuildThumbnailRule(final Project project) {
        project.getTasks().withType(BuildThumbnail.class, new Action<BuildThumbnail>() {
            @Override
            public void execute(BuildThumbnail task) {
                configureBuildThumbnailDefaults(project, task);
            }
        });
    }

    protected void configureBuildThumbnailDefaults(Project project, BuildThumbnail task) {

        final LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);

        task.getConventionMapping().map("classpath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() throws Exception {
                return liferayExtension.getPortalClasspath();
            }
        });
    }

    private void configureBuildThumbnailTask(Project project) {

        WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        Task mergeTask = project.getTasks().getByName(MERGE_THEME);

        BuildThumbnail task = project.getTasks().add(BUILD_THUMBNAIL, BuildThumbnail.class);
        //task.setThumbnailFile();  // TODO: where is the output dir..
        task.setOriginalFile(new File(warConvention.getWebAppDir(), "images/screenshot.png"));
        task.setHeight(120);
        task.setWidth(160);

        task.dependsOn(mergeTask);
    }

}
