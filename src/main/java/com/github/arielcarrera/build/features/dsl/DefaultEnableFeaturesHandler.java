package com.github.arielcarrera.build.features.dsl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Action;
import com.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import com.github.arielcarrera.build.features.utils.ClosureAction;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

class DefaultEnableFeaturesHandler extends GroovyObjectSupport implements EnableFeaturesHandler {

    private final FeatureRegistry registry;

    DefaultEnableFeaturesHandler(FeatureRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void enable(String feature) {
        this.registry.enableFeature(feature);
    }

    @Override
    public void enable(Collection<String> features) {
        features.forEach(this.registry::enableFeature);
    }

    @Override
    public void enable(Closure<?> closure) {
        this.enable(new ClosureAction<>(closure));
    }

    @Override
    public void enable(Action<Set<String>> action) {
        Set<String> collection = new HashSet<>();
        if (action != null) {
            action.execute(collection);
        }
        collection.forEach(this.registry::enableFeature);
    }

    @Override
    public void disable(String feature) {
        this.registry.disableFeature(feature);
    }

    @Override
    public void disable(Set<String> features) {
        features.forEach(this.registry::disableFeature);
    }

    @Override
    public void status(Closure<?> closure) {
        this.status(new ClosureAction<>(closure));
    }

    @Override
    public void status(Action<FeatureFlagsHandler> action) {
        DefaultFeatureFlagsHandler handler = new DefaultFeatureFlagsHandler();
        if (action != null) {
            action.execute(handler);
        }
        this.registry.selectFeatures(handler.getValues());
    }


}