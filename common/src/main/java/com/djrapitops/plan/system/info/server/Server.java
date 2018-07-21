/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.server;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Server that is running Plan.
 *
 * @author Rsl1122
 */
public class Server implements Comparable<Server> {
    private final UUID uuid;
    private int id;
    private String name;
    private String webAddress;
    private int maxPlayers;

    public Server(int id, UUID uuid, String name, String webAddress, int maxPlayers) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
        this.maxPlayers = maxPlayers;
    }

    public int getId() {
        return id;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server that = (Server) o;
        return id == that.id &&
                Objects.equals(uuid, that.uuid) &&
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
                ", maxPlayers=" + maxPlayers +
                '}';
    }

    @Override
    public int compareTo(Server other) {
        return Integer.compare(this.id, other.id);
    }
}
