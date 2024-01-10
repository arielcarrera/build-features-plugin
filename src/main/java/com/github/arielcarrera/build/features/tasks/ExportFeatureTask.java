package com.github.arielcarrera.build.features.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import com.github.arielcarrera.build.features.dsl.BaseBuildFeaturesExtension;
import io.spring.gradle.dependencymanagement.org.apache.commons.lang3.StringUtils;

abstract public class ExportFeatureTask extends DefaultTask {
    public static final String TASK = "exportFeature";
    public static final String PROPERTIES_FILE_NAME = "features-versions.properties";
    public static final String BUILD_FEATURES_REPO_ENV_VAR_NAME = "BUILD_FEATURES_REPO";
    public static final String RESOURCES_BUILD_FEATURES_DIR_PATH = "/src/main/resources/buildFeatures";
    public static final String RESOURCES_BUILD_FEATURES_PROPERTIES_PATH = "/src/main/resources";
    public static final String RESOURCES_BUILD_FEATURES_PROPERTIES_FILENAME = "build-features-versions.properties";
    private static final String DEPENDENCIES_CONTENT_PATTERN = "^\\s*dependencies\\s*\\{([.\\n\\s\\w\\(\\)'\\\":\\-_\\/]*)\\}";
    private static final String SETTINGS_CONTENT_PATTERN = "(^\\s*%s\\s*\\{[.\\n\\s\\w\\(\\)'\\\":\\-_\\/{}=]*features\\s*\\{)([.\\n\\s\\w\\(\\)'\\\":\\-_\\/=]*)(}[.\\n\\s\\w\\(\\)'\\\":\\-_\\/{}=]*})";

    private String dependency;
    private String featureName = "";
    private String desc = "";
    private String property = "";
    private String buildFeaturePath = "";
    private boolean force = false;

    @Option(option = "dependency", description = "Request the dependency to export.")
    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    @Option(option = "name", description = "Request the feature name.")
    public void setFeatureName(String name) {
        this.featureName = name;
    }

    @Option(option = "desc", description = "Request the feature description.")
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Option(option = "property", description = "Request the version property name.")
    public void setProperty(String versionProperty) {
        this.property = versionProperty;
    }

    @Option(option = "path", description = "Request the path of the build feature project.")
    public void setBuildFeaturePath(String path) {
        this.buildFeaturePath = path;
    }

    @Option(option = "f", description = "Force / override the output file.")
    public void setForce(boolean force) {
        this.force = force;
    }

    @Input
    public String getDependency() {
        return this.dependency;
    }

    @Input
    @Optional
    public String getFeatureName() {
        return this.featureName;
    }

    @Input
    @Optional
    public String getDesc() {
        return this.desc;
    }

