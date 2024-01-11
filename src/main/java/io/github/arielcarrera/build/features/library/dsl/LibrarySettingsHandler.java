package io.github.arielcarrera.build.features.library.dsl;

import org.gradle.api.provider.Property;
import io.github.arielcarrera.build.features.dsl.SettingsHandler;

public interface LibrarySettingsHandler extends SettingsHandler {

    Property<String> getDefaultSpringBootVersion();

    Property<Boolean> getImportSpringBootBom();

}