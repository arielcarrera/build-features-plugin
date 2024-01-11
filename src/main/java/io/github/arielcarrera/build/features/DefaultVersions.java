package io.github.arielcarrera.build.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.InvalidUserDataException;

/**
 * Default dependency versions.
 *
 * @author Ariel Carrera
 */
public final class DefaultVersions {
    public static final String KEY_SPRING_BOOT_VERSION = "SPRING_BOOT_VERSION";
    public static final String KEY_SPRING_CLOUD_VERSION = "SPRING_CLOUD_VERSION";
    private static final String PROPERTIES_FILE = "build-features-versions.properties";
    private final Properties VERSIONS;

    private static DefaultVersions instance;

    private DefaultVersions() {
        this.VERSIONS = new Properties();
        try {
            VERSIONS.load(BaseBuildFeaturesPlugin.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void put(String key, String version) {
        VERSIONS.putIfAbsent(key, version);
    }

    public String get(String key) {
        return (String) VERSIONS.get(key);
    }

    public String getOrElseThrow(String key) {
        String value = (String) VERSIONS.get(key);
        if (value != null) {
            return value;
        } else {
            throw new InvalidUserDataException(String.format("Cannot get property '%s' on default versions as it does not exist".formatted(key)));
        }
    }

    public String getOrElseThrow(String key, Supplier<? extends RuntimeException> exceptionSupplier) {
        String value = (String) VERSIONS.get(key);
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public String getOrDefault(String key, String defaultValue) {
        return (String) VERSIONS.getOrDefault(key, defaultValue);
    }

    public String getOrCompute(String key, Function<Object, String> mappingFunction) {
        return (String) VERSIONS.computeIfAbsent(key, mappingFunction);
    }

    public Set<String> list() {
        return VERSIONS.keySet().stream().filter(String.class::isInstance).map(String.class::cast).collect(Collectors.toSet());
    }

    public void load(InputStream inStream) throws IOException {
        VERSIONS.load(inStream);
    }

    public synchronized void load(ClassLoader classLoader, String resourceName) throws IOException {
        VERSIONS.load(classLoader.getResourceAsStream(resourceName));
    }

    public static DefaultVersions getInstance() {
        if (instance == null) {
            synchronized (DefaultVersions.class) {
                if (instance == null) {
                    instance = new DefaultVersions();
                }
            }
        }
        return instance;
    }
}