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
package com.djrapitops.plan.identification;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a Server that is running Plan.
 *
 * @author Rsl1122
 */
public class Server implements Comparable<Server> {
    private final UUID uuid;
    private Integer id;
    private String name;
    private String webAddress;
    private boolean proxy;

    public Server(UUID uuid, String name, String webAddress) {
        this(null, uuid, name, webAddress, false);
    }

    public Server(Integer id, UUID uuid, String name, String webAddress, boolean proxy) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
        this.proxy = proxy;
    }

    public Optional<Integer> getId() {
        return Optional.ofNullable(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIdentifiableName() {
        return !"Plan".equalsIgnoreCase(name) ? name : "Server " + id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server that = (Server) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(webAddress, that.webAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, id, name, webAddress);
    }

    @Override
    public String toString() {
        return "Server{" +
                "uuid=" + uuid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", webAddress='" + webAddress + '\'' +
                '}';
    }

    @Override
    public int compareTo(Server other) {
        return Integer.compare(this.id, other.id);
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isNotProxy() {
        return !isProxy();
    }

}
