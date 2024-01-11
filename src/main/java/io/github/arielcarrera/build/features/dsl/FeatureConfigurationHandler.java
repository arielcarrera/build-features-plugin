package io.github.arielcarrera.build.features.dsl;

import org.gradle.api.Action;
import groovy.lang.Closure;

public interface FeatureConfigurationHandler {

	void features(Closure<?> closure);

	void features(Action<EnableFeaturesHandler> action);

	void definitions(Closure<?> closure);

	void definitions(Action<DefinitionsHandler> action);

}