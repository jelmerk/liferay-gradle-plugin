package com.github.jelmerk;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Jelmer Kuperus
 */
public class HookPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);
    }
}
