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

import static org.junit.Assert.assertTrue;

/**
 * @author Alessandro Palumbo
 */
public class ThemePluginTest {

    Project project;
    ThemePlugin plugin;

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build();
        plugin = new ThemePlugin();
    }

    @Test
    public void testAppliedBasePlugins() {
        plugin.apply(project);

        assertTrue(project.getPlugins().hasPlugin(LiferayBasePlugin.class));
        assertTrue(project.getPlugins().hasPlugin(WarPlugin.class));
    }

    @Test
    public void testAddedExtension() {
        plugin.apply(project);
        assertTrue(project.getExtensions().getByName(ThemePlugin.THEME_EXTENSION_NAME)
                instanceof ThemePluginExtension);
    }

}
