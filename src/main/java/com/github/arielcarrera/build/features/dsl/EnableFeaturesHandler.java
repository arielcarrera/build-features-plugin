package com.github.arielcarrera.build.features.dsl;

import java.util.Collection;
import java.util.Set;

import org.gradle.api.Action;
import groovy.lang.Closure;

public interface EnableFeaturesHandler {

    void enable(String feature);

    void enable(Collection<String> features);

    void enable(Closure<?> closure);

    void enable(Action<Set<String>> action);

    void disable(String feature);

    void disable(Set<String> features);

    void status(Closure<?> closure);

    void status(Action<FeatureFlagsHandler> action);

}