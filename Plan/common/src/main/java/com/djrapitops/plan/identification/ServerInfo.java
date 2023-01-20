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

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.identification.properties.ServerProperties;

import java.util.Optional;

/**
 * SubSystem for managing Server information.
 * <p>
 * Most information is accessible via static methods.
 *
 * @author AuroraLS3
 */
public abstract class ServerInfo implements SubSystem {

    protected Server server;
    protected final ServerProperties serverProperties;

    protected ServerInfo(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public Server getServer() {
        return server;
    }

    public ServerUUID getServerUUID() {
        return server.getUuid();
    }

    public ServerIdentifier getServerIdentifier() {
        return new ServerIdentifier(server.getUuid(), server.getIdentifiableName());
    }

    public Optional<ServerUUID> getServerUUIDSafe() {
        return Optional.ofNullable(server).map(Server::getUuid);
    }

    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    @Override
    public void enable() {
        loadServerInfo();
        if (server == null) throw new EnableException("Server information did not load!");
    }

    protected abstract void loadServerInfo();

    @Override
    public void disable() {

    }

    protected ServerUUID generateNewUUID() {
        return ServerUUID.randomUUID();
    }
}
