package io.github.arielcarrera.build.features.dsl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import io.github.arielcarrera.build.features.dependencies.DependencyMetadata;
import io.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import io.github.arielcarrera.build.features.utils.ClosureAction;
import groovy.lang.Closure;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

class DefaultDefinitionsHandler implements DefinitionsHandler {

    private static final String KEY_ID = "key";
    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVATION_PROPERTY = "activationProperty";
    private final FeatureRegistry registry;

    DefaultDefinitionsHandler(FeatureRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void feature(String id, String name) {
        feature(id, name, null, (Action<FeatureHandler>) null);
    }

    @Override
    public void feature(String id, String name, Closure<?> closure) {
        feature(id, name, null, new ClosureAction<>(closure));
    }

    @Override
    public void feature(String id, String name, Action<FeatureHandler> action) {
        feature(id, name, null, action);
    }

    @Override
    public void feature(String id, String name, String activationProperty) {
        feature(id, name, activationProperty, (Action<FeatureHandler>) null);
    }

    @Override
    public void feature(String id, String name, String activationProperty, Closure<?> closure) {
        feature(id, name, activationProperty, new ClosureAction<>(closure));
    }

    @Override
    public void feature(String id, String name, String activationProperty, Action<FeatureHandler> action) {
        if (StringUtils.isBlank(id) ||
            StringUtils.isBlank(name)) {
            throw new InvalidUserDataException(
                "Feature  '" + id + "' is invalid. Id and name are required");
        }
        registerFeature(id, name, activationProperty, action);
    }

    @Override
    public void feature(Map<String, String> values) {
        feature(values, (Action<FeatureHandler>) null);
    }


    @Override
    public void feature(Map<String, String> values, Closure<?> closure) {
        feature(values, new ClosureAction<>(closure));
    }

    @Override
    public void feature(Map<String, String> values, Action<FeatureHandler> action) {
        final Set<String> missingRequiredAttr = new LinkedHashSet<>(Arrays.asList(KEY_ID, KEY_NAME));
        missingRequiredAttr.removeAll(values.keySet());
        if (!missingRequiredAttr.isEmpty()) {
            throw new InvalidUserDataException(
                "Feature identifier '" + values + "' did not specify " + listMapValues(missingRequiredAttr));
        }
        registerFeature(getAsString(values, KEY_ID), getAsString(values, KEY_NAME), getAsString(values, KEY_ACTIVATION_PROPERTY),
            action);
    }

    private void registerFeature(String key, String name, String activationProperty, Action<FeatureHandler> action) {
        final DefaultFeatureHandler dependenciesHandler = new DefaultFeatureHandler();
        // if there is an action, execute the given action...
        if (action != null) {
            action.execute(dependenciesHandler);
        }
        Set<DependencyMetadata> dependencies = dependenciesHandler.getDependencies();
        // add metadata to the registry
        this.registry.addFeatureDefinition(key, name, dependencies, StringUtils.isBlank(activationProperty) ? key : activationProperty);
    }

    private String listMapValues(Set<String> items) {
        return items.stream().collect(Collectors.joining(", "));
    }

    private String getAsString(Map<? extends CharSequence, ? extends CharSequence> map, String key) {
        CharSequence charSequence = map.get(key);
        return (charSequence != null) ? charSequence.toString() : null;
    }

}