package io.github.arielcarrera.build.features.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.logging.Logger;
import org.gradle.jvm.tasks.Jar;
import org.springframework.boot.gradle.dsl.SpringBootExtension;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootJar;
import org.springframework.boot.gradle.tasks.run.BootRun;
import io.github.arielcarrera.build.features.BaseBuildFeaturesPlugin;
import io.github.arielcarrera.build.features.DefaultVersions;
import io.github.arielcarrera.build.features.boot.dsl.SpringBootBuildFeaturesExtension;
import io.github.arielcarrera.build.features.boot.tasks.RunAppConfigTask;
import io.github.arielcarrera.build.features.boot.tasks.StopAppConfigTask;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;

/**
 * Main class that implements the SpringBoot Build Features Plugin.
 *
 * @author Ariel Carrera
 */
public class SpringBootBuildFeaturesPlugin extends BaseBuildFeaturesPlugin<SpringBootBuildFeaturesExtension> {
    public static final String PLUGIN_ID = SpringBootBuildFeaturesPlugin.class.getPackageName();
    private static final List<String> DEFAULT_SECRET_VAR_NAMES = List.of("PASSWORD", "PASS", "APIKEY", "API_KEY");
    private static final String FEATURES_PATH = "buildFeatures";
    protected static final List<FeatureScan> FEATURE_SCAN_LIST = List.of(new FeatureScan(BaseBuildFeaturesPlugin.class, FEATURES_PATH));

    @Override
    protected List<FeatureScan> getFeatureScanList() {
        return FEATURE_SCAN_LIST;
    }

    @Override
    protected boolean isLibrary() {
        return false;
    }

    @Override
    protected SpringBootBuildFeaturesExtension createExtension() {
        return SpringBootBuildFeaturesExtension.create(project, registry);
    }

    @Override
    protected void applyAdditionalPlugins() {
        //Spring boot plugin
        project.getPlugins().apply(SpringBootPlugin.class);
    }

    @Override
    protected void configureCommonExtensions() {
        super.configureCommonExtensions();
        // SpringBoot extension
        SpringBootExtension springBootExtension = project.getExtensions().getByType(SpringBootExtension.class);
        springBootExtension.buildInfo(buildInfo -> {
            String implVersion = getImplVersion(project);
            info("Specification Version  : " + project.getVersion().toString());
            info("Implementation Version : " + implVersion);
            buildInfo.getProperties().getAdditional().set(Map.of("version", implVersion));
        });
    }

    @Override
    protected void registerPluginTasks() {
        project.getTasks().register(RunAppConfigTask.RUN_APP_CONFIG_TASK_NAME, RunAppConfigTask.class, task -> {
            task.getDockerComposeFile().set(extension.getSettings().getDockerComposeFile());
            task.getDockerComposeName().set(extension.getSettings().getDockerComposeName());
            task.getDockerComposeProject().set(extension.getSettings().getDockerComposeProject());
        });
        project.getTasks().register(StopAppConfigTask.STOP_APP_CONFIG_TASK_NAME, StopAppConfigTask.class, task -> {
            task.getDockerComposeFile().set(extension.getSettings().getDockerComposeFile());
            task.getDockerComposeName().set(extension.getSettings().getDockerComposeName());
            task.getDockerComposeProject().set(extension.getSettings().getDockerComposeProject());
        });
    }

