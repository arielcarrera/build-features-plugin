package io.github.arielcarrera.build.features;

/**
 * Repository settings.
 *
 * @author Ariel Carrera
 */
public class RepositorySettings {

    private final String repoReleasesUrlVar;
    private final String repoReleasesName;
    private final String repoReleasesUsernameVar;
    private final String repoReleasesPasswordVar;
    private final String repoSnapshotsUrlVar;
    private final String repoSnapshotsName;
    private final String repoSnapshotsUsernameVar;
    private final String repoSnapshotsPasswordVar;

    public RepositorySettings() {
        this.repoReleasesUrlVar = "NEXUS_URL";
        this.repoReleasesName = "releasesRepository";
        this.repoReleasesUsernameVar = "NEXUS_USER";
        this.repoReleasesPasswordVar = "NEXUS_PASS";
        this.repoSnapshotsUrlVar = "NEXUS_URL";
        this.repoSnapshotsName = "snapshotsRepository";
        this.repoSnapshotsUsernameVar = "NEXUS_USER";
        this.repoSnapshotsPasswordVar = "NEXUS_PASS";
    }

    public RepositorySettings(String releasesUrl, String releasesName, String releasesUsername, String releasesPassword,
                              String snapshotsUrl, String snapshotsName, String snapshotsUsername, String snapshotsPassword) {
        this.repoReleasesUrlVar = releasesUrl;
        this.repoReleasesName = releasesName;
        this.repoReleasesUsernameVar = releasesUsername;
        this.repoReleasesPasswordVar = releasesPassword;
        this.repoSnapshotsUrlVar = snapshotsUrl;
        this.repoSnapshotsName = snapshotsName;
        this.repoSnapshotsUsernameVar = snapshotsUsername;
        this.repoSnapshotsPasswordVar = snapshotsPassword;
    }

    public String getReleasesRepositoryUrlVar() {
        return repoReleasesUrlVar;
    }

    public String getReleasesRepositoryName() {
        return repoReleasesName;
    }

    public String getReleasesRepositoryUsernameVar() {
        return repoReleasesUsernameVar;
    }

    public String getReleasesRepositoryPasswordVar() {
        return repoReleasesPasswordVar;
    }

    public String getSnapshotsRepositoryUrlVar() {
        return repoSnapshotsUrlVar;
    }

    public String getSnapshotsRepositoryName() {
        return repoSnapshotsName;
    }

    public String getSnapshotsRepositoryUsernameVar() {
        return repoSnapshotsUsernameVar;
    }

    public String getSnapshotsRepositoryPasswordVar() {
        return repoSnapshotsPasswordVar;
    }


}