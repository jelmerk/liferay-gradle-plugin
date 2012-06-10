package nl.orange11.liferay;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    public static final String SASS_TO_CSS ="sas-to-css";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        configureSassToCss(project);
    }

    private void configureSassToCss(Project project) {

        final WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        final SassToCss buildCss = project.getTasks().add(SASS_TO_CSS, SassToCss.class);

        buildCss.getConventionMapping().map("sassDir", new Callable<File>() {
            public File call() throws Exception {
                return warConvention.getWebAppDir();
            }
        });

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        warTask.dependsOn(buildCss);
    }

}
