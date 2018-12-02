/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.DateMap;
import com.djrapitops.plan.utilities.SHA256Hash;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

/**
 * Data class that contains information about IP and Geolocation.
 *
 * @author Rsl1122
 */
public class GeoInfo implements DateHolder, Serializable {

    private final String ip;
    private final String geolocation;
    private final String ipHash;
    private final long date;

    public GeoInfo(InetAddress address, String geolocation, long lastUsed) throws NoSuchAlgorithmException {
        this(formatIP(address), geolocation, lastUsed, new SHA256Hash(address.getHostAddress()).create());
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

    static String formatIP(InetAddress address) {
        String ip = address.getHostAddress();
        if ("localhost".equals(ip)) {
            return ip;
        }
        if (address instanceof Inet6Address) {
            StringBuilder b = new StringBuilder();
            int i = 0;
            for (String part : ip.split(":")) {
                if (i >= 3) {
                    break;
                }

                b.append(part).append(':');

                i++;
            }

            return b.append("xx..").toString();
        } else {
            StringBuilder b = new StringBuilder();
            int i = 0;
            for (String part : ip.split("\\.")) {
                if (i >= 2) {
                    break;
                }

                b.append(part).append('.');

                i++;
            }

            return b.append("xx.xx").toString();
        }
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