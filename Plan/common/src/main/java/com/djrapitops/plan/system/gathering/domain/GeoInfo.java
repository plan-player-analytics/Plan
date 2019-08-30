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
package com.djrapitops.plan.system.gathering.domain;

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.DateMap;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Objects;

/**
 * Data class that contains information about IP and Geolocation.
 *
 * @author Rsl1122
 */
public class GeoInfo implements DateHolder, Serializable {

    private final String ip;
    private final String geolocation;
    private final long date;

    public GeoInfo(InetAddress address, String geolocation, long lastUsed) {
        this(formatIP(address), geolocation, lastUsed);
    }

    public GeoInfo(String ip, String geolocation, long date) {
        this.ip = ip;
        this.geolocation = geolocation;
        this.date = date;
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
            for (String part : StringUtils.split(ip, ':')) {
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
            for (String part : StringUtils.split(ip, '.')) {
                if (i >= 2) {
                    break;
                }

                b.append(part).append('.');

                i++;
            }

            return b.append("xx.xx").toString();
        }
    }

    @Deprecated
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoInfo geoInfo = (GeoInfo) o;
        return Objects.equals(ip, geoInfo.ip) &&
                Objects.equals(geolocation, geoInfo.geolocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, geolocation);
    }

    @Override
    public String toString() {
        return "GeoInfo{" +
                "ip='" + ip + '\'' +
                ", geolocation='" + geolocation + '\'' +
                ", date=" + date +
                '}';
    }
}