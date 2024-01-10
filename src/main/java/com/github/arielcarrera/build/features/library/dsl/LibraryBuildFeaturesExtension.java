package com.github.arielcarrera.build.features.library.dsl;

import org.gradle.api.Project;
import com.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import com.github.arielcarrera.build.features.dsl.BaseBuildFeaturesExtension;

/**
 * Extension for Library
 * @author Ariel Carrera
 */
public abstract class LibraryBuildFeaturesExtension extends BaseBuildFeaturesExtension<LibrarySettingsHandler> {

    public LibraryBuildFeaturesExtension(Project project, FeatureRegistry registry) {
        super(project, registry);
    }

    @Override
    protected LibrarySettingsHandler createSettingsHandler() {
        return getObjectFactory().newInstance(LibrarySettingsHandler.class);
    }

    public static LibraryBuildFeaturesExtension create(Project project, FeatureRegistry registry) {
        return project.getExtensions().create(EXTENSION_NAME, LibraryBuildFeaturesExtension.class, project, registry);
    }

}