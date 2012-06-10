package nl.orange11.liferay;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;

import java.util.concurrent.Callable;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPlugin implements Plugin<Project> {

    public static final String BUILD_SERVICE = "build-service";

    public static final String JAR_SERVICE = "jar-service";

    public static final String SERVICE_BUILDER_SOURCE_SET_NAME = "servicebuilder";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        createServiceBuilderExtension(project);
        configureSourceSets(project);
        configureArchives(project);

        configureBuildServiceTask(project);
    }

    private void createServiceBuilderExtension(Project project) {
        project.getExtensions().create("servicebuilder", ServiceBuilderPluginExtension.class, project);
    }

    private void configureSourceSets(Project project) {

        JavaPluginConvention pluginConvention = (JavaPluginConvention) project
                .getConvention().getPlugins().get("java");

        SourceSetContainer sourceSets = pluginConvention.getSourceSets();
        SourceSet main = sourceSets.add(SERVICE_BUILDER_SOURCE_SET_NAME);
    }


    private void configureArchives(Project project) {

        final ServiceBuilderPluginExtension servicebuilderExtension = project.getExtensions()
                .getByType(ServiceBuilderPluginExtension.class);

        project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(JavaPlugin.TEST_TASK_NAME);

        Jar jar = project.getTasks().add(JAR_SERVICE, Jar.class);

        jar.setDescription("Assembles a jar archive containing the servicebuilder classes.");
        jar.setGroup(BasePlugin.BUILD_GROUP);
        jar.from(servicebuilderExtension.getSourceSets().getByName(SERVICE_BUILDER_SOURCE_SET_NAME).getOutput());

        project.getExtensions().getByType(DefaultArtifactPublicationSet.class)
                .addCandidate(new ArchivePublishArtifact(jar));

        project.getConfigurations().getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME).getArtifacts()
                .add(new ArchivePublishArtifact(jar));
    }

    private void configureBuildServiceTask(Project project) {
        Task buildService = project.getTasks().add(BUILD_SERVICE, BuildService.class);
        buildService.setDescription("Builds a liferay service");
        buildService.setGroup(LiferayBasePlugin.LIFERAY_GROUP);
    }
}