    @Input
    @Optional
    public String getProperty() {
        return this.property;
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

    @Input
    public boolean isForce() {
        return this.force;
    }

    public ExportFeatureTask() {
        setDescription("This task exports a dependency as a new feature");
        getProject().getLogging().captureStandardOutput(LogLevel.QUIET);
    }

    @TaskAction
    public void export() throws IOException {
        final boolean saveToRepository = StringUtils.isNotBlank(getBuildFeaturePath());

        String fName = StringUtils.isBlank(getFeatureName()) ? nameToCamelCase(dependency) : getFeatureName();
        if (fName.contains(" ")) {
            getLogger().error("ERROR: Feature name must not contain whitespace character");
            return;
        }
        String fDesc = StringUtils.isBlank(getDesc()) ? camelCaseToDesc(fName) : getDesc();
        final Set<Dependency> dependencies = new HashSet<>();
        getProject().getConfigurations().all(c -> {
            Set<Dependency> deps = c.getAllDependencies().stream().filter(item -> item.getName().contains(dependency)).collect(Collectors.toSet());
            dependencies.addAll(deps);
        });
        if (!dependencies.isEmpty()) {
            Map<String, String> versions = new HashMap<>();
            String impls = dependencies.stream().map(dep -> {
                    final String version = nameToSnakeCaseVersion(dep.getName());
                    if (StringUtils.isNotBlank(dep.getVersion())) {
                        versions.put(version, dep.getVersion());
                    }
                    Set<String> configurations = resolveConfiguration(dep);
                    return configurations.stream().map(cfg -> "    %s('%s:%s:%%%s', '%s')"
                        .formatted(cfg, dep.getGroup(), dep.getName(), version, nameToCamelCaseVersion(dep.getName())
                        )).collect(Collectors.joining(System.lineSeparator()));
                }
            ).collect(Collectors.joining(System.lineSeparator()));
            if (!versions.isEmpty()) {
                writeProperties(PROPERTIES_FILE_NAME, versions, saveToRepository);
            }
            writeFeature(fName, fDesc, impls, saveToRepository);
            if (saveToRepository) {
                publishToMavenLocal();
                updateBuildFile(fName, dependencies);
            }
        } else {
            getLogger().quiet("No result");
        }
    }

    private void publishToMavenLocal() throws IOException {
        PublishFeaturesToMavenLocalTask task = (PublishFeaturesToMavenLocalTask) getProject().getTasks().findByName(PublishFeaturesToMavenLocalTask.TASK);
        if (task != null) {
            getLogger().quiet("Executing Build Features: publishToMavenLocal");
            task.setBuildFeaturePath(getBuildFeaturePath());
            task.publishFeatures();
        } else {
            getLogger().quiet("Executing Build Features publishToMavenLocal");
            getLogger().quiet("NO TASK");
        }
    }

    private Set<String> resolveConfiguration(Dependency dep) {
        return getProject().getConfigurations().getAsMap().entrySet().stream().filter(entry ->
                !entry.getKey().endsWith("Classpath") && !entry.getKey().endsWith("Elements"))
            .filter(entry -> {
                List<Dependency> matching = entry.getValue().getIncoming().getDependencies().matching(it -> Objects.equals(dep.getGroup(), it.getGroup())
                    && dep.getName().equals(it.getName())).stream().toList();
                return !matching.isEmpty();
            }).map(HashMap.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private void updateBuildFile(String featureName, Set<Dependency> dependencies) throws IOException {
        final Path path = getProject().getBuildFile().toPath();
        //backup
        backupFile(path);
        //Read build file
        String content = Files.readString(path);
        //remove dependencies
        content = removeDependency(content, dependencies);
        content = addFeature(path, content, featureName);
        writeFile(path, content);
    }

    private String addFeature(Path path, String content, String fName) {
        final String extensionName = getProject().getExtensions().getByType(BaseBuildFeaturesExtension.class).getExtensionName();
        final Pattern compile = Pattern.compile(SETTINGS_CONTENT_PATTERN
            .formatted(extensionName), Pattern.MULTILINE);
        final Matcher matcher = compile.matcher(content);
        if (matcher.find() && matcher.groupCount() > 2) {
            final String section = matcher.group(2);
            final String[] split = section.split(System.lineSeparator());
            final boolean exists = Stream.of(split).anyMatch(line ->
                line.contains("enable \"%s\"".formatted(fName)) ||
                    line.contains("enable '%s'".formatted(fName)) ||
                    line.contains("enable(\"%s\")".formatted(fName)) ||
                    line.contains("enable('%s')".formatted(fName))
            );
            if (!exists) {
                int lastBreak = section.lastIndexOf(System.lineSeparator());
                int index = -1;
                if (lastBreak > 0) {
                    index = section.substring(0, lastBreak).lastIndexOf(System.lineSeparator());
                }
                if (index < 0) {
                    index = 0;
                }
                int indentation = calcIndentation(section, index);
                final String newSectionContent = section.substring(0, lastBreak) + System.lineSeparator() + StringUtils.repeat(" ", --indentation)
                    + "enable '%s'".formatted(fName) + section.substring(lastBreak);
                return matcher.replaceAll("$1" + newSectionContent + "$3");
            }
        }
        return content;
    }

    private void writeFile(Path path, String newContent) {
        try {
            Files.writeString(path, newContent);
        } catch (IOException e) {
            getLogger().error("ERROR: I/O exception");
        }
    }

    private String removeDependency(String content, Set<Dependency> dependencies) throws IOException {
        //replace
        final Pattern compile = Pattern.compile(DEPENDENCIES_CONTENT_PATTERN, Pattern.MULTILINE);
        final Matcher matcher = compile.matcher(content);
        if (matcher.find()) {
            final String section = matcher.group();
            final String[] split = section.split(System.lineSeparator());
            for (Dependency dep : dependencies) {
                final List<String> result = Stream.of(split).filter(line -> !(line.contains(dep.getGroup() + ":" + dep.getName() + ":")
                    || line.contains(dep.getGroup() + ":" + dep.getName() + "'")
                    || line.contains(dep.getGroup() + ":" + dep.getName() + "\""))).toList();
                if (result.size() < split.length) {
                    return matcher.replaceAll(String.join(System.lineSeparator(), result) + System.lineSeparator());
                }
            }
        }
        return content;
    }

    private void writeProperties(String fName, Map<String, String> versions, boolean promoteToBuildFeaturesRepo) throws IOException {
        final String propertiesContent = versions.entrySet().stream().map(entry ->
            "%s=%s".formatted(entry.getKey(), entry.getValue())
        ).collect(Collectors.joining(System.lineSeparator()));
        getLogger().quiet(PROPERTIES_FILE_NAME + " content:");
        getLogger().quiet(propertiesContent + System.lineSeparator());

        Path path = null;
        if (promoteToBuildFeaturesRepo) {
            //open
            path = Path.of(getBuildFeaturePath(), RESOURCES_BUILD_FEATURES_PROPERTIES_PATH,
                RESOURCES_BUILD_FEATURES_PROPERTIES_FILENAME);
            final File file = path.toFile();
            if (file.exists() && file.canWrite()) {
                //load props
                final Properties props = new Properties();
                props.load(new FileInputStream(file));
                //backup file if needed
                boolean mustBackup = versions.keySet().stream().anyMatch(props::containsKey);
                if (mustBackup) {
                    //backup
                    backupFile(path);
                }
                //update properties
                props.putAll(versions);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    props.store(out, null);
                }
                return;
            } else {
                path = Path.of(getBuildFeaturePath(), RESOURCES_BUILD_FEATURES_PROPERTIES_PATH);
                getLogger().error("ERROR: File %s not found or permission denied".formatted(file.getAbsolutePath()));
            }
        }
        if (path == null) {
            path = getProject().getRootDir().toPath();
        }
        final File file = path.resolve(camelCaseToFileName(fName) + ".properties").toFile();
        if (!isForce() && file.exists()) {
            getLogger().error("ERROR: File %s already exists \n".formatted(file.getName()));
        } else {
            writeFile(file.toPath(), propertiesContent);
        }
    }

    private void backupFile(Path path) throws IOException {
        final Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
        getProject().getLogger().quiet("backup file '%s' to '%s".formatted(path.getFileName(), backupPath.toString()));
        Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeFeature(String fName, String desc, String impls, boolean promoteToBuildFeaturesRepo) throws IOException {
        getLogger().quiet("feature %s content:".formatted(fName));
        final String content = """
            package buildFeatures
            feature('%s', '%s') {
            %s
            }
            """.formatted(fName, desc, impls);
        getLogger().quiet(content);

        Path path;
        if (promoteToBuildFeaturesRepo) {
            path = Path.of(getBuildFeaturePath(), RESOURCES_BUILD_FEATURES_DIR_PATH);
        } else {
            path = Path.of(getProject().getRootDir().toString(), RESOURCES_BUILD_FEATURES_DIR_PATH);
        }
        final String fileName = camelCaseToFileName(fName) + ".gradle";
        path = path.resolve(fileName);
        final File file = path.toFile();
        final boolean fileExists = file.exists();
        if (!isForce() && fileExists) {
            getLogger().error("ERROR: File %s already exists \n".formatted(file.getName()));
        } else {
            if (fileExists) {
                //backup file
                backupFile(path);
            }
            writeFile(file.toPath(), content);
        }
    }

    private String nameToSnakeCaseVersion(String name) {
        String upperCase = name.toUpperCase();
        if (StringUtils.isBlank(upperCase)) {
            return "BLANK_VERSION";
        } else if (upperCase.length() == 1) {
            return upperCase + "_VERSION";
        }
        StringBuilder stringBuilder = new StringBuilder().append(upperCase.charAt(0));
        boolean nextSpace = false;
        for (Character c : upperCase.substring(1).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (nextSpace) {
                    nextSpace = false;
                    stringBuilder.append("_").append(c);
                } else {
                    stringBuilder.append(c);
                }
            } else {
                nextSpace = true;
            }
        }
        return stringBuilder + "_VERSION";
    }

    private String nameToCamelCaseVersion(String name) {
        return nameToCamelCase(name) + "Version";
    }

    private String nameToCamelCase(String name) {
        String lowerCase = name.toLowerCase();
        if (StringUtils.isBlank(lowerCase)) {
            return "blank";
        } else if (lowerCase.length() == 1) {
            return lowerCase;
        }
        StringBuilder stringBuilder = new StringBuilder().append(lowerCase.charAt(0));
        boolean nextCapitalize = false;
        for (Character c : lowerCase.substring(1).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (nextCapitalize) {
                    nextCapitalize = false;
                    stringBuilder.append(Character.toUpperCase(c));
                } else {
                    stringBuilder.append(c);
                }
            } else {
                nextCapitalize = true;
            }
        }
        return stringBuilder.toString();
    }

    private String camelCaseToDesc(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        } else if (name.length() == 1) {
            return name.toUpperCase();
        }
        Character last = Character.toUpperCase(name.charAt(0));
        StringBuilder stringBuilder = new StringBuilder().append(last);
        boolean nextWord = false;
        for (Character c : name.substring(1).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (nextWord) {
                    nextWord = false;
                    last = Character.toUpperCase(c);
                    stringBuilder.append(" ").append(last);
                } else {
                    if (Character.isUpperCase(c) && Character.isLowerCase(last)) {
                        last = Character.toUpperCase(c);
                        stringBuilder.append(" ").append(last);
                    } else {
                        last = c;
                        stringBuilder.append(last);
                    }
                }
            } else {
                nextWord = true;
            }
        }
        return stringBuilder.toString();
    }

    private String camelCaseToFileName(String name) {
        if (StringUtils.isBlank(name)) {
            return "export";
        } else if (name.length() == 1) {
            return name.toLowerCase();
        }
        StringBuilder stringBuilder = new StringBuilder().append(Character.toLowerCase(name.charAt(0)));
        boolean nextWord = false;
        boolean lastMidleLetter = false;
        for (Character c : name.substring(1).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (nextWord) {
                    nextWord = false;
                    lastMidleLetter = false;
                    stringBuilder.append("-").append(Character.toLowerCase(c));
                } else {
                    if (Character.isUpperCase(c) && lastMidleLetter) {
                        stringBuilder.append("-").append(Character.toLowerCase(c));
                        lastMidleLetter = false;
                    } else {
                        stringBuilder.append(c);
                        lastMidleLetter = true;
                    }
                }
            } else {
                nextWord = true;
            }
        }

        return stringBuilder.toString();
    }

    private static int calcIndentation(String str, int startIndex) {
        int whitespaceCount = 0;
        for (int i = startIndex; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                whitespaceCount++;
            } else {
                return whitespaceCount;
            }
        }
        return whitespaceCount;
    }
}

