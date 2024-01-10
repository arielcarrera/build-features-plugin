package com.github.arielcarrera.build.features.dsl;

import java.util.List;
import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import com.github.arielcarrera.build.features.dependencies.Feature;
import com.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import com.github.arielcarrera.build.features.utils.ClosureAction;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

/**
 * Base extension.
 *
 * @author Ariel Carrera
 */
public abstract class BaseBuildFeaturesExtension<S extends SettingsHandler> extends GroovyObjectSupport implements BuildFeaturesExtension<S> {
    public static final String EXTENSION_NAME = "buildFeatures";
    protected final Project project;
    protected final FeatureRegistry registry;
    protected S settings;

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    public BaseBuildFeaturesExtension(Project project,
                                      FeatureRegistry registry) {
        this.project = project;
        this.registry = registry;
        this.settings = createSettingsHandler();
    }

    abstract protected S createSettingsHandler();

    @Override
    public void settings(Closure<?> closure) {
        settings(new ClosureAction<>(closure));
    }

    @Override
    public void settings(Action<S> action) {
        action.execute(settings);
    }

    @Override
    public S getSettings() {
        return this.settings;
    }

    @Override
    public void features(Closure<?> closure) {
        new DefaultFeatureConfigurationHandler(this.registry).features(closure);
    }

    @Override
    public void features(Action<EnableFeaturesHandler> action) {
        new DefaultFeatureConfigurationHandler(this.registry).features(action);
    }

    @Override
    public List<Feature> getFeatures() {
        return this.registry.getFeaturesEnabled();
    }

    @Override
    public void definitions(Closure<?> closure) {
        new DefaultFeatureConfigurationHandler(this.registry).definitions(closure);
    }

    @Override
    public void definitions(Action<DefinitionsHandler> action) {
        new DefaultFeatureConfigurationHandler(this.registry).definitions(action);
    }

    @Override
    public List<Feature> getDefinitions() {
        return this.registry.getAllFeatures();
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

}