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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.ServerUUID;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.apache.commons.text.TextStringBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Thread that is run when JVM shuts down.
 * <p>
 * Saves active sessions to the Database (PlayerQuitEvent is not called)
 *
 * @author AuroraLS3
 */
@SuppressWarnings("unused")
@Singleton
public class ShutdownHook extends Thread {

    private static ShutdownHook activated;

    // Static variables to keep these classes loaded until JVM shutdown hook has run.
    private static final SessionCache sessionCache = new SessionCache();
    private static final ActiveSession activeSession = new ActiveSession(null, null, 0, null, null);
    private static final FinishedSession finishedSession = new FinishedSession(null, null, 0, 0, 0, null);
    private static final DataMap extraData = new DataMap();
    private static final WorldTimes worldTimes = new WorldTimes();
    private static final PlayerKills playerKills = new PlayerKills();
    private static final PlayerKill playerKill = new PlayerKill(null, null, null, null, 0);
    private static final ServerIdentifier serverIdentifier = new ServerIdentifier(ServerUUID.randomUUID(), (String) null);
    private static final MobKillCounter mobKillCounter = new MobKillCounter();
    private static final DeathCounter deathCounter = new DeathCounter();
    private static final TextStringBuilder textStringBuilder = new TextStringBuilder(0);

    private final ShutdownDataPreservation dataPreservation;

    @Inject
    public ShutdownHook(ShutdownDataPreservation dataPreservation) {
        this.dataPreservation = dataPreservation;
    }

    private static boolean isActivated() {
        return activated != null;
    }

    private static void activate(ShutdownHook hook) {
        activated = hook;
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private static void deactivate() {
        Runtime.getRuntime().removeShutdownHook(activated);
        activated = null;
    }

    public void register() {
        if (isActivated()) {
            deactivate();
        }
        activate(this);
    }

    @Override
    public void run() {
        dataPreservation.preserveSessionsInCache();
    }

    @Singleton
    public static class Registrar extends TaskSystem.Task {
        private final ShutdownHook shutdownHook;

        @Inject
        public Registrar(ShutdownHook shutdownHook) {this.shutdownHook = shutdownHook;}

        @Override
        public void run() {
            shutdownHook.register();
        }

        @Override
        public void register(RunnableFactory runnableFactory) {
            runnableFactory.create(this).runTaskAsynchronously();
        }
    }
}
