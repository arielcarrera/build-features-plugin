package com.github.arielcarrera.build.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.barfuin.gradle.jacocolog.JacocoLogPlugin;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.impldep.org.junit.platform.launcher.Launcher;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import com.github.arielcarrera.build.features.dsl.BuildFeaturesExtension;
import com.github.arielcarrera.build.features.dependencies.FeatureManager;
import com.github.arielcarrera.build.features.dependencies.FeatureRegistry;
import com.github.arielcarrera.build.features.tasks.AppVersionTask;
import com.github.arielcarrera.build.features.tasks.BuildFeaturesTask;
import com.github.arielcarrera.build.features.tasks.ExportFeatureTask;
import com.github.arielcarrera.build.features.tasks.ListDependenciesTask;
import com.github.arielcarrera.build.features.tasks.PublishFeaturesTask;
import com.github.arielcarrera.build.features.tasks.PublishFeaturesToMavenLocalTask;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension;
import io.spring.gradle.dependencymanagement.dsl.ImportsHandler;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

/**
 * Plugin base class.
 *
 * @param <E> Extension type implementing {@link BuildFeaturesExtension}
 * @author Ariel Carrera
 */

abstract public class BaseBuildFeaturesPlugin<E extends BuildFeaturesExtension> implements Plugin<Project> {

    public record FeatureScan(Class<?> clazz, String... paths) {
    }

    protected E extension;

    protected Project project;

    protected FeatureRegistry registry;

    protected abstract List<FeatureScan> getFeatureScanList();

    private RepositorySettings repositorySettings;

    protected abstract boolean isLibrary();

    @Override
    public void apply(Project project) {
        this.repositorySettings = this.createRepositorySettings();
        this.project = project;
        registry = new FeatureRegistry(project.getLogger());
        extension = this.createExtension();
        applyPlugins();
        configureConventions();
        registerCommonTasks();
        registerPluginTasks();
        configureCommonTasks();
        configurePluginTasks();
        project.afterEvaluate(proj -> {
            scanFeatureFiles();
            configureRepositories(proj.getRepositories());
            configureCommonExtensions();
            configureManagedDependencies(proj.getExtensions().getByType(DependencyManagementExtension.class));
            configureDependencies(proj.getDependencies());
        });
    }

    protected RepositorySettings createRepositorySettings() {
        return new RepositorySettings();
    }

    /**
     * Import a given BOM.
     *
     * @param importsHandler             gradle import handle
     * @param versionProperty            extra property version (ext)
     * @param group                      artifact group
     * @param module                     artifact module name
     * @param defaultVersionProperty     default version property
     * @param defaultVersionPropertyName name of the given property (default version)
     * @param keyDefaultVersion          key of the default version in the {@link DefaultVersions} map
     */
    protected void importBom(ImportsHandler importsHandler, String versionProperty, String group, String module,
                             Property<String> defaultVersionProperty, String defaultVersionPropertyName,
                             String keyDefaultVersion
    ) {
        String bomVersion = ((String) project.getExtensions().getExtraProperties().getProperties().get(versionProperty));
        if (StringUtils.isNotBlank(bomVersion)) {
            info("Importing %s (bom). Version defined by user (%s):  %s".formatted(module, versionProperty, bomVersion));
            bomVersion = bomVersion.trim();
        } else if (defaultVersionProperty.isPresent()) {
            bomVersion = defaultVersionProperty.get().trim();
            info("Importing %s (bom). Version defined by user (%s):  %s".formatted(module, defaultVersionPropertyName, bomVersion));
        } else {
            bomVersion = DefaultVersions.getInstance().getOrElseThrow(keyDefaultVersion).trim();
            info("Importing %s (bom). Default version: %s".formatted(module, bomVersion));
        }
        importsHandler.mavenBom("%s:%s:%s".formatted(group, module, bomVersion));
    }

