/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.server;

import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Manages local server info file.
 * <p>
 * ServerInfo.yml contains current server's ID, UUID and Bungee WebServer connection information.
 * It
 *
 * @author Rsl1122
 */
public class ServerInfoFile extends Config {
    public ServerInfoFile(Plan plugin) throws IOException {
        this(plugin, new File(plugin.getDataFolder(), "ServerInfoFile.yml"));
    }

    ServerInfoFile(Plan plugin, File file) throws IOException {
        super(file, FileUtil.lines(file));
        List<String> defaults = new ArrayList<>();
            defaults.add("Server:");
            defaults.add("  UUID:");
            defaults.add("Bungee:");
            defaults.add("  WebAddress:");
            defaults.add("  Fail:");
        copyDefaults(defaults);
        save();
    }

    public void saveInfo(ServerInfo thisServer, ServerInfo bungee) throws IOException {
        Map<String, Serializable> serverMap = new HashMap<>();
        Map<String, Serializable> bungeeMap = new HashMap<>();

        serverMap.put("UUID", thisServer.getUuid().toString());
        set("Server", serverMap);

        String oldAddress = getString("Bungee.WebAddress");
        String newAddress = bungee.getWebAddress();

        if (!newAddress.equals(oldAddress)) {
            bungeeMap.put("Fail", 0);
            bungeeMap.put("WebAddress", newAddress);
            set("Bungee", bungeeMap);
        }
        save();
    }

    public Optional<UUID> getUUID() {
        String uuidString = getString("Server.UUID");
        if (Verify.isEmpty(uuidString)) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(uuidString));
    }

    public String getBungeeWebAddress() {
        return getString("Bungee.WebAddress");
    }

    public int markConnectionFail() {
        try {
            int fails = getInt("Bungee.Fail");
            set("Bungee.Fail", fails + 1);
            save();
            return fails;
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return -1;
    }

    public void resetConnectionFails() {
        try {
            set("Bungee.Fail", 0);
            save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}