/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jelmerk;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DirectDeploy extends DefaultTask {

    final Logger logger = LoggerFactory.getLogger(DirectDeploy.class);

    private String appServerType;
    private String pluginType;
    private File appServerDir;
    private FileCollection classPath;
    private File destDir;
    private Boolean customPortletXML;
    private File warFile;

    private void deploy(String className, ClassLoader classLoader, String[] args) throws ReflectiveOperationException {
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
        SecurityManager currentSecurityManager = System.getSecurityManager();

        SecurityManager securityManager = new SecurityManager() {
//            public void checkPermission(Permission permission) {
//            }
            public void checkExit(int status) {
                throw new SecurityException();
            }
        };

        System.setSecurityManager(securityManager);

        try {
            System.setProperty("external-properties", "com/liferay/portal/tools/dependencies/portal-tools.properties");
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
            Class<?> clazz = classLoader.loadClass(className);
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object)args);
        }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof SecurityException) {
            }
            else {
                throw e;
            }
        }
        finally {
            currentThread.setContextClassLoader(contextClassLoader);
            System.setSecurityManager(currentSecurityManager);
        }
      }

    private void deployPortlet() {
        String tldPath = (new File(appServerDir, "webapps/ROOT/WEB-INF/tld")).getAbsolutePath();

        System.setProperty("deployer.aui.taglib.dtd", tldPath + "/aui.tld");
        System.setProperty("deployer.custom.portlet.xml", String.valueOf(customPortletXML));
        System.setProperty("deployer.portlet.taglib.dtd", tldPath + "/liferay-portlet.tld");
        System.setProperty("deployer.portlet-ext.taglib.dtd", tldPath + "/liferay-portlet-ext.tld");
        System.setProperty("deployer.security.taglib.dtd", tldPath + "/liferay-security.tld");
        System.setProperty("deployer.theme.taglib.dtd", tldPath + "/liferay-theme.tld");
        System.setProperty("deployer.ui.taglib.dtd", tldPath + "/liferay-ui.tld");
        System.setProperty("deployer.util.taglib.dtd", tldPath + "/liferay-util.tld");

        String libPath = (new File(appServerDir, "webapps/ROOT/WEB-INF/lib")).getAbsolutePath();

        String[] args = {
                libPath + "/util-bridges.jar", libPath + "/util-java.jar",
                libPath + "/util-taglib.jar"
        };

        ClassLoader classLoader = getClassLoader(classPath.getFiles());
        try {
            deploy("com.liferay.portal.tools.deploy.PortletDeployer", classLoader, args);
        }
        catch (ReflectiveOperationException e)  {
            logger.error("Unable to execute direct deploy portlet.\n {} due to {}.", e.getMessage(), e.getCause());
        }
    }

    private void deployHook() {
        String libPath = (new File(appServerDir, "webapps/ROOT/WEB-INF/lib")).getAbsolutePath();

        String[] args = {libPath + "/util-java.jar"};

        ClassLoader classLoader = getClassLoader(classPath.getFiles());
        try {
            deploy("com.liferay.portal.tools.deploy.HookDeployer", classLoader, args);
        }
        catch (ReflectiveOperationException e)  {
            logger.error("Unable to execute direct deploy hook.\n {} due to {}.", e.getMessage(), e.getCause());
        }
    }

    private void deployTheme() {
        String tldPath = (new File(appServerDir, "webapps/ROOT/WEB-INF/tld")).getAbsolutePath();

        System.setProperty("deployer.theme.taglib.dtd", tldPath + "/liferay-theme.tld");
        System.setProperty("deployer.util.taglib.dtd", tldPath + "/liferay-util.tld");

        String libPath = (new File(appServerDir, "webapps/ROOT/WEB-INF/lib")).getAbsolutePath();

        String[] args = {libPath + "/util-java.jar", libPath + "/util-taglib.jar"};

        ClassLoader classLoader = getClassLoader(classPath.getFiles());
        try {
            deploy("com.liferay.portal.tools.deploy.ThemeDeployer", classLoader, args);
        }
        catch (ReflectiveOperationException e)  {
            logger.error("Unable to execute direct deploy theme.\n {} due to {}.", e.getMessage(), e.getCause());
        }
    }

    private ClassLoader getClassLoader (Set<File> classPath) {

        List<URL> urls = new ArrayList<URL>();

        for (File pathFile : classPath) {
            try {
                URL url= pathFile.toURI().toURL();
                urls.add(url);
            }
            catch (MalformedURLException e) {
                logger.info("Unable to import in ClassLoader {}.", e);
            }
        }

        return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    }

    @TaskAction
    public void directDeploy() throws UnsupportedOperationException {
        Project project = getProject();

        System.setProperty("deployer.app.server.type", appServerType);
        System.setProperty("deployer.base.dir", (new File(project.getBuildDir(), "libs")).getAbsolutePath());
        System.setProperty("deployer.dest.dir", (new File(appServerDir, "webapps")).getAbsolutePath());
        System.setProperty("deployer.file.pattern", warFile.getName());
        System.setProperty("deployer.unpack.war", "true");


        if (pluginType.equals("portlet")) {
            deployPortlet();
        }
        else if (pluginType.equals("hook")) {
            deployHook();
        }
        else if (pluginType.equals("theme")) {
            deployTheme();
        }
        if (pluginType.equals("ext")) {
            throw new UnsupportedOperationException("Ext direct deployment not yet supported.");
        }
        else if (pluginType.equals("layouttpl")) {
             throw new UnsupportedOperationException("Template layout direct deployment not yet supported.");
        }
        else if (pluginType.equals("web")) {
            throw new UnsupportedOperationException("Web direct deployment not yet supported.");
        }
    }

    public String getAppServerType() {
        return appServerType;
    }

    public void setAppServerType(String appServerType) {
        this.appServerType = appServerType;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public File getAppServerDir() {
        return appServerDir;
    }

    public void setAppServerDir(File appServerDir) {
        this.appServerDir = appServerDir;
    }

    public FileCollection getClassPath() {
        return classPath;
    }

    public void setClassPath (FileCollection classPath) {
        this.classPath = classPath;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public Boolean getCustomPortletXML() {
        return customPortletXML;
    }

    public void setCustomPortletXML(Boolean customPortletXML) {
        this.customPortletXML = customPortletXML;
    }

    public File getWarFile() {
        return warFile;
    }

    public void setWarFile(File warFile) {
        this.warFile = warFile;
    }
}