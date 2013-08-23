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

import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for testing the theme plugin.
 *
 * @author Jelmer Kuperus
 */
public class ThemePluginIntegrationTest extends AbstractPluginIntegrationTest {

    private File projectDir;

    @Before
    public void setup() {
        projectDir = getProjectDir("theme");
    }

    @Test
    public void testIntegration() throws Exception {

        runBuild(projectDir, "clean", "war");

        File createdWarFile = new File(projectDir, "build/libs/theme.war");

        assertTrue(createdWarFile.exists());
        assertThatHasZipEntry(createdWarFile, "WEB-INF/liferay-look-and-feel.xml");
        assertThatHasZipEntry(createdWarFile, "templates/navigation.vm");
        assertThatHasZipEntry(createdWarFile, "templates/init_custom.vm");
        assertThatHasZipEntry(createdWarFile, "templates/portal_normal.vm");
        assertThatHasZipEntry(createdWarFile, "css/.sass-cache/main.css");

        final String portalNormalArchiveContent = readZipEntryToUtf8String(createdWarFile, "templates/portal_normal.vm");

        Assert.assertEquals(portalNormalArchiveContent,"#If this line exists merge the parent theme is merged in the right way",
                "Theme local files are not preserved in merge!");
    }

}
