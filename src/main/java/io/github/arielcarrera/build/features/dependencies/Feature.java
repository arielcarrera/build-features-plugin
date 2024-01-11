package io.github.arielcarrera.build.features.dependencies;

import java.util.Set;

/**
 * Feature data definition.
 *
 * @param key the feature key/id
 * @param name the name of the feature
 * @param dependencies the dependencies included in the feature
 * @param activationProperty the activation condition/property of the feature
 * @author Ariel Carrera
 */
public record Feature(String key, String name, Set<DependencyMetadata> dependencies, String activationProperty) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Feature))
            return false;
        Feature other = (Feature) obj;

        return (this.key == null && other.key == null)
            || (this.key != null && this.key.equals(other.key));
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (key != null) {
            result = 31 * result + key.hashCode();
        }
        return result;
    }
}