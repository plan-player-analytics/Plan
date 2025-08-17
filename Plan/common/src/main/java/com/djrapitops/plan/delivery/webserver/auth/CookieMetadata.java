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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.domain.auth.User;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class CookieMetadata {

    private final User user;
    private final String ipAddress;
    private long expires;

    public CookieMetadata(User user, long expires, String ipAddress) {
        this.user = user;
        this.expires = expires;
        this.ipAddress = ipAddress;
    }

    public User getUser() {
        return user;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CookieMetadata that = (CookieMetadata) o;
        return getExpires() == that.getExpires() && Objects.equals(getUser(), that.getUser()) && Objects.equals(getIpAddress(), that.getIpAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getExpires(), getIpAddress());
    }

    @Override
    public String toString() {
        return "CookieMetadata{" +
                "user=" + user +
                ", expires=" + expires +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
