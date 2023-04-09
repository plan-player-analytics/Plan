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

/**
 * Represents a Server that is running Plan.
 *
 * @author AuroraLS3
 */
public class Server implements Comparable<Server> {
    private final ServerUUID uuid;
    private Integer id;
    private String name;
    private String webAddress;
    private boolean proxy;

    private final String planVersion;

    public Server(ServerUUID uuid, String name, String webAddress, String planVersion) {
        this(null, uuid, name, webAddress, false, planVersion);
    }

    public Server(Integer id, ServerUUID uuid, String name, String webAddress, boolean proxy, String planVersion) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
        this.proxy = proxy;
        this.planVersion = planVersion;
    }

    public Optional<Integer> getId() {
        return Optional.ofNullable(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public ServerUUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public static String getIdentifiableName(String name, int id, boolean proxy) {
        String serverPrefix = proxy ? "Proxy " : "Server ";
        return "Plan".equalsIgnoreCase(name) || "Proxy".equalsIgnoreCase(name)
                ? serverPrefix + id
                : name;
    }

    public String getIdentifiableName() {
        return getIdentifiableName(name, id, proxy);
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

    public String getPlanVersion() {
        return planVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server that = (Server) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(webAddress, that.webAddress) &&
                Objects.equals(planVersion, that.planVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, id, name, webAddress, planVersion);
    }

    @Override
    public String toString() {
        return "Server{" +
                "uuid=" + uuid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", webAddress='" + webAddress + '\'' +
                ", proxy=" + proxy +
                ", planVersion='" + planVersion + '\'' +
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
