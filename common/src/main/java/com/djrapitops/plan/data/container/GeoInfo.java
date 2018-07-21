/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.DateMap;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.SHA256Hash;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

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

    public GeoInfo(InetAddress address, String geolocation, long lastUsed)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this(FormatUtils.formatIP(address), geolocation, lastUsed, new SHA256Hash(address.getHostAddress()).create());
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
        return Objects.equals(ip, geoInfo.ip) &&
                Objects.equals(geolocation, geoInfo.geolocation) &&
                Objects.equals(ipHash, geoInfo.ipHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, geolocation, ipHash);
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