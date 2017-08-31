/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.server;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Server that is running Plan.
 *
 * @author Rsl1122
 */
public class ServerInfo {
    private final UUID uuid;
    private int id;
    private String name;
    private String webAddress;

    public ServerInfo(int id, UUID uuid, String name, String webAddress) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return id == that.id &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(webAddress, that.webAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, id, name, webAddress);
    }
}