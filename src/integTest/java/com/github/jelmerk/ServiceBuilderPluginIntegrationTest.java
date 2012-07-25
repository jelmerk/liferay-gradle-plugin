package com.github.jelmerk;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

/**
 * Integration test for testing the servicebuilder plugin.
 *
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginIntegrationTest {

    private File projectDir;

    @Before
    public void setup() {
        projectDir = new File(System.getProperty("integTestProjectsDir"), "servicebuilder");
    }

    @Test
    public void testIntegration() throws Exception {
        DefaultGradleConnector connector = (DefaultGradleConnector) GradleConnector.newConnector();

        ProjectConnection connection = connector
                .embedded(true)
                .forProjectDirectory(projectDir)
                .connect();

        try {
            BuildLauncher build = connection.newBuild();
            build.forTasks("clean", "generateService");
            build.run();

            // check if the api was generated

            File generatedServiceInterfaceFile = new File(projectDir,
                    "src/service/java/com/github/jelmerk/servicebuilder/service/BarLocalService.java");

            assertTrue(generatedServiceInterfaceFile.exists());


            // check if the impl was generated
            File generatedServiceImplFile = new File(projectDir,
                    "src/main/java/com/github/jelmerk/servicebuilder/service/impl/BarLocalServiceImpl.java");

            assertTrue(generatedServiceImplFile.exists());

            // check that the jalopy file was used

            String generatedServiceImplFileContent = Files.toString(generatedServiceImplFile, Charsets.UTF_8);
            assertTrue(generatedServiceImplFileContent
                    .contains("Copyright (c) 2012 Jelmer Kuperus All rights reserved."));

        } finally {
            connection.close();
        }

    }

}
