package io.github.arielcarrera.build.features.dsl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import io.github.arielcarrera.build.features.DefaultVersions;
import io.github.arielcarrera.build.features.dependencies.DependencyMetadata;
import io.github.arielcarrera.build.features.utils.ClosureAction;
import groovy.lang.Closure;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

class DefaultFeatureHandler implements FeatureHandler {
    private static final String KEY_CONFIGURATION = "configuration";
    private static final String KEY_GROUP = "group";
    private static final String KEY_NAME = "name";
    private static final String KEY_VERSION = "version";
    private static final String KEY_VERSION_PROPERTY = "versionProperty";
    private static final String CONFIG_IMPLEMENTATION = "implementation";
    private static final String CONFIG_TEST_IMPLEMENTATION = "testImplementation";
    private final Set<DependencyMetadata> dependencies = new HashSet<>();


    @Override
    public void dependency(String configuration, String id) {
        dependency(configuration, id, (Action<DependencyHandler>) null);
    }

    @Override
    public void dependency(String configuration, String id, Closure<?> closure) {
        dependency(configuration, id, new ClosureAction<>(closure));
    }

    @Override
    public void dependency(String configuration, String id, Action<DependencyHandler> action) {
        dependency(configuration, id, null, action);
    }

    @Override
    public void dependency(String configuration, String id, String versionProperty) {
        dependency(configuration, id, versionProperty, (Action<DependencyHandler>) null);
    }

    @Override
    public void dependency(String configuration, String id, String versionProperty, Closure<?> closure) {
        dependency(configuration, id, versionProperty, new ClosureAction<>(closure));
    }

    @Override
    public void dependency(String configuration, String id, String versionProperty, Action<DependencyHandler> action) {
        String[] fragment = id.split(":");
        if (fragment.length > 3 || fragment.length < 2 ||
            StringUtils.isEmpty(fragment[0]) ||
            StringUtils.isEmpty(fragment[1]) ||
            (fragment.length == 3 && StringUtils.isEmpty(fragment[2]))) {
            throw new InvalidUserDataException(
                "Dependency  '" + id + "' is malformed. The required syntax is" + " 'group:name:version' (or 'group:name' for managed dependencies)");
        }
        addDependency(configuration, fragment[0], fragment[1], fragment.length == 3 ? fragment[2] : null, versionProperty, action);
    }

    @Override
    public void dependency(Map<String, String> values) {
        dependency(values, (Action<DependencyHandler>) null);
    }

    @Override
    public void dependency(Map<String, String> values, Closure<?> closure) {
        dependency(values, new ClosureAction<>(closure));
    }

    @Override
    public void dependency(Map<String, String> values, Action<DependencyHandler> action) {
        Set<String> missingAttributes = new LinkedHashSet<>(Arrays.asList(KEY_CONFIGURATION, KEY_GROUP, KEY_NAME));
        missingAttributes.removeAll(values.keySet());
        if (!missingAttributes.isEmpty()) {
            throw new InvalidUserDataException(
                "Feature identifier '" + values + "' did not specify " + mapFieldsToString(missingAttributes));
        }
        addDependency(values.get(KEY_CONFIGURATION), values.get(KEY_GROUP), values.get(KEY_NAME), values.get(KEY_VERSION), values.get(KEY_VERSION_PROPERTY), action);
    }

    @Override
    public void implementation(String id) {
        dependency(CONFIG_IMPLEMENTATION, id, (Action<DependencyHandler>) null);
    }

    @Override
    public void implementation(String id, Closure<?> closure) {
        dependency(CONFIG_IMPLEMENTATION, id, new ClosureAction<>(closure));
    }

    @Override
    public void implementation(String id, Action<DependencyHandler> action) {
        dependency(CONFIG_IMPLEMENTATION, id, action);
    }

    @Override
    public void implementation(String id, String versionProperty) {
        dependency(CONFIG_IMPLEMENTATION, id, versionProperty, (Action<DependencyHandler>) null);
    }

    @Override
    public void implementation(String id, String versionProperty, Closure<?> closure) {
        dependency(CONFIG_IMPLEMENTATION, id, versionProperty, new ClosureAction<>(closure));
    }

    @Override
    public void implementation(String id, String versionProperty, Action<DependencyHandler> action) {
        dependency(CONFIG_IMPLEMENTATION, id, versionProperty, action);
    }

    @Override
    public void implementation(Map<String, String> values) {
        implementation(values, (Action<DependencyHandler>) null);
    }

    @Override
    public void implementation(Map<String, String> values, Closure<?> closure) {
        implementation(values, new ClosureAction<>(closure));
    }

    @Override
    public void implementation(Map<String, String> values, Action<DependencyHandler> action) {
        final HashMap<String, String> localValues = new HashMap<>(values);
        localValues.put(KEY_CONFIGURATION, CONFIG_IMPLEMENTATION);
        dependency(localValues, action);
    }

    // testDependency
    @Override
    public void testImplementation(String id, String versionProperty) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, versionProperty, (Action<DependencyHandler>) null);
    }

    @Override
    public void testImplementation(String id, String versionProperty, Closure<?> closure) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, versionProperty, new ClosureAction<>(closure));
    }

    @Override
    public void testImplementation(String id, String versionProperty, Action<DependencyHandler> action) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, versionProperty, action);
    }

    @Override
    public void testImplementation(String id) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, (Action<DependencyHandler>) null);
    }

    @Override
    public void testImplementation(String id, Closure<?> closure) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, new ClosureAction<>(closure));
    }

    @Override
    public void testImplementation(String id, Action<DependencyHandler> action) {
        dependency(CONFIG_TEST_IMPLEMENTATION, id, action);
    }

    @Override
    public void testImplementation(Map<String, String> values) {
        testImplementation(values, (Action<DependencyHandler>) null);
    }

    @Override
    public void testImplementation(Map<String, String> values, Closure<?> closure) {
        testImplementation(values, new ClosureAction<>(closure));
    }

    @Override
    public void testImplementation(Map<String, String> values, Action<DependencyHandler> action) {
        final HashMap<String, String> localValues = new HashMap<>(values);
        localValues.put(KEY_CONFIGURATION, CONFIG_TEST_IMPLEMENTATION);
        dependency(localValues, action);
    }

    private void addDependency(String configuration, String group, String name, String version, String versionProperty, Action<DependencyHandler> action) {
        final DefaultDependencyHandler dependencyHandler = new DefaultDependencyHandler();
        // if there is an action, execute the given action...
        if (action != null) {
            action.execute(dependencyHandler);
        }
        if (version != null) {
            if (version.startsWith("%")) {
                String substring = version.substring(1);
                version = DefaultVersions.getInstance().getOrDefault(substring, version);
            }
            version = version.trim();
        }
        final String condition = !StringUtils.isBlank(dependencyHandler.getConditionalFeatureEnabled()) ? dependencyHandler.getConditionalFeatureEnabled() :
            !StringUtils.isBlank(dependencyHandler.getConditionalFeatureNotEnabled()) ? "!" + dependencyHandler.getConditionalFeatureNotEnabled() : null;
        // add metadata to the registry
        this.dependencies.add(new DependencyMetadata(configuration, group, name, version, versionProperty, dependencyHandler.getExclusions(),
            condition));
    }

    private String mapFieldsToString(Set<String> items) {
        return items.stream().collect(Collectors.joining(", "));
    }

    public Set<DependencyMetadata> getDependencies() {
        return this.dependencies;
    }

}