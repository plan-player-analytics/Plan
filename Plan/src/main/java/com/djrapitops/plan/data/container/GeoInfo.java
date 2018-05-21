/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.SHA256Hash;
import com.google.common.base.Objects;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Data class that contains information about IP and Geolocation.
 *
 * @author Rsl1122
 */
public class GeoInfo {

    private final String ip;
    private final String geolocation;
    private final String ipHash;
    private final long lastUsed;

    public GeoInfo(String ip, String geolocation, long lastUsed)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this(FormatUtils.formatIP(ip), geolocation, lastUsed, new SHA256Hash(ip).create());
    }

    public GeoInfo(String ip, String geolocation, long lastUsed, String ipHash) {
        this.ip = ip;
        this.geolocation = geolocation;
        this.lastUsed = lastUsed;
        this.ipHash = ipHash;
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

    public String getIpHash() {
        return ipHash;
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