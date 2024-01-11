package io.github.arielcarrera.build.features.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.logging.Logger;

/**
 * Registry of features.
 *
 * @author Ariel Carrera
 */
public class FeatureRegistry {

    private final Set<Feature> features = new HashSet<>();

    private final Map<String, Boolean> selectedFeatures = new HashMap<>();

    private final Logger logger;

    public FeatureRegistry(Logger logger) {
        this.logger = logger;
    }

    public void addFeatureDefinition(String key, String name, Set<DependencyMetadata> dependencies, String activationProperty) {
        logger.info("Registering feature key '%s', name '%s', dependency count '%d', activationProperty '%s'".formatted(key, name, dependencies.size(), activationProperty));
        Set<DependencyMetadata> deps = (dependencies != null) ? dependencies : Collections.emptySet();
        Feature feature = new Feature(key, name, deps, activationProperty);
        this.features.add(feature);
    }

    public List<Feature> getAllFeatures() {
        return new ArrayList<>(this.features);
    }

    public void validateSelectedFeatures() {
        Set<String> selectedKeys = this.selectedFeatures.keySet();
        Set<String> allFeatureKeys = this.features.stream().map(Feature::key).collect(Collectors.toSet());
        List<String> invalidKeys = selectedKeys.stream().filter(key -> !allFeatureKeys.contains(key)).sorted().collect(Collectors.toList());
        if (invalidKeys.isEmpty()) {
            return;
        }
        throw new InvalidUserDataException("Feature does not exist: %s".formatted(invalidKeys.stream().collect(Collectors.joining(", "))));
    }

    public List<Feature> getFeaturesEnabled() {
        final List<Feature> sorted = this.features.stream().filter(feature -> {
            Boolean isEnabled = this.selectedFeatures.getOrDefault(feature.key(), Boolean.FALSE);
            return Boolean.TRUE.equals(isEnabled);
        }).collect(Collectors.toList());
        return sorted;
    }

    public void selectFeatures(Map<String, Boolean> values) {
        if (values != null) {
            logger.lifecycle("selecting features");
            this.selectedFeatures.putAll(values);
        }
    }

    public void enableFeature(String key) {
        if (key != null) {
            logger.trace("Feature '%s' marked as enabled ".formatted(key));
            this.selectedFeatures.put(key, Boolean.TRUE);
        }
    }

    public void disableFeature(String key) {
        if (key != null) {
            logger.lifecycle("Feature '%s' marked as disabled".formatted(key));
            this.selectedFeatures.put(key, Boolean.FALSE);
        }
    }
}