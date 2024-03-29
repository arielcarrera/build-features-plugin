package io.github.arielcarrera.build.features.dependencies;

/**
 * Dependency Exclusion data definition.
 *
 * @param group the dependency group to exclude
 * @param name  the dependency name to exclude
 * @author Ariel Carrera
 */
public record DependencyExclusion(String group, String name) {
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof DependencyExclusion))
            return false;
        DependencyExclusion other = (DependencyExclusion) obj;
        boolean groupEquals = (this.group == null && other.group == null)
            || (this.group != null && this.group.equals(other.group));
        boolean moduleEquals = (this.name == null && other.name == null)
            || (this.name != null && this.name.equals(other.name));
        return groupEquals && moduleEquals;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (group != null) {
            result = 31 * result + group.hashCode();
        }
        if (name != null) {
            result = 31 * result + name.hashCode();
        }
        return result;
    }

}