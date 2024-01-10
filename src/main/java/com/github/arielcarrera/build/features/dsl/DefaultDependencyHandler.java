package com.github.arielcarrera.build.features.dsl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.InvalidUserDataException;
import com.github.arielcarrera.build.features.dependencies.DependencyExclusion;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

class DefaultDependencyHandler implements DependencyHandler {

    private static final String KEY_GROUP = "group";
    private static final String KEY_MODULE = "name";
    private final Set<DependencyExclusion> exclusions = new HashSet<>();
    private String conditionalFeatureEnabled;
    private String conditionalFeatureNotEnabled;

    @Override
    public void exclude(String group, String name) {
        if (StringUtils.isBlank(group) && StringUtils.isBlank(name)) {
            throw new InvalidUserDataException("An exclusion requires at least one field: group or name");
        }
        this.exclusions.add(new DependencyExclusion(group, name));
    }

    @Override
    public void exclude(String exclusion) {
        String[] fragment = exclusion.split(":");
        if (fragment.length != 2) {
            throw new InvalidUserDataException(
                "Exclusion using only one argument requires the form 'group:name'. The exclusion '" + exclusion + "' is malformed.");
        }
        this.exclusions.add(new DependencyExclusion(fragment[0], fragment[1]));
    }

    @Override
    public void exclude(Map<String, String> exclusion) {
        String group = exclusion.get(KEY_GROUP);
        String name = exclusion.get(KEY_MODULE);
        if (StringUtils.isBlank(group) && StringUtils.isBlank(name)) {
            throw new InvalidUserDataException("An exclusion requires at least one field: group or name");
        }
        this.exclusions.add(new DependencyExclusion(group, name));
    }

    @Override
    public void conditionalOnFeature(String feature) {
        this.conditionalOnFeatureEnabled(feature);
    }

    @Override
    public void conditionalOnFeatureEnabled(String feature) {
        if (StringUtils.isBlank(feature)) {
            throw new InvalidUserDataException("The condition requires a flag");
        }
        this.conditionalFeatureEnabled = feature;
    }

    @Override
    public void conditionalOnFeatureNotEnabled(String feature) {
        if (StringUtils.isBlank(feature)) {
            throw new InvalidUserDataException("The condition requires a flag");
        }
        this.conditionalFeatureNotEnabled = feature;
    }

    public String getConditionalFeatureEnabled() {
        return this.conditionalFeatureEnabled;
    }

    public String getConditionalFeatureNotEnabled() {
        return this.conditionalFeatureNotEnabled;
    }

    Set<DependencyExclusion> getExclusions() {
        return this.exclusions;
    }

}