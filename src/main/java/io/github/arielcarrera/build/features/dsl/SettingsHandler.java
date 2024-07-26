package io.github.arielcarrera.build.features.dsl;

import java.math.BigDecimal;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

public interface SettingsHandler {

    Property<String> getJavaVersion();

    Property<String> getTargetJavaVersion();

    Property<String> getDefaultSpringCloudVersion();

    Property<Boolean> getImportSpringCloudBomEnabled();

    Property<Boolean> getPublishEnabled();

    Property<String> getArtifactId();

    Property<Boolean> getTestCoverageEnabled();

    SetProperty<String> getTestCoverageExclusions();

    Property<BigDecimal> getTestCoverageMinimumThreshold();

}