    protected void scanFeatureFiles() {
        final List<FeatureScan> featureScanInfoList = this.getFeatureScanList();
        featureScanInfoList.forEach(info -> {
            final Set<String> paths = Set.of(info.paths());
            //Scan base jar files
            final File jarFile = new File(info.clazz().getProtectionDomain().getCodeSource().getLocation().getPath());
            if (jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    final Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        final JarEntry jarEntry = entries.nextElement();
                        if (!jarEntry.isDirectory()) {
                            final String name = jarEntry.getName();
                            boolean match = paths.stream().anyMatch(path -> name.startsWith(path + "/"));
                            if (match) {
                                project.getLogger().info("Jar file: " + name);
                                processFile(name);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //If it is running from IDE
                paths.forEach(path -> {
                    final URL url = Launcher.class.getResource("/" + path);
                    if (url != null) {
                        try {
                            final File apps = new File(url.toURI());
                            for (File app : Objects.requireNonNull(apps.listFiles())) {
                                if (!app.isDirectory()) {
                                    final String name = app.getName();
                                    if (name.startsWith(path + "/")) {
                                        project.getLogger().info("Filename: " + name);
                                        processFile(name);
                                    }
                                }
                            }
                        } catch (URISyntaxException | IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        });
    }

    private void processFile(String name) throws IOException {
        final InputStream resourceAsStream = BaseBuildFeaturesPlugin.class.getClassLoader().getResourceAsStream(name);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceAsStream)))) {
            // create temporal file and wrap content...
            final File file = File.createTempFile("feature-", ".tmp");
            file.deleteOnExit();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(extension.getExtensionName() + " {");
                bw.write(System.lineSeparator());
                bw.write("definitions {");
                bw.write(System.lineSeparator());
                boolean isFirstLine = true;
                while (reader.ready()) {
                    String line = reader.readLine();
                    //Skip package definition if present
                    if (isFirstLine && line.startsWith("package ")) {
                        project.getLogger().trace("Skipping package definition in file '{}'", name);
                    } else {
                        bw.write(line);
                        bw.write(System.lineSeparator());
                    }
                    if (isFirstLine) {
                        isFirstLine = false;
                    }
                }
                bw.write("}");
                bw.write(System.lineSeparator());
                bw.write("}");
                bw.write(System.lineSeparator());
            }
            URI resourceURI = file.toURI();
            project.apply(objectConfigurationAction -> {
                project.getLogger().info("Applying feature file: %s. Temporal file: %s".formatted(name, resourceURI.toString()));
                objectConfigurationAction.from(resourceURI);
            });
        }
    }

    protected void configureRepositories(RepositoryHandler repositories) {
        info("Setting up repositories");
        if (repositories.findByName("MavenLocal") == null) {
            repositories.add(repositories.mavenLocal());
        }
        if (repositories.findByName("MavenRepo") == null) {
            repositories.add(repositories.mavenCentral());
        }
        try {
            final String releasesRepoUrl = getEnvironmentVariable(repositorySettings.getReleasesRepositoryUrlVar(), false);
            if (releasesRepoUrl != null) {
                final String releasesRepoUser = getEnvironmentVariable(repositorySettings.getReleasesRepositoryUsernameVar(), false);
                final String releasesRepoPass = getEnvironmentVariable(repositorySettings.getReleasesRepositoryPasswordVar(), false);
                final URI releasesURI = new URI(releasesRepoUrl + (releasesRepoUrl.endsWith("/") ? "maven-releases/" : "/maven-releases/"));
                boolean isReleasesRepositoryDefined = repositories.findByName(repositorySettings.getReleasesRepositoryName()) != null ||
                    repositories.stream().filter(repo -> MavenArtifactRepository.class.isAssignableFrom(repo.getClass()))
                        .anyMatch(repo -> ((MavenArtifactRepository) repo).getUrl().toString().equals(releasesURI.toString()));
                if (!isReleasesRepositoryDefined) {
                    info("Adding releases repository");
                    repositories.maven(repo -> {
                        repo.setName(repositorySettings.getReleasesRepositoryName());
                        repo.setUrl(releasesURI);
                        repo.credentials(cred -> {
                            cred.setUsername(releasesRepoUser);
                            cred.setPassword(releasesRepoPass);
                        });
                        repo.mavenContent(MavenRepositoryContentDescriptor::releasesOnly);
                    });
                } else {
                    info("Using user-defined releases repository");
                }
            }
            final String snapshotsRepoUrl = getEnvironmentVariable(repositorySettings.getSnapshotsRepositoryUrlVar(), false);
            if (snapshotsRepoUrl != null) {
                final String snapshotsRepoUser = getEnvironmentVariable(repositorySettings.getSnapshotsRepositoryUsernameVar(), false);
                final String snapshotsRepoPass = getEnvironmentVariable(repositorySettings.getSnapshotsRepositoryPasswordVar(), false);
                final URI snapshotsURI = new URI(snapshotsRepoUrl + (snapshotsRepoUrl.endsWith("/") ? "maven-snapshots/" : "/maven-snapshots/"));
                boolean isSnapshotsRepositoryDefined = repositories.findByName(repositorySettings.getSnapshotsRepositoryName()) != null ||
                    repositories.stream().filter(repo -> MavenArtifactRepository.class.isAssignableFrom(repo.getClass()))
                        .anyMatch(repo -> ((MavenArtifactRepository) repo).getUrl().toString().equals(snapshotsURI.toString()));
                if (!isSnapshotsRepositoryDefined) {
                    info("Adding snapshots repository");
                    repositories.maven(repo -> {
                        repo.setName(repositorySettings.getSnapshotsRepositoryName());
                        repo.setUrl(snapshotsURI);
                        repo.credentials(cred -> {
                            cred.setUsername(snapshotsRepoUser);
                            cred.setPassword(snapshotsRepoPass);
                        });
                        repo.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
                    });
                } else {
                    info("Using user-defined snapshots repository");
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Create the current extension (settings) bean.
     *
     * @return the extension
     */
    abstract protected E createExtension();

    /**
     * Ensure that all common plugins required for the build are applied.
     */
    protected void applyPlugins() {
        if (isLibrary()) {
            //Java library plugin
            project.getPluginManager().apply(JavaLibraryPlugin.class);
        } else {
            //Java plugin
            project.getPluginManager().apply(JavaPlugin.class);
        }
        //Maven dependency management plugin
        project.getPluginManager().apply(DependencyManagementPlugin.class);
        project.getPluginManager().apply(MavenPublishPlugin.class);
        //Jacoco plugin
        project.afterEvaluate(proj -> {
            if (Boolean.TRUE.equals(extension.getSettings().getTestCoverageEnabled().getOrElse(Boolean.FALSE))) {
                project.getPluginManager().apply(JacocoPlugin.class);
                project.getPluginManager().apply(JacocoLogPlugin.class);
            }
        });
        applyAdditionalPlugins();
    }

    /**
     * Add specific plugins for the implementation.
     */
    abstract protected void applyAdditionalPlugins();

    /**
     * Configure common conentions.
     */
    protected void configureConventions() {
        //Set default source compatibility version
        final JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        javaPluginExtension.setSourceCompatibility(17);
//        javaPluginExtension.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(17));
//        project.afterEvaluate(proj -> {
//            final Property<String> languageVersion = this.extension.getSettings().getJavaVersion();
//            final JavaLanguageVersion javaLanguageVersion = JavaLanguageVersion.of(languageVersion.getOrElse("17"));
//            final JavaPluginExtension javaPluginAfterEvaluate = proj.getExtensions().getByType(JavaPluginExtension.class);
//            javaPluginAfterEvaluate.getToolchain().getLanguageVersion().set(javaLanguageVersion);
//            String ver = languageVersion.getOrElse("17");
//            javaPluginAfterEvaluate.setSourceCompatibility(Integer.parseInt(ver));
//        });
    }

    /**
     * Configure common plugins.
     */
    protected void configureCommonExtensions() {
        //Publishing
        boolean publishEnabled = extension.getSettings().getPublishEnabled().getOrElse(isLibrary() ? Boolean.TRUE :
            Boolean.FALSE).booleanValue();
        if (publishEnabled) {
            final PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            // add repository
            publishing.repositories(artifactRepositories -> {
                try {
                    boolean isSnapshot = project.getVersion().toString().endsWith("SNAPSHOT");
                    if (isSnapshot) {
                        artifactRepositories.add(project.getRepositories().getByName(this.repositorySettings.getSnapshotsRepositoryName()));
                    } else {
                        artifactRepositories.add(project.getRepositories().getByName(this.repositorySettings.getReleasesRepositoryName()));
                    }
                } catch (Exception e) {
                    info("No repository found. Setting up fallback repository (mavenLocal)");
                    artifactRepositories.add(project.getRepositories().getByName("MavenLocal"));
                }
            });

            final MavenPublication mavenPublication = publishing.getPublications().maybeCreate("mavenJava", MavenPublication.class);
            mavenPublication.from(project.getComponents().getByName("java"));
            mavenPublication.setGroupId(project.getGroup().toString());
            String artifactId = extension.getSettings().getArtifactId().getOrNull();
            if (StringUtils.isBlank(artifactId)) {
                throw new InvalidUserDataException("the attribute 'artifactId' is required when publishing is enabled");
            }
            mavenPublication.setArtifactId(artifactId);
            mavenPublication.setVersion(project.getVersion().toString());
        }
    }

    /**
     * Method for registering the new plugin tasks.
     */
    protected void registerCommonTasks() {
        project.getTasks().register(AppVersionTask.TASK, AppVersionTask.class, task -> {
            task.getVersion().set((String) project.getVersion());
            task.getImplementationVersion().set(getImplVersion(project));
        });
        project.getTasks().register(ListDependenciesTask.TASK, ListDependenciesTask.class);
        project.getTasks().register(ExportFeatureTask.TASK, ExportFeatureTask.class);
        project.getTasks().register(BuildFeaturesTask.TASK, BuildFeaturesTask.class);
        project.getTasks().register(PublishFeaturesTask.TASK, PublishFeaturesTask.class);
        project.getTasks().register(PublishFeaturesToMavenLocalTask.TASK, PublishFeaturesToMavenLocalTask.class);
    }

    /**
     * Method for registering the new plugin tasks.
     */
    abstract protected void registerPluginTasks();

    /**
     * Configure common tasks.
     */
    protected void configureCommonTasks() {
        project.getTasks().withType(Test.class).configureEach(Test::useJUnitPlatform);
        //enable/disable publish task
        project.getTasks().getByName("publish", (task) -> {
            boolean publishEnabled = extension.getSettings().getPublishEnabled().getOrElse(isLibrary() ? Boolean.TRUE :
                Boolean.FALSE).booleanValue();
            if (!publishEnabled) {
                info("Publishing disabled");
                task.setEnabled(false);
            } else {
                info("Publishing enabled");
                task.setEnabled(true);
            }
        });
        //Jacoco
        project.afterEvaluate(proj -> {
            boolean testCoverageEnabled = extension.getSettings().getTestCoverageEnabled().getOrElse(Boolean.FALSE).booleanValue();
            if (testCoverageEnabled) {
                JacocoReport jacocoTestReportTask = (JacocoReport) proj.getTasks().getByName("jacocoTestReport");
                jacocoTestReportTask.reports(configurableReports -> {
                    configurableReports.getXml().getRequired().set(Boolean.TRUE);
                    configurableReports.getHtml().getRequired().set(Boolean.TRUE);
                });
                FileSystem fileSystem = FileSystems.getDefault();
                Set<String> testCoverageExclusions = extension.getSettings().getTestCoverageExclusions().getOrElse(Collections.emptySet());
                jacocoTestReportTask.getClassDirectories().setFrom(jacocoTestReportTask.getClassDirectories().getAsFileTree().getFiles().stream().filter(file ->
                    testCoverageExclusions.isEmpty() || testCoverageExclusions.stream().noneMatch(pattern -> fileSystem.getPathMatcher("glob:" + pattern).matches(file.toPath()))).collect(Collectors.toSet())
                );
                JacocoCoverageVerification jacocoTestCoverageVerificationTask = (JacocoCoverageVerification) proj.getTasks().getByName("jacocoTestCoverageVerification");
                jacocoTestCoverageVerificationTask.violationRules(violationRules -> {
                    violationRules.rule(rule -> rule.limit(limit -> limit.setMinimum(extension.getSettings().getTestCoverageMinimumThreshold().getOrElse(BigDecimal.ZERO))));
                });
                jacocoTestCoverageVerificationTask.getClassDirectories().setFrom(jacocoTestCoverageVerificationTask.getClassDirectories().getAsFileTree().getFiles().stream().filter(file ->
                    testCoverageExclusions.isEmpty() || testCoverageExclusions.stream().noneMatch(pattern -> fileSystem.getPathMatcher("glob:" + pattern).matches(file.toPath()))).collect(Collectors.toSet())
                );
                //configure check task
                Task check = proj.getTasks().getByName("check");
                check.dependsOn(jacocoTestReportTask);
                check.dependsOn(jacocoTestCoverageVerificationTask);
            }
        });


    }

    /**
     * Configure actions for certain necessary tasks.
     */
    abstract protected void configurePluginTasks();

    /**
     * Configure managed dependencies.
     */
    abstract protected void configureManagedDependencies(DependencyManagementExtension dependencyManagementExtension);

    /**
     * Add build dependencies to the project.
     *
     * @param dependencies handler
     */
    protected void configureDependencies(org.gradle.api.artifacts.dsl.DependencyHandler dependencies) {
        FeatureManager manager = new FeatureManager(project.getDependencies(), registry, project.getLogger(), project.getExtensions().getExtraProperties());
        manager.applyDependencies();
    }

    /**
     * Returns the implementation version.
     *
     * @return
     */
    protected static String getImplVersion(Project project) {
        final String projVersion = project.getVersion().toString();
        final String implVersion = projVersion.endsWith("-SNAPSHOT") ? projVersion.substring(0, projVersion.length() - 9) + '.' + new Date().getTime() + "-SNAPSHOT" : projVersion;
        return implVersion;
    }

    protected void info(String Definiendo_repositorio_para_snapshots) {
        project.getLogger().lifecycle(Definiendo_repositorio_para_snapshots);
    }

    private String getEnvironmentVariable(String varName, boolean isRequired) {
        final String value = System.getenv(varName);
        if (StringUtils.isBlank(value)) {
            if (isRequired) {
                throw new InvalidUserDataException(varName + " is not defined");
            } else {
                project.getLogger().warn(varName + " is not defined");
            }
        }
        return value;
    }

}