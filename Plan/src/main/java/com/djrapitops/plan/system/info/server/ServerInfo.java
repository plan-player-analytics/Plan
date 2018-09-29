/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
