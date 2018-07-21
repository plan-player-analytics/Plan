package com.djrapitops.plan.data.store.objects;

import java.util.Objects;
import java.util.UUID;

/**
 * Object storing nickname information.
 *
 * @author Rsl1122
 */
public class Nickname implements DateHolder {

    private final String name;
    private final long date;
    private final UUID serverUUID;

    public Nickname(String name, long date, UUID serverUUID) {
        this.name = name;
        this.date = date;
        this.serverUUID = serverUUID;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getDate() {
        return date;
    }

    public UUID getServerUUID() {
        return serverUUID;
    }

    @Override
    public String toString() {
        return "Nickname{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", serverUUID=" + serverUUID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nickname)) return false;
        Nickname nickname = (Nickname) o;
        return Objects.equals(name, nickname.name) &&
                Objects.equals(serverUUID, nickname.serverUUID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, date, serverUUID);
    }
}