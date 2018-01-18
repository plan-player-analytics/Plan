/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages local server info file.
 * <p>
 * Server.yml contains current server's ID, UUID and Bungee WebServer connection information.
 * It
 *
 * @author Rsl1122
 */
public class ServerInfoFile extends Config {
    public ServerInfoFile(File dataFolder) throws IOException {
        super(new File(dataFolder, "ServerInfoFile.yml"));
        copyDefaults(FileSystem.readFromResource("DefaultServerInfoFile.yml"));
        save();
    }

    public void saveServerUUID(UUID serverUUID) throws IOException {
        set("Server.UUID", serverUUID.toString());
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