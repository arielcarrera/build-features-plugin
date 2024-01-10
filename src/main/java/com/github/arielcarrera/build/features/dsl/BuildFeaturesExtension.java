package com.github.arielcarrera.build.features.dsl;

import java.util.List;

import org.gradle.api.Action;
import com.github.arielcarrera.build.features.dependencies.Feature;
import groovy.lang.Closure;

/**
 * @author Ariel Carrera
 */
public interface BuildFeaturesExtension<S extends SettingsHandler> extends FeatureConfigurationHandler {

    String getExtensionName();

    void settings(Closure<?> closure);

    void settings(Action<S> action);

    List<Feature> getDefinitions();

    S getSettings();

    List<Feature> getFeatures();
}