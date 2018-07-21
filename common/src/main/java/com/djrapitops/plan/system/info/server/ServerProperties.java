package com.djrapitops.plan.system.info.server;

/**
 * Class responsible for holding server variable values that do not change
 * without a reload.
 *
 * @author Rsl1122
 * @since 3.4.1
 */
public interface ServerProperties {
    String getIp();

    String getName();

    int getPort();

    String getVersion();

    String getImplVersion();

    int getMaxPlayers();

    String getServerId();

    int getOnlinePlayers();
}
