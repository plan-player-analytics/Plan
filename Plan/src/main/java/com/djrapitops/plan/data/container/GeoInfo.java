/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.HasDate;
import com.google.common.base.Objects;

/**
 * Data class that contains information about IP and Geolocation.
 *
 * @author Rsl1122
 */
public class GeoInfo implements HasDate {

    private final String ip;
    private final String geolocation;
    private final long lastUsed;

    public GeoInfo(String ip, String geolocation, long lastUsed) {
        this.ip = ip;
        this.geolocation = geolocation;
        this.lastUsed = lastUsed;
    }

    public String getIp() {
        return ip;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    @Override
    public long getDate() {
        return getLastUsed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoInfo geoInfo = (GeoInfo) o;
        return Objects.equal(ip, geoInfo.ip) &&
                Objects.equal(geolocation, geoInfo.geolocation);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, geolocation);
    }
}