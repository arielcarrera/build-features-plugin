package io.github.arielcarrera.build.features.tasks;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.jetbrains.annotations.NotNull;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

abstract public class BuildFeaturesTask extends DefaultTask {
    public static final String TASK = "buildFeatures";
    public static final String BUILD_FEATURES_REPO_ENV_VAR_NAME = "BUILD_FEATURES_REPO";
    private boolean publish = false;
    private boolean publishToMavenLocal = false;
    private String buildFeaturePath = "";

    @Option(option = "publish", description = "Publish BuildFeatures project to remote repository.")
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    @Option(option = "publishToMavenLocal", description = "Publish BuildFeatures project to local repository.")
    public void setPublishToMavenLocal(boolean publishToMavenLocal) {
        this.publishToMavenLocal = publishToMavenLocal;
    }

    @Option(option = "path", description = "Request the path of the build feature project.")
    public void setBuildFeaturePath(String path) {
        this.buildFeaturePath = path;
    }

    @Input
    public boolean isPublish() {
        return this.publish;
    }

    @Input
    public boolean isPublishToMavenLocal() {
        return this.publishToMavenLocal;
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

    public BuildFeaturesTask() {
        setDescription("This task builds the Build Features project");
        getProject().getLogging().captureStandardOutput(LogLevel.QUIET);
    }

    @TaskAction
    public void buildFeatures() throws IOException {
        getProject().exec(spec -> {
            final String path = getBuildFeaturePath();
            if (StringUtils.isNotBlank(path)) {
                spec.setWorkingDir(new File(path));
                final String publishCmd = resolvePublishCommand();
                if (publishCmd != null) {
                    spec.commandLine("%s/gradlew".formatted(path), "build", publishCmd);
                } else {
                    spec.commandLine("%s/gradlew".formatted(path), "build");
                }
            } else {
                getLogger().error("ERROR: Build Features PATH not found. Try using --buildFeaturePath %PATH% or setting the environment variable " + BUILD_FEATURES_REPO_ENV_VAR_NAME);
            }
        });
    }

    private String resolvePublishCommand() {
        return isPublish() ? "publish" : isPublishToMavenLocal() ?
            "publishToMavenLocal" : null;
    }
}

