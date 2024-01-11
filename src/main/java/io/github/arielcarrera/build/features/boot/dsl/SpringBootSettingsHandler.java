package io.github.arielcarrera.build.features.boot.dsl;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import io.github.arielcarrera.build.features.dsl.SettingsHandler;

public interface SpringBootSettingsHandler extends SettingsHandler {

    Property<String> getEnvFile();

    Property<Boolean> getShowEnvVars();

    ListProperty<String> getSecretVariableNames();

    Property<Boolean> getDockerComposeEnabled();

    Property<String> getDockerComposeFile();

    Property<String> getDockerComposeName();

    Property<String> getDockerComposeProject();

}