package nl.orange11.liferay;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.plugins.*;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.String.format;

/**
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPlugin implements Plugin<Project> {

    public static final String BUILD_SERVICE = "generateService";

    public static final String SERVICE_JAVADOC = "serviceJavadoc";

    public static final String JAR_SERVICE = "jarService";

    public static final String SERVICE_BUILDER_CONFIGURATION_NAME = "serviceBuilder";

    public static final String SERVICE_SOURCE_SET_NAME = "service";

    public static final String SERVICE_CONFIGURATION_NAME = "service";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        // configureJavadoc(project);

        configureConfigurations(project);
        createServiceBuilderExtension(project);
        configureSourceSets(project);
        configureArchives(project);

        configureTaskRule(project);

        configureServiceJavadocTask(project);
        configureBuildServiceTask(project);
    }

//    private void configureJavadoc(Project project) {
//
//        // annoyingly a circular dependency is created if you depend on an artifact of a configuration in your own
//        // project. This is done in the configureJavaDoc method of the JavaPlugin. There addDependsOnTaskInOtherProjects
//        // is called to ensure that we always get a circular dependency.. annoying
//        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
//
//        Task javadocTask = project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
//        project.getTasks().remove(javadocTask);
//
//
//        SourceSet mainSourceSet = pluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
//        Javadoc javadoc = project.getTasks().add(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);
//        javadoc.setDescription("Generates Javadoc API documentation for the main source code.");
//        javadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
//        javadoc.setClasspath(mainSourceSet.getOutput().plus(mainSourceSet.getCompileClasspath()));
//        javadoc.setSource(mainSourceSet.getAllJava());
//
//
//        final Configuration configuration = project.getConfigurations().getByName(configurationName);
//
//        javadoc.dependsOn(configuration.getTaskDependencyFromProjectDependency(useDependedOn, otherProjectTaskName));
//
//        addDependsOnTaskInOtherProjects(javadoc, true, JavaPlugin.JAVADOC_TASK_NAME,
//                JavaPlugin.COMPILE_CONFIGURATION_NAME);
//
//
//        // is a TaskDependency
//
////        Configuration compileConfiguration = project.getConfigurations()
////                .getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME);
////
////        Set<Object> dependsOn = javadocTask.getDependsOn();
////
////        for (Iterator<Object> iterator = dependsOn.iterator(); iterator.hasNext(); ) {
////            Object dependency = iterator.next();
////
////            if (compileConfiguration == dependency) {
////                iterator.remove();
////            }
////        }
////
////        System.out.println("DO WE EVEN GET HERE ?" +  dependsOn);
//
//    }

    private void configureConfigurations(Project project) {

        Configuration serviceBuilderConfiguration = project.getConfigurations().add(SERVICE_BUILDER_CONFIGURATION_NAME);
        serviceBuilderConfiguration.setVisible(false);
        serviceBuilderConfiguration.setDescription("The servicebuilder configuration");

        Configuration serviceConfiguration = project.getConfigurations().add(SERVICE_CONFIGURATION_NAME);
        serviceConfiguration.setDescription("The service configuration");
    }

    private void createServiceBuilderExtension(Project project) {
        project.getExtensions().create("servicebuilder", ServiceBuilderPluginExtension.class, project);
    }

    private void configureSourceSets(Project project) {
        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        pluginConvention.getSourceSets().add(SERVICE_SOURCE_SET_NAME);
    }

    private void configureArchives(Project project) {

        JavaPluginConvention pluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        Jar jar = project.getTasks().add(JAR_SERVICE, Jar.class);

        jar.setDescription("Assembles a jar archive containing the servicebuilder classes.");
        jar.setGroup(BasePlugin.BUILD_GROUP);
        jar.from(pluginConvention.getSourceSets().getByName(SERVICE_SOURCE_SET_NAME).getOutput());
        jar.setArchiveName(format("%s-service.jar", project.getName()));

        project.getArtifacts().add(SERVICE_CONFIGURATION_NAME, project.getTasks().getByName(JAR_SERVICE));
    }

    private void configureBuildServiceTask(Project project) {
        BuildService buildService = project.getTasks().add(BUILD_SERVICE, BuildService.class);
        buildService.setDescription("Builds a liferay service");
        buildService.setGroup(LiferayBasePlugin.LIFERAY_GROUP);

    }

    private void configureServiceJavadocTask(Project project) {

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

//        ReportingExtension reportingExtension = project.getExtensions().findByType(ReportingExtension.class);



        Javadoc javadoc =  (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);

        /*
			<doclet name="com.liferay.tools.doclets.standard.Standard" path="${project.dir}/lib/development/liferay-doclet.jar">
				<param name="-linksource" />
			</doclet>

         */

        SourceSetContainer sourceSets = javaConvention.getSourceSets();

        SourceSet servicebuilderSourceSet = sourceSets.getByName(SERVICE_SOURCE_SET_NAME);


        UnionFileTree allSources = new UnionFileTree();
        allSources.add(javadoc.getSource());
        allSources.add(servicebuilderSourceSet.getAllJava());

        javadoc.setSource(allSources);

