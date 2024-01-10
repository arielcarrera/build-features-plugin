package com.github.arielcarrera.build.features.dsl;

import java.util.Map;

public interface DependencyHandler {

    void exclude(String group, String name);

    void exclude(String exclusion);

    void exclude(Map<String, String> exclusion);

    /**
     * Use conditionalOnFeatureEnabled instead.
     * @param feature
     */
    @Deprecated(since = "0.2.0")
    void conditionalOnFeature(String feature);

    void conditionalOnFeatureEnabled(String feature);

    void conditionalOnFeatureNotEnabled(String feature);
}