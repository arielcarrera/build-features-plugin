package com.github.arielcarrera.build.features.dsl;

import java.util.Map;

import org.gradle.api.Action;
import groovy.lang.Closure;

public interface FeatureHandler {

    void dependency(String configuration, String id);

    void dependency(String configuration, String id, Closure<?> closure);

    void dependency(String configuration, String id, Action<DependencyHandler> action);

    void dependency(String configuration, String id, String versionProperty);

    void dependency(String configuration, String id, String versionProperty, Closure<?> closure);

    void dependency(String configuration, String id, String versionProperty, Action<DependencyHandler> action);

    void dependency(Map<String, String> values);

    void dependency(Map<String, String> values, Closure<?> closure);

    void dependency(Map<String, String> values, Action<DependencyHandler> action);

    //implementation
    void implementation(String id);

    void implementation(String id, Closure<?> closure);

    void implementation(String id, Action<DependencyHandler> action);

    void implementation(String id, String versionProperty);

    void implementation(String id, String versionProperty, Closure<?> closure);

    void implementation(String id, String versionProperty, Action<DependencyHandler> action);

    void implementation(Map<String, String> id);

    void implementation(Map<String, String> id, Closure<?> closure);

    void implementation(Map<String, String> id, Action<DependencyHandler> action);

    // testDependency
    void testImplementation(String id, String versionProperty);

    void testImplementation(String id, String versionProperty, Closure<?> closure);

    void testImplementation(String id, String versionProperty, Action<DependencyHandler> action);

    // testDependency
    void testImplementation(String id);

    void testImplementation(String id, Closure<?> closure);

    void testImplementation(String id, Action<DependencyHandler> action);

    void testImplementation(Map<String, String> values);

    void testImplementation(Map<String, String> values, Closure<?> closure);

    void testImplementation(Map<String, String> values, Action<DependencyHandler> action);
}