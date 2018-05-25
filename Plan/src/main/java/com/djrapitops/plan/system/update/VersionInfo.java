package com.djrapitops.plan.system.update;

import com.djrapitops.plugin.api.utility.Version;
import com.google.common.base.Objects;

/**
 * Data class for reading version.txt in https://github.com/Rsl1122/Plan-PlayerAnalytics.
 *
 * @author Rsl1122
 */
public class VersionInfo implements Comparable<VersionInfo> {

    private final boolean release;
    private final Version version;
    private final String downloadUrl;
    private final String changeLogUrl;

    public VersionInfo(boolean release, Version version, String downloadUrl, String changeLogUrl) {
        this.release = release;
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.changeLogUrl = changeLogUrl;
    }

    public boolean isRelease() {
        return release;
    }

    public Version getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getChangeLogUrl() {
        return changeLogUrl;
    }

    public boolean isTrusted() {
        return downloadUrl.startsWith("https://github.com/Rsl1122/Plan-PlayerAnalytics/releases/download/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return release == that.release &&
                Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(release, version);
    }

    @Override
    public int compareTo(VersionInfo o) {
        return o.version.compareTo(this.version);
    }
}