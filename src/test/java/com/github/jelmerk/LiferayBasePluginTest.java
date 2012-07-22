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

import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link LiferayBasePlugin}
 *
 * @author Jelmer Kuperus
 */
public class LiferayBasePluginTest {

    Project project;
    LiferayBasePlugin plugin;

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build();
        plugin = new LiferayBasePlugin();
    }

    @Test
    public void testAppliesBasePluginsAndAddsExtensionObject() {
        plugin.apply(project);

        assertTrue(project.getPlugins().hasPlugin(WarPlugin.class));
        assertTrue(project.getExtensions().getByName(LiferayBasePlugin.LIFERAY_EXTENSION_NAME)
                instanceof LiferayPluginExtension);
    }

    @Test
    public void testCreatesDeployTask() {
        plugin.apply(project);

        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);
        assertNotNull(task);
    }

}
