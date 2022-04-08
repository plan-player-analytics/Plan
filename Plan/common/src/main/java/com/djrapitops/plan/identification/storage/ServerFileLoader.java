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
package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.storage.file.PlanFiles;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class ServerFileLoader extends Config implements ServerLoader {

    private final String currentVersion;
    private final PlanFiles files;
    private final PlanConfig config;

    private boolean prepared;

    @Inject
    public ServerFileLoader(
            @Named("currentVersion") String currentVersion,
            PlanFiles files,
            PlanConfig config
    ) {
        super(files.getFileFromPluginFolder("ServerInfoFile.yml"));
        this.currentVersion = currentVersion;
        this.files = files;
        this.config = config;

        prepared = false;
    }

    public void prepare() throws IOException {
        read();
        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar("DefaultServerInfoFile.yml").asInputStream())) {
            copyMissing(reader.read());
        }
        save();
        prepared = true;
    }

    @Override
    public Optional<Server> load(ServerUUID loaded) {
        try {
            if (!prepared) prepare();

            String serverUUIDString = getString("Server.UUID");
            if (serverUUIDString == null) return Optional.empty();

            Integer id = getInteger("Server.ID");
            ServerUUID serverUUID = ServerUUID.fromString(serverUUIDString);
            String name = config.getNode(PluginSettings.SERVER_NAME.getPath())
                    .map(ConfigNode::getString)
                    .orElse("Proxy");
            String address = getString("Server.Web_address");

            return Optional.of(new Server(id, serverUUID, name, address, false, currentVersion));
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml: " + e.getMessage());
        }
    }

    @Override
    public void save(Server server) {
        try {
            if (!prepared) prepare();

            server.getId().ifPresent(id -> set("Server.ID", id));
            set("Server.UUID", server.getUuid());
            set("Server.Web_address", server.getWebAddress());

            save();
        } catch (IOException e) {
            throw new EnableException("Failed to write ServerInfoFile.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ServerFileLoader that = (ServerFileLoader) o;
        return Objects.equals(files, that.files) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), files, config);
    }
}
