/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;

/**
 * SubSystem for managing Server information.
 * <p>
 * Most information is accessible via static methods.
 *
 * @author Rsl1122
 */
public abstract class ServerInfo implements SubSystem {

    protected Server server;
    protected ServerProperties serverProperties;

    public ServerInfo(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public Server getServer() {
        return server;
    }

    public UUID getServerUUID() {
        return getServer().getUuid();
    }

    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    @Override
    public void enable() throws EnableException {
        server = loadServerInfo();
        Verify.nullCheck(server, () -> new EnableException("Server information did not load!"));
    }

    protected abstract Server loadServerInfo() throws EnableException;

    @Override
    public void disable() {

    }
}
