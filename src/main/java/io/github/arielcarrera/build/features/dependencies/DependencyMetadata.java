package io.github.arielcarrera.build.features.dependencies;

import java.util.Set;

import org.gradle.api.plugins.ExtraPropertiesExtension;

/**
 * @param configuration
 * @param group
 * @param name
 * @param version
 * @param versionProperty
 * @param excludedDependencies
 * @param activationCondition  condition for activation/inclusion
 * @author Ariel Carrera
 */
public record DependencyMetadata(String configuration, String group, String name, String version, String versionProperty,
                                 Set<DependencyExclusion> excludedDependencies, String activationCondition) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof DependencyMetadata))
            return false;
        DependencyMetadata other = (DependencyMetadata) obj;
        boolean configurationEquals = (this.configuration == null && other.configuration == null)
            || (this.configuration != null && this.configuration.equals(other.configuration));
        boolean groupEquals = (this.group == null && other.group == null)
            || (this.group != null && this.group.equals(other.group));
        boolean moduleEquals = (this.name == null && other.name == null)
            || (this.name != null && this.name.equals(other.name));
        return configurationEquals && groupEquals && moduleEquals;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (configuration != null) {
            result = 31 * result + configuration.hashCode();
        }
        if (group != null) {
            result = 31 * result + group.hashCode();
        }
        if (name != null) {
            result = 31 * result + name.hashCode();
        }
        return result;
    }

    public String resolve(ExtraPropertiesExtension extraPropertiesExtension) {
        if (versionProperty != null) {
            Object value = extraPropertiesExtension.getProperties().get(versionProperty);
            if (value instanceof String strValue) {
                return group + ":" + name + ":" + strValue;
            }
        }
        if (version != null) {
            return group + ":" + name + ":" + version;
        }

        return group + ":" + name;
    }

    @Override
    public String toString() {
        if (version != null) {
            return group + ":" + name + ":" + version;
        } else if (versionProperty != null) {
            return group + ":" + name + ":$" + versionProperty;
        }
        return group + ":" + name;
    }
}