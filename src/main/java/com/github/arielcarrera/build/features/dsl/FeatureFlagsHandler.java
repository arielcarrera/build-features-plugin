package com.github.arielcarrera.build.features.dsl;

import java.util.Map;

public interface FeatureFlagsHandler {

	void value(String name, Boolean value);

	void value(Map<String, Boolean> properties);

}