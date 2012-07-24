package com.github.jelmerk;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.junit.Test;

import java.io.File;

/**
 * @author Jelmer Kuperus
 */
public class MyIntegrationTest {

    @Test
    public void testIntegration() {

//        DefaultGradleConnector connector = (DefaultGradleConnector) GradleConnector.newConnector();
//
//        ProjectConnection connection = connector
//                .embedded(true)
//                .forProjectDirectory(new File("/Users/jkuperus/Development/Projects/liferay-gradle-plugin-samples/complex-mixed"))
//                .connect();
//
//        try {
//            BuildLauncher build = connection.newBuild();
//
//            //select tasks to run:
//            build.forTasks("clean", "test");
//
////            ProgressListener listener = null; // use your implementation
////            build.addProgressListener(listener);
//
//
//            build.addProgressListener(new ProgressListener() {
//                @Override
//                public void statusChanged(ProgressEvent event) {
//                    System.out.println("##########" + event.getDescription());
//                }
//            });
//
//            //kick the build off:
//            build.run();
//        } finally {
//            connection.close();
//        }

    }

}
