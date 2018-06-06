/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.DateMap;
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
public class GeoInfo implements DateHolder {

    private final String ip;
    private final String geolocation;
    private final String ipHash;
    private final long date;

    public GeoInfo(String ip, String geolocation, long date)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this(FormatUtils.formatIP(ip), geolocation, date, new SHA256Hash(ip).create());
    }

    public GeoInfo(String ip, String geolocation, long date, String ipHash) {
        this.ip = ip;
        this.geolocation = geolocation;
        this.date = date;
        this.ipHash = ipHash;
    }

    public static DateMap<GeoInfo> intoDateMap(Iterable<GeoInfo> geoInfo) {
        DateMap<GeoInfo> map = new DateMap<>();
        for (GeoInfo info : geoInfo) {
            map.put(info.date, info);
        }
        return map;
    }

    public String getIp() {
        return ip;
    }

    public String getGeolocation() {
        return geolocation;
    }

    @Override
    public long getDate() {
        return date;
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
                Objects.equal(geolocation, geoInfo.geolocation) &&
                Objects.equal(ipHash, geoInfo.ipHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, geolocation, ipHash);
    }

    @Override
    public String toString() {
        return "GeoInfo{" +
                "ip='" + ip + '\'' +
                ", geolocation='" + geolocation + '\'' +
                ", ipHash='" + ipHash + '\'' +
                ", date=" + date +
                '}';
    }
}