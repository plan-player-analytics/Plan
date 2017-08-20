/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.server;

import com.djrapitops.plugin.config.BukkitConfig;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages local server info file.
 * <p>
 * ServerInfo.yml contains current server's ID, UUID & Bungee WebServer connection information.
 * It
 *
 * @author Rsl1122
 */
public class ServerInfoFile extends BukkitConfig<Plan> {
    public ServerInfoFile(Plan plugin) throws IOException, InvalidConfigurationException {
        super(plugin, "ServerInfo");
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        config.addDefault("Server.ID", "-1");
        config.addDefault("Server.UUID", "");
        config.addDefault("Bungee.WebAddress", "");
        config.addDefault("Bungee.Port", -1);
        config.addDefault("Bungee.Fail", 0);
        save();
    }

    public void saveInfo(ServerInfo thisServer, ServerInfo bungee) throws IOException {
        Map<String, Serializable> serverMap = new HashMap<>();
        Map<String, Serializable> bungeeMap = new HashMap<>();

        serverMap.put("ID", thisServer.getId());
        serverMap.put("UUID", thisServer.getUuid().toString());

        bungeeMap.put("WebAddress", bungee.getWebAddress());
        bungeeMap.put("Port", bungee.getPort());

        getConfig().set("Server", serverMap);
        getConfig().set("Bungee", bungeeMap);
        save();
    }

    public int getID() {
        return getConfig().getInt("Server.ID");
    }

    public UUID getUUID() {
        String uuidString = getConfig().getString("Server.UUID");
        if (uuidString == null) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    public String getBungeeWebAddress() {
        return getConfig().getString("Bungee.WebAddress");
    }

    public int getBungeePort() {
        return getConfig().getInt("Bungee.Port");
    }

    public void markConnectionFail() throws IOException {
        FileConfiguration config = getConfig();
        int fails = config.getInt("Bungee.Fail");
        config.set("Bungee.Fail", fails + 1);
        save();
    }
}