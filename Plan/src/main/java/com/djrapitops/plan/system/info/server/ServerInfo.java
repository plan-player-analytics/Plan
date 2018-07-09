/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
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

    public static ServerInfo getInstance() {
        ServerInfo serverInfo = PlanSystem.getInstance().getServerInfo();
        Verify.nullCheck(serverInfo, () -> new IllegalStateException("ServerInfo was not initialized."));
        return serverInfo;
    }

    public static Server getServer() {
        return getInstance().server;
    }

    public static ServerProperties getServerProperties() {
        return getInstance().serverProperties;
    }

    public static UUID getServerUUID() {
        return getServer().getUuid();
    }

    public static String getServerName() {
        return getServer().getName();
    }

    public static int getServerID() {
        return getServer().getId();
    }

    @Override
    public void enable() throws EnableException {
        // ServerProperties are required when creating Server
        Verify.nullCheck(serverProperties, () -> new IllegalStateException("Server Properties did not load!"));
        server = loadServerInfo();
        Verify.nullCheck(server, () -> new IllegalStateException("Server information did not load!"));
    }

    protected abstract Server loadServerInfo() throws EnableException;

    @Override
    public void disable() {

    }
}
