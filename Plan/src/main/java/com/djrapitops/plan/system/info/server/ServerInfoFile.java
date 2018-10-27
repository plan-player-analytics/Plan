/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plugin.config.Config;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
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

    private final PlanFiles files;

    @Inject
    public ServerInfoFile(PlanFiles files) {
        super(files.getFileFromPluginFolder("ServerInfoFile.yml"));
        this.files = files;
    }

    public void prepare() throws IOException {
        copyDefaults(files.readFromResource("DefaultServerInfoFile.yml"));
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

}