//
//        SourceSet servicebuilderSourceSet = sourceSets.getByName(SERVICE_SOURCE_SET_NAME);
//
//        Javadoc javadoc = project.getTasks().add(SERVICE_JAVADOC, Javadoc.class);
//        javadoc.setDescription("Generates Javadoc API documentation for the service source code.");
//        javadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
//        javadoc.setClasspath(servicebuilderSourceSet.getOutput().plus(servicebuilderSourceSet.getCompileClasspath()));
//        javadoc.setSource(servicebuilderSourceSet.getAllJava());
//        javadoc.setDestinationDir(reportingExtension.file("service-api-docs"));


        //reporting.file("rest-api-docs")
    }

    private void configureTaskRule(final Project project) {
        project.getTasks().withType(BuildService.class, new Action<BuildService>() {
            @Override
            public void execute(BuildService buildService) {
                configureTask(project, buildService);
            }
        });
    }

    protected void configureTask(final Project project, final BuildService buildService) {

        final LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);

        final ServiceBuilderPluginExtension serviceBuilderExtension = project.getExtensions()
                .getByType(ServiceBuilderPluginExtension.class);

        final WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);

        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        SourceSetContainer sourceSets = javaConvention.getSourceSets();
        SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet servicebuilderSourceSet = sourceSets.getByName(SERVICE_SOURCE_SET_NAME);

        final SourceDirectorySet allMainJava = mainSourceSet.getAllJava();
        final SourceDirectorySet allServiceBuilderJava = servicebuilderSourceSet.getAllJava();
        final SourceDirectorySet allResources = mainSourceSet.getResources();

        buildService.getConventionMapping().map("pluginName", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return project.getName();
            }
        });


        buildService.getConventionMapping().map("serviceInputFile", new Callable<File>() {
            @Override
            public File call() throws Exception {
                if (serviceBuilderExtension.getServiceInputFileName() != null) {
                    return serviceBuilderExtension.getServiceInputFile();
                }
                return new File(warConvention.getWebAppDir(), "WEB-INF/service.xml");
            }
        });

        buildService.getConventionMapping().map("jalopyInputFile", new Callable<File>() {
            @Override
            public File call() throws Exception {
                if (serviceBuilderExtension.getJalopyInputFileName() != null) {
                    return serviceBuilderExtension.getJalopyInputFile();
                }
                return null;
            }
        });

        buildService.getConventionMapping().map("implSrcDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return allMainJava.getSrcDirs().iterator().next();
            }
        });

        buildService.getConventionMapping().map("apiSrcDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return allServiceBuilderJava.getSrcDirs().iterator().next();
            }
        });

        buildService.getConventionMapping().map("resourceDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return allResources.getSrcDirs().iterator().next();
            }
        });

        buildService.getConventionMapping().map("webappSrcDir", new Callable<File>() {
            @Override
            public File call() throws Exception {
                return warConvention.getWebAppDir();
            }
        });

        buildService.getConventionMapping().map("classpath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() throws Exception {

                Configuration servicebuilderConfiguration = project.getConfigurations()
                        .getByName(SERVICE_BUILDER_CONFIGURATION_NAME);

                if (servicebuilderConfiguration.getDependencies().isEmpty()) {

                    // the sdk dependencies : we will need to download those

                    DependencyHandler projectDependencies = project.getDependencies();

                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "com.thoughtworks.qdox:qdox:1.12");
                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "jalopy:jalopy:1.5rc3");
                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.servlet:servlet-api:2.5");
                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.servlet.jsp:jsp-api:2.1");
                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, "javax.activation:activation:1.1");

                    //  the portal classpath dependencies : we have those locally

                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, liferayExtension.getPortalClasspath());

                    // the common classpath dependencies : we can get from the portal

                    File appServerGlobalLibDirName = liferayExtension.getAppServerGlobalLibDir();

                    SimpleFileCollection appserverClasspath = new SimpleFileCollection(
                            new File(appServerGlobalLibDirName, "commons-digester.jar"),
                            new File(appServerGlobalLibDirName, "commons-lang.jar"),
                            new File(appServerGlobalLibDirName, "easyconf.jar")
                    );

                    projectDependencies.add(SERVICE_BUILDER_CONFIGURATION_NAME, appserverClasspath);

                }

                return servicebuilderConfiguration;
            }
        });


    }
}
