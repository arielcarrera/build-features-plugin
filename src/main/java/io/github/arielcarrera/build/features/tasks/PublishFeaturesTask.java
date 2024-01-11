package io.github.arielcarrera.build.features.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

abstract public class PublishFeaturesTask extends DefaultTask {
    public static final String TASK = "publishFeatures";
    public static final String BUILD_FEATURES_REPO_ENV_VAR_NAME = "BUILD_FEATURES_REPO";
    private String buildFeaturePath = "";

    @Option(option = "path", description = "Request the path of the build feature project.")
    public void setBuildFeaturePath(String path) {
        this.buildFeaturePath = path;
    }

    @Input
    @Optional
    public String getBuildFeaturePath() {
        if (StringUtils.isNotBlank(this.buildFeaturePath)) {
            return this.buildFeaturePath;
        }
        final String path = System.getenv().get(BUILD_FEATURES_REPO_ENV_VAR_NAME);

        return StringUtils.isNotBlank(path) ? path : "";
    }

    public PublishFeaturesTask() {
        setDescription("This task builds and publishes the Build Features project");
        getProject().getLogging().captureStandardOutput(LogLevel.QUIET);
    }

    @TaskAction
    public void publishFeatures() throws IOException {
        getProject().exec(spec -> {
            final String path = getBuildFeaturePath();
            if (StringUtils.isNotBlank(path)) {
                spec.setWorkingDir(new File(path));
                spec.commandLine("%s/gradlew".formatted(path), "build", "publish");
            } else {
                getLogger().error("ERROR: Build Features PATH not found. Try using --buildFeaturePath %PATH% or setting the environment variable " + BUILD_FEATURES_REPO_ENV_VAR_NAME);
            }
        });

    }
}

