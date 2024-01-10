package com.github.arielcarrera.build.features.dsl;

import java.util.HashMap;
import java.util.Map;

class DefaultFeatureFlagsHandler implements FeatureFlagsHandler {
    private final Map<String, Boolean> values = new HashMap<>();

    @Override
    public void value(String name, Boolean value) {
        this.values.put(name, value);
    }

    @Override
    public void value(Map<String, Boolean> properties) {
        this.values.putAll(properties);
    }

    public Map<String, Boolean> getValues() {
        return new HashMap<>(values);
    }

}