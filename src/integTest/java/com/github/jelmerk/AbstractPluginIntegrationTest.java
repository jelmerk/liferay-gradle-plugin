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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertTrue;

/**
 * Abstract base class for integration tests.
 *
 * @author Jelmer Kuperus
 */
public abstract class AbstractPluginIntegrationTest {

    private static final String PROPJECCT_DIR_SYSTEM_PROPERTY_NAME = "integTestProjectsDir";

    protected File getProjectDir(String projectName) {
        return new File(System.getProperty(PROPJECCT_DIR_SYSTEM_PROPERTY_NAME), projectName);
    }

    protected void runBuild(File projectDir, String... targets) {
        DefaultGradleConnector connector = (DefaultGradleConnector) GradleConnector.newConnector();

        ProjectConnection connection = connector
                .embedded(true)
                .forProjectDirectory(projectDir)
                .connect();
        try {
            BuildLauncher build = connection.newBuild();
            build.forTasks(targets);
            build.run();

        } finally {
            connection.close();
        }
    }

    protected String readUtf8TextFile(File file) throws IOException {
        return Files.toString(file, Charsets.UTF_8);
    }

    protected boolean hasZipEntry(File file, String path) throws IOException {
        ZipInputStream in = new ZipInputStream(new FileInputStream(file));

        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (path.equals(entry.getName())) {
                return true;
            }
        }
        return false;
    }

    protected String readZipEntryToUtf8String(File file, String path) throws IOException {
        ZipInputStream in = new ZipInputStream(new FileInputStream(file));

        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            if (path.equals(entry.getName())) {
                return readZipEntryToUtf8String(in);
            }
        }

        return null;
    }

    private String readZipEntryToUtf8String(ZipInputStream in) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int data = 0;
        while( ( data = in.read() ) != - 1 )
        {
            output.write( data );
        }

        // The ZipEntry is extracted in the output
        output.close();

        return new String(output.toByteArray(),"UTF-8");
    }


    protected void assertThatHasZipEntry(File file, String path) throws IOException {
        assertTrue(String.format("The entry '%s' does not exixts in the file",path),hasZipEntry(file, path));
    }
}
