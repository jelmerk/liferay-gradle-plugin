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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Implementation of {@link Plugin} that adds tasks and configuration for creating Liferay portlets.
 * When you configure this plugin {@link LiferayBasePlugin} and  {@link SassCompilationPluginDelegate} are configured as well.
 *
 * @author Jelmer Kuperus
 */
public class PortletPlugin implements Plugin<Project> {

    /**
     * The name of the task that processes Syntactically Awesome StyleSheets (SASS) files.
     */
    public static final String SASS_TO_CSS_TASK_NAME =  SassCompilationPluginDelegate.SASS_TO_CSS_TASK_NAME;

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        LiferayPluginExtension liferayExtension = project.getExtensions().getByType(LiferayPluginExtension.class);
        liferayExtension.setPluginType("portlet");

        SassCompilationPluginDelegate sassCompilationPluginDelegate = new SassCompilationPluginDelegate();
        sassCompilationPluginDelegate.doApply(project);
    }

}
