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
 * @author Jelmer Kuperus
 */
public class LiferayPluginExtension {

    private final Project project;

    private String appServerDirName;
    private String appServerGlobalLibDirName;
    private String appServerPortalDirName;

    private String autoDeployDirName;

    public LiferayPluginExtension(Project project) {
        this.project = project;
    }

    public String getAppServerDirName() {
        return appServerDirName;
    }

    public void setAppServerDirName(String appServerDirName) {
        this.appServerDirName = appServerDirName;
    }

    public String getAppServerGlobalLibDirName() {
        if (appServerGlobalLibDirName != null) {
            return appServerGlobalLibDirName;
        }

        File appServerPortalDir = new File(getAppServerDirName(), "lib/ext");
        return appServerPortalDir.getPath();
    }

    public void setAppServerGlobalLibDirName(String appServerGlobalLibDirName) {
        this.appServerGlobalLibDirName = appServerGlobalLibDirName;
    }

    public String getAppServerPortalDirName() {
        if (appServerPortalDirName != null) {
            return appServerPortalDirName;
        }

        File appServerPortalDir = new File(getAppServerDirName(), "webapps/ROOT");
        return appServerPortalDir.getPath();
    }

    public void setAppServerPortalDirName(String appServerPortalDirName) {
        this.appServerPortalDirName = appServerPortalDirName;
    }

    public String getAutoDeployDirName() {
        if (autoDeployDirName != null) {
            return autoDeployDirName;
        }

        File deployDir = new File(getAppServerDirName(), "../deploy");
        return deployDir.getPath();
    }

    public void setAutoDeployDirName(String autoDeployDirName) {
        this.autoDeployDirName = autoDeployDirName;
    }

    public File getAppServerDir() {
        return project.file(getAppServerDirName());
    }

    public File getAppServerGlobalLibDir() {
        return project.file(getAppServerGlobalLibDirName());
    }

    public File getAppServerPortalDir() {
        return project.file(getAppServerPortalDirName());
    }

    public File getAutoDeployDir() {
        return project.file(getAutoDeployDirName());
    }

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
