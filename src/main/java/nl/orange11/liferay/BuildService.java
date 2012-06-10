package nl.orange11.liferay;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.Path;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class BuildService extends AbstractTask {

    private Project project;

    private FileCollection portalClasspath;

    private File serviceInputFile;

    public BuildService(Project project) {
        this.project = project;
    }

    @TaskAction
    public void buildService() {

        if (getServiceInputFile() == null) {
            throw new InvalidUserDataException("Please specify a serviceInputFile");
        }

        if (!getServiceInputFile().exists()) {
            throw new InvalidUserDataException("ServiceInputFile " + serviceInputFile + " does not exist.");
        }

        WarPluginConvention warConvention = getProject().getConvention().getPlugin(WarPluginConvention.class);
        JavaPluginConvention javaConvention = getProject().getConvention().getPlugin(JavaPluginConvention.class);


        SourceSetContainer sourceSets = javaConvention.getSourceSets();
        SourceSet sourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        SourceDirectorySet allJava = sourceSet.getAllJava();
        SourceDirectorySet resources = sourceSet.getResources();

        File resourcesDir = resources.getSingleFile();
        File mainSourceSetDir = allJava.getSingleFile();
        File servicebuilderMainSourceSetDir = new File("todo"); // TODO: figure out how to get this
        File webappSourceDir = warConvention.getWebAppDir();

        Mkdir mkServicebuilderMainSourceSetDir = new Mkdir();
        mkServicebuilderMainSourceSetDir.setDir(servicebuilderMainSourceSetDir);
        mkServicebuilderMainSourceSetDir.execute();

        Mkdir mkSqlDir = new Mkdir();
        mkSqlDir.setDir(new File(webappSourceDir, "sql"));
        mkSqlDir.execute();


        Java javaTask = new Java();
        javaTask.setTaskName("service builder");
        javaTask.setClassname("com.liferay.portal.tools.servicebuilder.ServiceBuilder");
        javaTask.setOutputproperty("service.test.output");


        Project antProject = getAnt().getAntProject();

        Path classPath = new Path(antProject);

        for (File dep : getPortalClasspath()) {
            classPath.createPathElement()
                     .setLocation(dep);
        }

        javaTask.setProject(antProject);
        javaTask.setClasspath(classPath);

        javaTask.createArg()
                .setLine("-Dexternal-properties=com/liferay/portal/tools/dependencies/portal-tools.properties");

        javaTask.createArg()
                .setLine("-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger");

        javaTask.createArg()
                .setLine("service.input.file=" + getServiceInputFile().getPath());

        javaTask.createArg()
                .setLine("service.hbm.file="
                        + new File(resourcesDir, "META-INF/portlet-hbm.xml").getPath());

        javaTask.createArg()
                .setLine("service.orm.file="
                        + new File(resourcesDir, "META-INF/portlet-orm.xml").getPath());

        javaTask.createArg()
                .setLine("service.model.hints.file="
                        + new File(resourcesDir, "META-INF/portlet-model-hints.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.file="
                        + new File(resourcesDir, "META-INF/portlet-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.base.file="
                        + new File(resourcesDir, "META-INF/base-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.cluster.file="
                        + new File(resourcesDir, "META-INF/cluster-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.dynamic.data.source.file="
                        + new File(resourcesDir, "META-INF/dynamic-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.hibernate.file="
                        + new File(resourcesDir, "META-INF/hibernate-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.infrastructure.file="
                        + new File(resourcesDir, "META-INF/infrastructure-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.spring.shard.data.source.file="
                        + new File(resourcesDir, "META-INF/shard-data-source-spring.xml").getPath());

        javaTask.createArg()
                .setLine("service.api.dir=" + servicebuilderMainSourceSetDir.getPath());

        javaTask.createArg()
                .setLine("service.impl.dir=" + mainSourceSetDir.getPath());

        javaTask.createArg()
                .setLine("service.json.file=" + new File(webappSourceDir, "js/service.js").getPath());

        javaTask.createArg()
                .setLine("service.sql.dir=" + new File(webappSourceDir, "WEB-INF/sql").getPath());

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
                .setLine("service.plugin.name=" + project.getName());

        javaTask.execute();

        String output = antProject.getProperty("service.test.output"); // does this work?

        if (output.contains("Error")) {
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

    public File getServiceInputFile() {
        return serviceInputFile;
    }

    public void setServiceInputFile(File serviceInputFile) {
        this.serviceInputFile = serviceInputFile;
    }
}
