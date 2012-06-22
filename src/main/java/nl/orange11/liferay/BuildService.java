package nl.orange11.liferay;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class BuildService extends ConventionTask {

    private FileCollection projectClasspath;
    private FileCollection portalClasspath;

    @InputFiles
    private FileCollection sdkClasspath;

    private File appServerGlobalLibDirName;

    private File implSrcDir;
    private File apiSrcDir;
    private File resourceDir;
    private File webappSrcDir;

    private File serviceInputFile;

    @TaskAction
    public void buildService() {

        if (getServiceInputFile() == null) {
            throw new InvalidUserDataException("Please specify a serviceInputFile");
        }

        if (!getServiceInputFile().exists()) {
            throw new InvalidUserDataException("ServiceInputFile " + getServiceInputFile() + " does not exist.");
        }


        Mkdir mkServicebuilderMainSourceSetDir = new Mkdir();
        mkServicebuilderMainSourceSetDir.setDir(getImplSrcDir());
        mkServicebuilderMainSourceSetDir.execute();

        Mkdir mkSqlDir = new Mkdir();
        mkSqlDir.setDir(new File(getWebappSrcDir(), "sql"));
        mkSqlDir.execute();

        Java javaTask = new Java();
        javaTask.setTaskName("service builder");
        javaTask.setClassname("com.liferay.portal.tools.servicebuilder.ServiceBuilder");
        javaTask.setOutputproperty("service.test.output");


        Project antProject = getAnt().getAntProject();

        Path antClassPath = new Path(antProject);


        SimpleFileCollection appserverClasspath = new SimpleFileCollection(
                new File(appServerGlobalLibDirName, "commons-digester.jar"),
                new File(appServerGlobalLibDirName, "commons-lang.jar"),
                new File(appServerGlobalLibDirName, "easyconf.jar")
        );

        UnionFileCollection classPath = new UnionFileCollection();
        classPath.add(getProjectClasspath());
        classPath.add(getSdkClasspath());
        classPath.add(getPortalClasspath());
        classPath.add(appserverClasspath);


        for (File dep : classPath) {
            antClassPath.createPathElement()
                     .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(antClassPath);

        javaTask.createArg()
                .setLine("-Dexternal-properties=com/liferay/portal/tools/dependencies/portal-tools.properties");

        javaTask.createArg()
                .setLine("-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger");

        javaTask.createArg()
                .setLine("service.input.file=" + getServiceInputFile().getPath());

        javaTask.createArg()
                .setLine("service.hbm.file="
                        + new File(getResourceDir(), "META-INF/portlet-hbm.xml").getPath());

        javaTask.createArg()
                .setLine("service.orm.file="
                        + new File(getResourceDir(), "META-INF/portlet-orm.xml").getPath());

        javaTask.createArg()
                .setLine("service.model.hints.file="
                        + new File(getResourceDir(), "META-INF/portlet-model-hints.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.file="
                        + new File(getResourceDir(), "META-INF/portlet-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.base.file="
                        + new File(getResourceDir(), "META-INF/base-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.cluster.file="
                        + new File(getResourceDir(), "META-INF/cluster-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.dynamic.data.source.file="
                        + new File(getResourceDir(), "META-INF/dynamic-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.hibernate.file="
                        + new File(getResourceDir(), "META-INF/hibernate-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.infrastructure.file="
                        + new File(getResourceDir(), "META-INF/infrastructure-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.shard.data.source.file="
                        + new File(getResourceDir(), "META-INF/shard-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.api.dir=" + getApiSrcDir().getPath());

        javaTask.createArg()
                .setLine("service.impl.dir=" + getImplSrcDir().getPath());

        javaTask.createArg()
                .setLine("service.json.file=" + new File(getWebappSrcDir(), "js/service.js").getPath());

        javaTask.createArg()
                .setLine("service.sql.dir=" + new File(getWebappSrcDir(), "WEB-INF/sql").getPath());

        javaTask.createArg()
                .setLine("service.sql.file=tables.sql");

        javaTask.createArg()
                .setLine("service.sql.indexes.file=indexes.sql");

        javaTask.createArg()
                .setLine("service.sql.indexes.properties.file=indexes.properties");

        javaTask.createArg()
                .setLine("service.sql.sequences.file=sequences.sql");

        javaTask.createArg()
                .setLine("service.auto.namespace.tables=true");

        javaTask.createArg()
                .setLine("service.bean.locator.util=com.liferay.util.bean.PortletBeanLocatorUtil");

        javaTask.createArg()
                .setLine("service.props.util=com.liferay.util.service.ServiceProps");

        javaTask.createArg()
                .setLine("service.plugin.name=" + "myplugin"); // TODO: probably should be something like the module name

        javaTask.execute();

        String output = antProject.getProperty("service.test.output"); // does this work?


        System.out.println("***********" + (output == null));
        System.out.println("####" + output);
        if (output != null && output.contains("Error")) {
            throw new TaskExecutionException(this, null);
        }

        /*

        <target name="build-service">
            <mkdir dir="docroot/WEB-INF/classes" />
            <mkdir dir="docroot/WEB-INF/lib" />
            <mkdir dir="docroot/WEB-INF/service" />
            <mkdir dir="docroot/WEB-INF/sql" />
            <mkdir dir="docroot/WEB-INF/src" />

            <copy todir="docroot/WEB-INF/classes">
                <fileset dir="docroot/WEB-INF/src" excludes="** / *.java" />
            </copy>

            <path id="service.classpath">
                <path refid="lib.classpath" />
                <path refid="portal.classpath" />
                <fileset dir="${app.server.lib.portal.dir}" includes="commons-digester.jar,commons-lang.jar,easyconf.jar" />
                <fileset dir="docroot/WEB-INF/lib" includes="*.jar" />
                <pathelement location="docroot/WEB-INF/classes" />
            </path>

            <if>
                <not>
                    <isset property="service.input.file" />
                </not>
                    <then>
                        <property name="service.input.file" value="${basedir}/docroot/WEB-INF/service.xml" />
                </then>
            </if>

            <java
                classname="com.liferay.portal.tools.servicebuilder.ServiceBuilder"
                classpathref="service.classpath"
                outputproperty="service.test.output"
            >
                <arg value="-Dexternal-properties=com/liferay/portal/tools/dependencies/portal-tools.properties" />
                <arg value="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" />
                <arg value="service.input.file=${service.input.file}" />
                <arg value="service.hbm.file=${basedir}/docroot/WEB-INF/src/META-INF/portlet-hbm.xml" />
                <arg value="service.orm.file=${basedir}/docroot/WEB-INF/src/META-INF/portlet-orm.xml" />
                <arg value="service.model.hints.file=${basedir}/docroot/WEB-INF/src/META-INF/portlet-model-hints.xml" />
                <arg value="service.spring.file=${basedir}/docroot/WEB-INF/src/META-INF/portlet-spring.xml" />
                <arg value="service.spring.base.file=${basedir}/docroot/WEB-INF/src/META-INF/base-spring.xml" />
                <arg value="service.spring.cluster.file=${basedir}/docroot/WEB-INF/src/META-INF/cluster-spring.xml" />
                <arg value="service.spring.dynamic.data.source.file=${basedir}/docroot/WEB-INF/src/META-INF/dynamic-data-source-spring.xml" />
                <arg value="service.spring.hibernate.file=${basedir}/docroot/WEB-INF/src/META-INF/hibernate-spring.xml" />
                <arg value="service.spring.infrastructure.file=${basedir}/docroot/WEB-INF/src/META-INF/infrastructure-spring.xml" />
                <arg value="service.spring.shard.data.source.file=${basedir}/docroot/WEB-INF/src/META-INF/shard-data-source-spring.xml" />
                <arg value="service.api.dir=${basedir}/docroot/WEB-INF/service" />
                <arg value="service.impl.dir=${basedir}/docroot/WEB-INF/src" />
                <arg value="service.json.file=${basedir}/docroot/js/service.js" />
                <arg value="service.sql.dir=${basedir}/docroot/WEB-INF/sql" />
                <arg value="service.sql.file=tables.sql" />
                <arg value="service.sql.indexes.file=indexes.sql" />
                <arg value="service.sql.indexes.properties.file=indexes.properties" />
                <arg value="service.sql.sequences.file=sequences.sql" />
                <arg value="service.auto.namespace.tables=true" />
                <arg value="service.bean.locator.util=com.liferay.util.bean.PortletBeanLocatorUtil" />
                <arg value="service.props.util=com.liferay.util.service.ServiceProps" />
                <arg value="service.plugin.name=${plugin.name}" />
            </java>

            <echo>${service.test.output}</echo>

            <if>
                <contains string="${service.test.output}" substring="Error" />
                <then>
                    <fail>Service Builder generated exceptions.</fail>
                </then>
            </if>

            <delete file="ServiceBuilder.temp" />

            <mkdir dir="docroot/WEB-INF/service-classes" />

            <path id="service.classpath">
                <fileset dir="${app.server.lib.global.dir}" includes="*.jar" />
                <fileset dir="${project.dir}/lib" includes="activation.jar,jsp-api.jar,mail.jar,servlet-api.jar" />
                <fileset dir="docroot/WEB-INF/lib" excludes="${plugin.name}-service.jar" includes="*.jar" />
            </path>

            <antcall target="compile-java">
                <param name="javac.classpathref" value="service.classpath" />
                <param name="javac.destdir" value="docroot/WEB-INF/service-classes" />
                <param name="javac.srcdir" value="docroot/WEB-INF/service" />
                <reference refid="service.classpath" torefid="service.classpath" />
            </antcall>

            <zip
                basedir="docroot/WEB-INF/service-classes"
                destfile="docroot/WEB-INF/lib/${plugin.name}-service.jar"
            />

            <delete dir="docroot/WEB-INF/service-classes" />
        </target>

         */

    }

    public FileCollection getPortalClasspath() {
        return portalClasspath;
    }

    public void setPortalClasspath(FileCollection portalClasspath) {
        this.portalClasspath = portalClasspath;
    }

    public FileCollection getSdkClasspath() {
        return sdkClasspath;
    }

    public void setSdkClasspath(FileCollection sdkClasspath) {
        this.sdkClasspath = sdkClasspath;
    }

    public File getServiceInputFile() {
        return serviceInputFile;
    }

    public void setServiceInputFile(File serviceInputFile) {
        this.serviceInputFile = serviceInputFile;
    }

    public File getImplSrcDir() {
        return implSrcDir;
    }

    public void setImplSrcDir(File implSrcDir) {
        this.implSrcDir = implSrcDir;
    }

    public File getApiSrcDir() {
        return apiSrcDir;
    }

    public void setApiSrcDir(File apiSrcDir) {
        this.apiSrcDir = apiSrcDir;
    }

    public File getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    public File getWebappSrcDir() {
        return webappSrcDir;
    }

    public void setWebappSrcDir(File webappSrcDir) {
        this.webappSrcDir = webappSrcDir;
    }

    public File getAppServerGlobalLibDirName() {
        return appServerGlobalLibDirName;
    }

    public void setAppServerGlobalLibDirName(File appServerGlobalLibDirName) {
        this.appServerGlobalLibDirName = appServerGlobalLibDirName;
    }

    public FileCollection getProjectClasspath() {
        return projectClasspath;
    }

    public void setProjectClasspath(FileCollection projectClasspath) {
        this.projectClasspath = projectClasspath;
    }
}
