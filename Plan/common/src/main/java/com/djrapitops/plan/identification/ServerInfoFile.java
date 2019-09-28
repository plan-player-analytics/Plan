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

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.storage.file.PlanFiles;
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
        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar("DefaultServerInfoFile.yml").asInputStream())) {
            copyMissing(reader.read());
        }
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
