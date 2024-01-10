package com.github.arielcarrera.avro.features;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.github.arielcarrera.build.features.boot.SpringBootBuildFeaturesPlugin;

public class BuildFeaturesBasePluginTest {

    @Test
    public void basicTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(SpringBootBuildFeaturesPlugin.PLUGIN_ID);

        Assertions.assertTrue(project.getPluginManager()
            .hasPlugin(SpringBootBuildFeaturesPlugin.PLUGIN_ID));

        Assertions.assertNotNull(project.getTasks().getByName("runAppConfig"));
        Assertions.assertNotNull(project.getTasks().getByName("stopAppConfig"));
        Assertions.assertNotNull(project.getTasks().getByName("version"));
    }

}
