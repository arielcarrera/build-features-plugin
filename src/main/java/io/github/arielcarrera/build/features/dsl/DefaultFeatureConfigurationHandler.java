package io.github.arielcarrera.build.features.dsl;

import org.gradle.api.Action;
import io.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import groovy.lang.Closure;

class DefaultFeatureConfigurationHandler implements FeatureConfigurationHandler {

    private final FeatureRegistry registry;

    DefaultFeatureConfigurationHandler(FeatureRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void features(Closure<?> closure) {
        if (closure != null) {
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.setDelegate(new DefaultEnableFeaturesHandler(this.registry));
            closure.call();
        }
    }

    @Override
    public void features(Action<EnableFeaturesHandler> action) {
        action.execute(new DefaultEnableFeaturesHandler(this.registry));
    }

    @Override
    public void definitions(Closure<?> closure) {
        if (closure != null) {
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.setDelegate(new DefaultDefinitionsHandler(this.registry));
            closure.call();
        }
    }

    @Override
    public void definitions(Action<DefinitionsHandler> action) {
        action.execute(new DefaultDefinitionsHandler(this.registry));
    }

}