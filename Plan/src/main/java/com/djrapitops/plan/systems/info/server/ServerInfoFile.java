/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.server;

import com.djrapitops.plugin.config.BukkitConfig;
import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages local server info file.
 * <p>
 * ServerInfo.yml contains current server's ID, UUID & Bungee WebServer connection information.
 * It
 *
 * @author Rsl1122
 */
public class ServerInfoFile extends BukkitConfig {
    public ServerInfoFile(Plan plugin) throws IOException {
        super(plugin, "ServerInfoFile.yml");
        IFileConfig config = super.getConfig();
        config.copyDefaults();
        config.addDefault("Server.UUID", "");
        config.addDefault("Bungee.WebAddress", "");
        config.addDefault("Bungee.Fail", 0);
        save();
    }

    public void saveInfo(ServerInfo thisServer, ServerInfo bungee) throws IOException {
        IFileConfig config = getConfig();
        Map<String, Serializable> serverMap = new HashMap<>();
        Map<String, Serializable> bungeeMap = new HashMap<>();

        serverMap.put("UUID", thisServer.getUuid().toString());
        config.set("Server", serverMap);

        String oldAddress = config.getString("Bungee.WebAddress");
        String newAddress = bungee.getWebAddress();

        if (!newAddress.equals(oldAddress)) {
            bungeeMap.put("Fail", 0);
            bungeeMap.put("WebAddress", newAddress);
            config.set("Bungee", bungeeMap);
        }
        save();
    }

    public Optional<UUID> getUUID() {
        String uuidString = getConfig().getString("Server.UUID");
        if (Verify.isEmpty(uuidString)) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(uuidString));
    }

    public String getBungeeWebAddress() {
        return getConfig().getString("Bungee.WebAddress");
    }

    public void markConnectionFail() {
        try {
            IFileConfig config = getConfig();
            int fails = config.getInt("Bungee.Fail");
            config.set("Bungee.Fail", fails + 1);
            save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}