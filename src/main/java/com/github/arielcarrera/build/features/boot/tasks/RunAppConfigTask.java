package com.github.arielcarrera.build.features.boot.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import com.github.arielcarrera.build.features.utils.OSPlatform;

abstract public class RunAppConfigTask extends DefaultTask {
    public static final String RUN_APP_CONFIG_TASK_NAME = "runAppConfig";

    @Optional
    @Input
    abstract public Property<String> getDockerComposeFile();

    @Optional
    @Input
    abstract public Property<String> getDockerComposeName();

    @Optional
    @Input
    abstract public Property<String> getDockerComposeProject();

    public RunAppConfigTask() {
        setDescription("This task starts the app-config");
    }

    @TaskAction
    public void resolveLatestVersion() {
        getLogger().lifecycle("Iniciando app-config...");
        getProject().exec(execSpec -> {
            String commandLine = "docker compose -f " + getDockerComposeFile().getOrElse("compose.yaml") + " -p " +
                getDockerComposeProject().getOrElse("app-config") + " up -d app-config";
            if (OSPlatform.isWindows()) {
                execSpec.commandLine("cmd", "/c", commandLine);
            } else {
                execSpec.commandLine("sh", "-c", commandLine);
            }
        });
        int elapsed = 0;
        String result = "";
        while (!result.contains("healthy") && elapsed < 30) {
            getLogger().lifecycle("Esperando inicio de app-config...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // InterruptedException during sleep
            }
            elapsed++;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                getProject().exec(execSpec -> {
                    String commandLine = "docker compose -p " + getDockerComposeProject().getOrElse("app-config") + " ps --status=running | %GREP_COMMAND% "
                        + getDockerComposeName().getOrElse("app-config");
                    if (OSPlatform.isWindows()) {
                        execSpec.commandLine("cmd", "/c", commandLine.replace("%GREP_COMMAND%", "findstr"));
                    } else {
                        execSpec.commandLine("sh", "-c", commandLine.replace("%GREP_COMMAND%", "grep"));
                    }
                    execSpec.setStandardOutput(os);
                });
                result = os.toString();
                getLogger().debug(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        getLogger().lifecycle("App-config disponible");
    }
}

