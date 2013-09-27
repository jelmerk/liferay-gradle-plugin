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
import org.gradle.api.tasks.bundling.War;
import org.gradle.invocation.DefaultGradle;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link LiferayBasePlugin}
 *
 * @author Jelmer Kuperus
 */
public class LiferayBasePluginTest {

    Project project;
    DefaultGradle gradle;
    LiferayBasePlugin plugin;

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build();
        gradle = (DefaultGradle) project.getGradle();
        plugin = new LiferayBasePlugin();
        project.getExtensions().getByType(LiferayPluginExtension.class).setAppServerDirName("liferay_extension_dir");
    }

    @Test
    public void testAppliesBasePlugins() {
        plugin.apply(project);
        assertTrue(project.getPlugins().hasPlugin(WarPlugin.class));
    }

    @Test
    public void testAddedExtension() {
        plugin.apply(project);
        assertTrue(project.getExtensions().getByName(LiferayBasePlugin.LIFERAY_EXTENSION_NAME)
                instanceof LiferayPluginExtension);
    }

    @Test
    public void testCreatedDeployTask() {
        plugin.apply(project);
        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);
        assertNotNull(task);
        assertEquals(LiferayBasePlugin.LIFERAY_GROUP_NAME, task.getGroup());
        assertTrue(task.getDependsOn().contains(project.getTasks().getByName(WarPlugin.WAR_TASK_NAME)));
    }

    @Test
    public void testDeployTaskAutoDeployDirDefault() {
        plugin.apply(project);

        LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);
        liferayExtension.setAutoDeployDirName("liferay_extension_dir");

        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);
        gradle.getBuildListenerBroadcaster().projectsEvaluated(gradle);

        assertEquals(liferayExtension.getAutoDeployDir(), task.getAutoDeployDir());
    }

    @Test
    public void testDeployTaskAutoDeployDirOverride() {
        File override = new File("override");

        plugin.apply(project);

        LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);
        liferayExtension.setAutoDeployDirName("liferay_extension_dir");

        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);
        task.setAutoDeployDir(override);

        gradle.getBuildListenerBroadcaster().projectsEvaluated(gradle);

        assertEquals(override, task.getAutoDeployDir());
    }

    @Test
    public void testWarFileDefault() {
        plugin.apply(project);

        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);

        gradle.getBuildListenerBroadcaster().projectsEvaluated(gradle);

        assertEquals(warTask.getArchivePath(), task.getWarFile());
    }

    @Test
    public void testWarFileOverride() {
        File override = new File("override");

        plugin.apply(project);

        Deploy task = (Deploy) project.getTasks().getByName(LiferayBasePlugin.DEPLOY_TASK_NAME);
        task.setWarFile(override);

        gradle.getBuildListenerBroadcaster().projectsEvaluated(gradle);

        assertEquals(override, task.getWarFile());
    }
}
