/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.server;

import java.util.UUID;

/**
 * Represents a Server that is running Plan.
 *
 * @author Rsl1122
 */
public class ServerInfo {
    private final int id;
    private final UUID uuid;
    private String name;
    private String webAddress;
    private int port;

    public ServerInfo(int id, UUID uuid, String name, String webAddress, int port) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.webAddress = webAddress;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}