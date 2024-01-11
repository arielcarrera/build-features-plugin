package io.github.arielcarrera.build.features.library;

import java.util.List;
import java.util.Map;

import org.gradle.api.plugins.JavaPluginExtension;
import io.github.arielcarrera.build.features.BaseBuildFeaturesPlugin;
import io.github.arielcarrera.build.features.DefaultVersions;
import io.github.arielcarrera.build.features.library.dsl.LibraryBuildFeaturesExtension;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;

/**
 * Main class that implements the Library Build Features Plugin.
 *
 * @author Ariel Carrera
 */
public class LibraryBuildFeaturesPlugin extends BaseBuildFeaturesPlugin<LibraryBuildFeaturesExtension> {
    public static final String PLUGIN_ID = LibraryBuildFeaturesPlugin.class.getPackageName();
    private static final String FEATURES_PATH = "buildFeatures";
    protected static final List<FeatureScan> FEATURE_SCAN_LIST = List.of(new FeatureScan(BaseBuildFeaturesPlugin.class, FEATURES_PATH));

    @Override
    protected List<FeatureScan> getFeatureScanList() {
        return FEATURE_SCAN_LIST;
    }

    @Override
    protected boolean isLibrary() {
        return true;
    }

    @Override
    protected LibraryBuildFeaturesExtension createExtension() {
        return LibraryBuildFeaturesExtension.create(project, registry);
    }

    @Override
    protected void applyAdditionalPlugins() {
    }

    @Override
    protected void configureCommonExtensions() {
        super.configureCommonExtensions();
        // Java extension
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaPluginExtension.manifest(manifest -> {
            String implVersion = getImplVersion(project);
            manifest.attributes(Map.of("Specification-Title", project.getRootProject().getName(),
                "Specification-Version", project.getVersion().toString(),
                "Implementation-Title", project.getRootProject().getName(),
                "Implementation-Version", implVersion));
        });
    }

    @Override
    protected void registerPluginTasks() {
    }

    @Override
    protected void configurePluginTasks() {
    }

    @Override
    protected void configureManagedDependencies(DependencyManagementExtension dependencyManagementExtension) {
        dependencyManagementExtension.imports(importsHandler -> {
            if (extension.getSettings().getImportSpringBootBom().getOrElse(Boolean.TRUE)) {
                importBom(importsHandler, "springBootVersion", "org.springframework.boot", "spring-boot-dependencies",
                    extension.getSettings().getDefaultSpringBootVersion(), "defaultSpringBootVersion",
                    DefaultVersions.KEY_SPRING_BOOT_VERSION);
            } else {
                info("Skipping import of spring-boot-dependencies (bom)");
            }
            if (extension.getSettings().getImportSpringCloudBomEnabled().getOrElse(Boolean.TRUE)) {
                importBom(importsHandler, "springCloudVersion", "org.springframework.cloud", "spring-cloud-dependencies",
                    extension.getSettings().getDefaultSpringCloudVersion(), "defaultSpringCloudVersion",
                    DefaultVersions.KEY_SPRING_CLOUD_VERSION);
            } else {
                info("Skipping import of spring-cloud-dependencies (bom)");
            }
        });
    }
}