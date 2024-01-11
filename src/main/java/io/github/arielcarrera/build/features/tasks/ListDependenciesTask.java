package io.github.arielcarrera.build.features.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

abstract public class ListDependenciesTask extends DefaultTask {
    public static final String TASK = "listDependencies";

    private Boolean all = Boolean.FALSE;

    @Option(option = "all", description = "Request for all dependencies including resolved ones.")
    public void setAll(String implementation) {
        this.all = Boolean.parseBoolean(implementation);
    }

    @Input
    public Boolean getAll() {
        return this.all;
    }

    public ListDependenciesTask() {
        setDescription("This task prints a list of the project dependencies");
        getProject().getLogging().captureStandardOutput(LogLevel.QUIET);
    }

    @TaskAction
    public void listDependencies() {
        boolean showAll = Boolean.TRUE.equals(getAll());
        this.getProject().getConfigurations().getByName("compileClasspath").getIncoming().getResolutionResult().getAllDependencies()
            .stream().filter(result -> showAll || result.getFrom().toString().startsWith("project :"))
            .sorted((result, result2) -> result.getRequested().toString().compareTo(result2.getRequested().toString()))
            .map(DependencyResult::getRequested).forEach(requested -> getLogger().quiet(String.valueOf(requested)));
    }
}
