/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
public class BukkitConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh = 0;

    private Server mainServer;
    private Set<Server> servers;

    public BukkitConnectionSystem() {
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) {
        return null;
    }

    @Override
    public boolean isMainServerAvailable() {
        return false;
    }

    @Override
    public Optional<String> getMainAddress() {
        return Optional.empty();
    }

    @Override
    public void enable() {
        RunnableFactory.createNew("Server List Update Task", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    Database database = Database.getActive();
                    Optional<Server> bungeeInformation = database.fetch().getBungeeInformation();
                    bungeeInformation.ifPresent(server -> mainServer = server);
                    servers = database.fetch().getBukkitServers();
                    latestServerMapRefresh = MiscUtils.getTime();
                } catch (DBException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        }).runTaskTimerAsynchronously(TimeAmount.SECOND.ticks() * 30L, TimeAmount.MINUTE.ticks() * 5L);
    }

    @Override
    public void disable() {

    }
}