    @Override
    protected void configurePluginTasks() {
        // disable the -plain jar generation
        project.getTasks().withType(Jar.class).configureEach(jar -> {
            jar.setEnabled(false);
        });
        // add custom manifest metadata on bootJar task execution
        project.getTasks().withType(BootJar.class).configureEach(bootJar -> {
            bootJar.setEnabled(true);
            bootJar.doFirst(task -> {
                bootJar.manifest(manifest -> {
                    manifest.attributes(Map.of("Specification-Title", project.getRootProject().getName(),
                        "Specification-Version", project.getVersion(),
                        "Implementation-Version", getImplVersion(project)
                    ));
                });
            });
        });
        // add custom bootRun
        project.getTasks().withType(BootRun.class).configureEach(bootRun -> {
            Boolean isDockerComposeEnabled = extension.getSettings().getDockerComposeEnabled().getOrElse(Boolean.TRUE);
            if (isDockerComposeEnabled) {
                bootRun.doFirst(task -> {
                    String envFilePath = extension.getSettings().getEnvFile().getOrElse(".env");
                    //set environment variables
                    info("Looking for .env file '" + envFilePath + "'...");
                    File envFile = project.file(envFilePath);
                    if (envFile.exists() && envFile.canRead()) {
                        info("Env file found.\nSetting up environment variables...");
                        Boolean showEnvVarsEnabled = extension.getSettings().getShowEnvVars().getOrElse(Boolean.TRUE);
                        if (!showEnvVarsEnabled) {
                            info("Property 'showEnvVarsEnabled' disabled");
                        }
                        Set<String> calculatedSecretNames = calculateSecretNames();
                        processEnvFile(bootRun, envFile, showEnvVarsEnabled, calculatedSecretNames, project.getLogger());
                    } else {
                        info("Env file not found. File '" + envFilePath + "'.");
                    }
                    info("Starting application...");
                });
                String composeFilename = extension.getSettings().getDockerComposeFile().getOrElse("compose.yaml");
                File composeFile = new File(composeFilename);
                if (composeFile.exists() && composeFile.isFile() && composeFile.canRead()) {
                    info("Docker compose support enabled");
                    bootRun.dependsOn(RunAppConfigTask.RUN_APP_CONFIG_TASK_NAME);
                } else {
                    info("Docker compose not found");
                }
            } else {
                info("Docker compose support disabled");
            }
        });
    }

    @Override
    protected void configureManagedDependencies(DependencyManagementExtension dependencyManagementExtension) {
        dependencyManagementExtension.imports(importsHandler -> {
            if (extension.getSettings().getImportSpringCloudBomEnabled().getOrElse(Boolean.TRUE)) {
                importBom(importsHandler, "springCloudVersion", "org.springframework.cloud", "spring-cloud-dependencies",
                    extension.getSettings().getDefaultSpringCloudVersion(), "defaultSpringCloudVersion",
                    DefaultVersions.KEY_SPRING_CLOUD_VERSION);
            } else {
                info("Skipping import of spring-cloud-dependencies (bom)");
            }
        });
    }

    /**
     * Method for processing the env file.
     *
     * @param bootRun               task for running a Spring Boot application
     * @param envFile               Properties file reference
     * @param showEnvVarsEnabled    Enable log of environment variables
     * @param calculatedSecretNames Set of secret key names to be obfuscated
     * @param logger                The current logger instance
     */
    private static void processEnvFile(BootRun bootRun, File envFile, Boolean showEnvVarsEnabled, Set<String> calculatedSecretNames, Logger logger) {
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {

            String line = reader.readLine();
            while (line != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    int pos = line.indexOf("=");
                    String key = line.substring(0, pos).trim();
                    String value = line.substring(pos + 1).trim();
                    if (System.getenv(key) == null) {
                        if (showEnvVarsEnabled) {
                            if (calculatedSecretNames.stream().anyMatch(key::contains)) {
                                logger.lifecycle("Var " + key + "=***");
                            } else {
                                logger.lifecycle("Var " + key + "=" + value);
                            }
                        }
                        bootRun.environment(key, value);
                    }
                }
                // read next line
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate the secret key set to consider during logging of environment variables.
     *
     * @return Set of secret key names to be obfuscated
     */
    private Set<String> calculateSecretNames() {
        List<String> secretNames = extension.getSettings().getSecretVariableNames().get();
        Set<String> calculatedSecretNames = new HashSet<>(secretNames);
        calculatedSecretNames.addAll(DEFAULT_SECRET_VAR_NAMES);
        return calculatedSecretNames;
    }
}