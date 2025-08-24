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
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class ServerFileLoader implements ServerLoader {

    private final String currentVersion;
    private final PlanFiles files;
    private final PlanConfig config;

    private final AtomicReference<Config> serverInfoConfig = new AtomicReference<>();

    @Inject
    public ServerFileLoader(
            @Named("currentVersion") String currentVersion,
            PlanFiles files,
            PlanConfig config
    ) {
        this.currentVersion = currentVersion;
        this.files = files;
        this.config = config;
    }


    private boolean isNotPrepared() {
        return serverInfoConfig.get() == null;
    }

    private void prepare() throws IOException {
        serverInfoConfig.set(new Config(
                files.getFileFromPluginFolder("ServerInfoFile.yml"),
                readDefaults()
        ));
    }

    private Config readDefaults() throws IOException {
        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar("DefaultServerInfoFile.yml").asInputStream())) {
            return reader.read();
        }
    }

    @Override
    public Optional<Server> load(ServerUUID loaded) {
        try {
            if (isNotPrepared()) prepare();

            String serverUUIDString = serverInfoConfig.get().getString("Server.UUID");
            if (serverUUIDString == null) return Optional.empty();

            Integer id = serverInfoConfig.get().getInteger("Server.ID");
            ServerUUID serverUUID = ServerUUID.fromString(serverUUIDString);
            String name = config.getNode(PluginSettings.SERVER_NAME.getPath())
                    .map(ConfigNode::getString)
                    .orElse("Plan");
            String address = serverInfoConfig.get().getString("Server.Web_address");

            return Optional.of(new Server(id, serverUUID, name, address, false, currentVersion));
        } catch (IOException e) {
            throw new EnableException("Failed to read ServerInfoFile.yml: " + e.getMessage());
        }
    }

    @Override
    public void save(Server server) {
        try {
            if (isNotPrepared()) prepare();

            server.getId().ifPresent(id -> serverInfoConfig.get().set("Server.ID", id));
            serverInfoConfig.get().set("Server.UUID", server.getUuid().toString());
            serverInfoConfig.get().set("Server.Web_address", server.getWebAddress());

            serverInfoConfig.get().save();
        } catch (IOException e) {
            throw new EnableException("Failed to write ServerInfoFile.yml: " + e.getMessage(), e);
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
