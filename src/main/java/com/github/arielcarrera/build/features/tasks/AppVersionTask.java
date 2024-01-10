package com.github.arielcarrera.build.features.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

abstract public class AppVersionTask extends DefaultTask {
    public static final String TASK = "version";

    private Boolean implementation = Boolean.FALSE;

    @Option(option = "implementation", description = "Request the implementation version.")
    public void setImplementation(String implementation) {
        this.implementation = Boolean.parseBoolean(implementation);
    }

    @Input
    public Boolean getImplementation() {
        return this.implementation;
    }

    @Internal
    abstract public Property<String> getVersion();

    @Internal
    abstract public Property<String> getImplementationVersion();

    public AppVersionTask() {
        setDescription("This task prints the app version");
        getProject().getLogging().captureStandardOutput(LogLevel.QUIET);
    }

    @TaskAction
    public void printVersion() {
        if (Boolean.TRUE.equals(getImplementation())) {
            getLogger().quiet(getImplementationVersion().get());
        } else {
            getLogger().quiet(getVersion().get());
        }
    }
}

