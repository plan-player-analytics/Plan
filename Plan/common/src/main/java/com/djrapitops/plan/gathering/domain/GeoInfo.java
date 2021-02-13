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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.DateHolder;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data class that contains information about IP and Geolocation.
 *
 * @author AuroraLS3
 */
public class GeoInfo implements DateHolder, Serializable {

    private final String geolocation;
    private final long date;

    public GeoInfo(String geolocation, long lastUsed) {
        this.geolocation = geolocation;
        this.date = lastUsed;
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
        return Objects.equals(geolocation, geoInfo.geolocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geolocation);
    }

    @Override
    public String toString() {
        return "GeoInfo{" +
                "geolocation='" + geolocation + '\'' +
                ", date=" + date +
                '}';
    }
}