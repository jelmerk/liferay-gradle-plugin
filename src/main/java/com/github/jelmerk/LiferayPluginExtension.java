package com.github.jelmerk;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Extension that holds Liferay specific paths.
 *
 * @author Jelmer Kuperus
 */
public class LiferayPluginExtension {

    private final Project project;

    private String appServerDirName;
    private String appServerGlobalLibDirName;
    private String appServerPortalDirName;

    private String autoDeployDirName;

    /**
     * Constructs a new LiferayPluginExtension.
     *
     * @param project the project this extension is registered under
     */
    public LiferayPluginExtension(Project project) {
        this.project = project;
    }

    /**
     * Returns the path to the root folder of the application server that Liferay is running in.
     *
     * @return the path to the root folder of the application server that Liferay is running in
     */
    public String getAppServerDirName() {
        return appServerDirName;
    }

    /**
     * Sets the path to the root folder of the application server that Liferay is running in.
     * This property is required to be set.
     *
     * @param appServerDirName the path to the root folder of the application server that Liferay is running in.
     */
    public void setAppServerDirName(String appServerDirName) {
        this.appServerDirName = appServerDirName;
    }

    /**
     * Return the path to the folder that holds the Liferay libraries that are on the global classpath.
     *
     * @return the path to the folder that holds the Liferay libraries that are on the global classpath
     */
    public String getAppServerGlobalLibDirName() {
        if (appServerGlobalLibDirName != null) {
            return appServerGlobalLibDirName;
        }

        File appServerPortalDir = new File(getAppServerDirName(), "lib/ext");
        return appServerPortalDir.getPath();
    }

    /**
     * Set the path to the folder that holds the Liferay libraries that are on the global classpath.
     * If unset this value defaults to $appServerDirName/lib/ext
     *
     * @param appServerGlobalLibDirName the path to the folder that holds the Liferay libraries that are on
     *                                  the global classpath
     */
    public void setAppServerGlobalLibDirName(String appServerGlobalLibDirName) {
        this.appServerGlobalLibDirName = appServerGlobalLibDirName;
    }

    /**
     * Returns the path to the exploded Liferay web application.
     *
     * @return the path to the exploded Liferay web application
     */
    public String getAppServerPortalDirName() {
        if (appServerPortalDirName != null) {
            return appServerPortalDirName;
        }

        File appServerPortalDir = new File(getAppServerDirName(), "webapps/ROOT");
        return appServerPortalDir.getPath();
    }

    /**
     * Sets the path to the exploded Liferay web application.
     * If unset this value defaults to $appServerDirName/webapps/ROOT
     *
     * @param appServerPortalDirName the path to the exploded Liferay web application
     */
    public void setAppServerPortalDirName(String appServerPortalDirName) {
        this.appServerPortalDirName = appServerPortalDirName;
    }

    /**
     * Returns the path to the Liferay auto deploy folder. Plugins placed in this folder will automatically be deployed.
     *
     * @return the path to the Liferay auto deploy folder
     */
    public String getAutoDeployDirName() {
        if (autoDeployDirName != null) {
            return autoDeployDirName;
        }

        File deployDir = new File(getAppServerDirName(), "../deploy");
        return deployDir.getPath();
    }

    /**
     * Sets the path to the Liferay auto deploy folder. Plugins placed in this folder will automatically be deployed.
     * If unset this value defaults to $appServerDirName/../deploy
     *
     * @param autoDeployDirName the path to the Liferay auto deploy folder
     */
    public void setAutoDeployDirName(String autoDeployDirName) {
        this.autoDeployDirName = autoDeployDirName;
    }

    /**
     * Returns a file pointing to the root folder of the application server that Liferay is running in.
     *
     * @return a file pointing to the root folder of the application server that Liferay is running in
     */
    public File getAppServerDir() {
        return project.file(getAppServerDirName());
    }

    /**
     * Returns a file pointing to the folder that holds the Liferay libraries that are on the global classpath.
     *
     * @return a file pointing to the folder that holds the Liferay libraries that are on the global classpath
     */
    public File getAppServerGlobalLibDir() {
        return project.file(getAppServerGlobalLibDirName());
    }

    /**
     * Returns a file pointing to the exploded Liferay web application.
     *
     * @return a file pointing to the exploded Liferay web application
     */
    public File getAppServerPortalDir() {
        return project.file(getAppServerPortalDirName());
    }

    /**
     * Returns a file pointing to the auto deploy folder. Plugins placed in this folder will automatically be deployed.
     *
     * @return a file pointing to the auto deploy folder. Plugins placed in this folder will automatically be deployed
     */
    public File getAutoDeployDir() {
        return project.file(getAutoDeployDirName());
    }

    /**
     * Returns a file collection that holds all classes on the portal web application's classpath. It includes classes
     * that are not available to plugins.
     *
     * @return a file collection that holds all classes on the portal web application's classpath
     */
    public FileCollection getPortalClasspath() {
        File portalClassesDir = new File(getAppServerPortalDir(), "WEB-INF/classes");
        File portalLibDir = new File(getAppServerPortalDir(), "WEB-INF/lib");

        Set<File> pluginClasspath = project.getBuildscript().getConfigurations()
                .getByName(ScriptHandler.CLASSPATH_CONFIGURATION).resolve();


        List<File> classPath = new ArrayList<File>();
        classPath.add(portalClassesDir);
        classPath.addAll(asList(portalLibDir.listFiles(JarFilenameFilter.getInstance())));
        classPath.addAll(asList(getAppServerGlobalLibDir().listFiles(JarFilenameFilter.getInstance())));
        classPath.addAll(pluginClasspath);

        return project.files(classPath);
    }

    /**
     * Configures this class from a groovy closure.
     *
     * @param closure the closure that configures this class
     */
    public void liferay(Closure closure) {
        ConfigureUtil.configure(closure, this);
    }

    static class JarFilenameFilter implements FilenameFilter {

        private static volatile JarFilenameFilter instance;

        @Override
        public boolean accept(File dir, String name) {
            return name != null && name.toLowerCase(Locale.getDefault()).endsWith(".jar");
        }

        public static JarFilenameFilter getInstance() {
            if (instance == null) {
                synchronized (JarFilenameFilter.class) {
                    //  See: http://sourceforge.net/tracker/?func=detail&atid=397078&aid=2843447&group_id=29721
                    if (instance == null) { // NOSONAR
                        instance = new JarFilenameFilter();
                    }
                }
            }
            return instance;
        }
    }


}
