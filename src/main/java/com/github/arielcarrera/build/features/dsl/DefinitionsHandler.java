package com.github.arielcarrera.build.features.dsl;

import java.util.Map;

import org.gradle.api.Action;
import groovy.lang.Closure;

public interface DefinitionsHandler {

    void feature(String id, String name);

    void feature(String id, String name, Closure<?> closure);

    void feature(String id, String name, Action<FeatureHandler> action);

    void feature(String id, String name, String propertyName);

    void feature(String id, String name, String propertyName, Closure<?> closure);

    void feature(String id, String name, String propertyName, Action<FeatureHandler> action);

    void feature(Map<String, String> values);

    void feature(Map<String, String> values, Closure<?> closure);

    void feature(Map<String, String> values, Action<FeatureHandler> action);
}