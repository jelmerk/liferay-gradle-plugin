package com.github.jelmerk;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Jelmer Kuperus
 */
public class LayoutPlugin implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LiferayBasePlugin.class);

        // the layout ant file also invokes the merge target but only when original.war.file is set
        // basically it will unzip the contents of the war file to a tmp folder and copies the templates on top
        // should we support this ?
    }
}
