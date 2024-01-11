package io.github.arielcarrera.build.features.dependencies;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import io.github.arielcarrera.build.features.utils.ActionClosure;

/**
 * Manager of Features.
 *
 * @author Ariel Carrera
 */
public class FeatureManager {

    private final DependencyHandler dependencyHandler;
    private final FeatureRegistry registry;
    private final Logger logger;
    private final ExtraPropertiesExtension extraPropertiesExtension;

    public FeatureManager(DependencyHandler dependencyHandler, FeatureRegistry registry, Logger logger, ExtraPropertiesExtension extraPropertiesExtension) {
        this.dependencyHandler = dependencyHandler;
        this.registry = registry;
        this.logger = logger;
        this.extraPropertiesExtension = extraPropertiesExtension;
    }

    public void applyDependencies() {
        logger.lifecycle("Adding features:");
        registry.validateSelectedFeatures();
        List<Feature> featuresEnabled = registry.getFeaturesEnabled();
        featuresEnabled.stream().sorted(Comparator.comparing(Feature::name)).forEach(feature -> logger.lifecycle("> Feature: " + feature.name() + " enabled"));
        featuresEnabled.stream().flatMap(feature -> feature.dependencies().stream()).forEach(
            dep -> {
                if (dep.excludedDependencies().isEmpty()) {
                    //check conditional
                    if (checkActivationCondition(dep)) {
                        dependencyHandler.add(dep.configuration(), dep.resolve(extraPropertiesExtension));
                    }
                } else {
                    Action<ModuleDependency> action = (dependency) -> {
                        dep.excludedDependencies().forEach(exclusion ->
                            {
                                boolean includeGroup = exclusion.group() != null;
                                boolean includeModule = exclusion.name() != null;
                                Map<String, String> excludeMap = includeGroup && includeModule ? Map.of(ExcludeRule.GROUP_KEY, exclusion.group(), ExcludeRule.MODULE_KEY, exclusion.name()) :
                                    includeGroup ? Map.of(ExcludeRule.GROUP_KEY, exclusion.group()) :
                                        includeModule ? Map.of(ExcludeRule.MODULE_KEY, exclusion.name()) : null;
                                if (excludeMap != null) {
                                    dependency.exclude(excludeMap);
                                }
                            }
                        );
                    };
                    //check conditional
                    if (checkActivationCondition(dep)) {
                        dependencyHandler.add(dep.configuration(), dep.resolve(extraPropertiesExtension), new ActionClosure<>(this, action));
                    }
                }
            }
        );
    }

    private boolean checkActivationCondition(DependencyMetadata dep) {
        if (dep.activationCondition() == null) {
            return true;
        } else if (dep.activationCondition().startsWith("!")) {
            // not enabled check...
            final String condition = dep.activationCondition().substring(1);
            return registry.getFeaturesEnabled().stream().noneMatch(feature -> condition.equals(feature.key()));
        }

        return registry.getFeaturesEnabled().stream().anyMatch(feature -> dep.activationCondition().equals(feature.key()));
    }
}