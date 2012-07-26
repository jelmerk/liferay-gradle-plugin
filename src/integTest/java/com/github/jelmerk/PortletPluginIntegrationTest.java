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

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for testing the portlet plugin.
 *
 * @author Jelmer Kuperus
 */
public class PortletPluginIntegrationTest extends AbstractPluginIntegrationTest {

    private File projectDir;

    @Before
    public void setup() {
        projectDir = getProjectDir("portlet");
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
            build.forTasks("clean", "war");
            build.run();

            File createdWarFile = new File(projectDir, "build/libs/portlet.war");

            assertTrue(createdWarFile.exists());
            assertTrue(hasZipEntry(createdWarFile, "index.jsp"));
            assertTrue(hasZipEntry(createdWarFile, "css/.sass-cache/main.css"));

        } finally {
            connection.close();
        }

    }

}
