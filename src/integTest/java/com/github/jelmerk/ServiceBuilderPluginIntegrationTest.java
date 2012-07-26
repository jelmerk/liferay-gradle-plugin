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

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Integration test for testing the servicebuilder plugin.
 *
 * @author Jelmer Kuperus
 */
public class ServiceBuilderPluginIntegrationTest extends AbstractPluginIntegrationTest {

    private File projectDir;

    @Before
    public void setup() {
        projectDir = getProjectDir("servicebuilder");
    }

    @Test
    public void testIntegration() throws Exception {

        runBuild(projectDir, "clean", "generateService");

        // check if the api was generated

        File generatedServiceInterfaceFile = new File(projectDir,
                "src/service/java/com/github/jelmerk/servicebuilder/service/BarLocalService.java");

        assertTrue(generatedServiceInterfaceFile.exists());

        // check if the impl was generated

        File generatedServiceImplFile = new File(projectDir,
                "src/main/java/com/github/jelmerk/servicebuilder/service/impl/BarLocalServiceImpl.java");

        assertTrue(generatedServiceImplFile.exists());

        // check that the jalopy file was used

        String generatedServiceImplFileContent = readUtf8TextFile(generatedServiceImplFile);
        assertTrue(generatedServiceImplFileContent
                .contains("Copyright (c) 2012 Jelmer Kuperus All rights reserved."));

    }

}
