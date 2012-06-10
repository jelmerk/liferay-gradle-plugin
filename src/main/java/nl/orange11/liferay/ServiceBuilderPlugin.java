package nl.orange11.liferay;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPlugin implements Plugin<Project> {

    public static final String BUILD_SERVICE ="build-service";

    public static final String SERVICE_BUILDER_SOURCE_SET_NAME = "servicebuilder main";
    public static final String SERVICE_BUILDER_TEST_SOURCE_SET_NAME = "servicebuilder test";


    @Override
    public void apply(Project project) {
        createServiceBuilderConvention(project);

        JavaPluginConvention javaConvention = (JavaPluginConvention) project
                .getConvention().getPlugins().get("java");

        configureServiceBuilderSourceSets(javaConvention);

        configureBuildServiceTask(project);
    }

    private void createServiceBuilderConvention(Project project) {
        project.getConvention().getPlugins().put("servicebuilder", new LiferayPluginConvention(project));
    }

    private void configureServiceBuilderSourceSets(JavaPluginConvention pluginConvention) {
        Project project = pluginConvention.getProject();

        SourceSetContainer sourceSets = pluginConvention.getSourceSets();

        SourceSet main = sourceSets.add(SERVICE_BUILDER_SOURCE_SET_NAME);
        SourceSet test = sourceSets.add(SERVICE_BUILDER_TEST_SOURCE_SET_NAME);

        test.setCompileClasspath(project.files(main.getOutput(),
                project.getConfigurations().getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME)));
        test.setRuntimeClasspath(project.files(test.getOutput(),
                main.getOutput(), project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_CONFIGURATION_NAME)));
    }

    private void configureBuildServiceTask(Project project) {
        Task buildService = project.getTasks().add(BUILD_SERVICE, BuildService.class);
        buildService.setDescription("Builds a liferay service");
        buildService.setGroup(LiferayBasePlugin.LIFERAY_GROUP);
    }
}
