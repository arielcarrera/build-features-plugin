package io.github.arielcarrera.build.features.boot.dsl;

import org.gradle.api.Project;
import io.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import io.github.arielcarrera.build.features.dsl.BaseBuildFeaturesExtension;

/**
 * Main class that implements the SpringBoot Build Features Plugin.
 *
 * @author Ariel Carrera
 */
public abstract class SpringBootBuildFeaturesExtension extends BaseBuildFeaturesExtension<SpringBootSettingsHandler> {

    public SpringBootBuildFeaturesExtension(Project project, FeatureRegistry registry) {
        super(project, registry);
    }

    @Override
    protected SpringBootSettingsHandler createSettingsHandler() {
        return getObjectFactory().newInstance(SpringBootSettingsHandler.class);
    }

    public static SpringBootBuildFeaturesExtension create(Project project, FeatureRegistry registry) {
        return project.getExtensions().create(EXTENSION_NAME, SpringBootBuildFeaturesExtension.class, project, registry);
    }
}