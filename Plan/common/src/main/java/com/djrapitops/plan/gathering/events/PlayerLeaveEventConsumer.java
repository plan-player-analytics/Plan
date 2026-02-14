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
package com.djrapitops.plan.gathering.events;

import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.PlayerGatheringTasks;
import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.BanStatusTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PlayerLeaveEventConsumer {

    private final Processing processing;
    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final JoinAddressCache joinAddressCache;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;

    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final PlayerGatheringTasks playerGatheringTasks;

    @Inject
    public PlayerLeaveEventConsumer(Processing processing, PlanConfig config, DBSystem dbSystem, JoinAddressCache joinAddressCache, NicknameCache nicknameCache, SessionCache sessionCache, ExtensionSvc extensionService, Exporter exporter, PlayerGatheringTasks playerGatheringTasks) {
        this.processing = processing;
        this.config = config;
        this.dbSystem = dbSystem;
        this.joinAddressCache = joinAddressCache;
        this.nicknameCache = nicknameCache;
        this.sessionCache = sessionCache;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.playerGatheringTasks = playerGatheringTasks;
    }

    public void beforeLeave(PlayerLeave leave) {
        updatePlayerDataExtensionValues(leave);
    }

    public void onLeaveGameServer(PlayerLeave leave) {
        onLeaveGameServer(leave, 0);
    }

    public void onLeaveGameServer(PlayerLeave leave, int attempt) {
        Optional<ActiveSession> activeSession = SessionCache.getCachedSession(leave.getPlayerUUID());
        if (activeSession.isEmpty() && attempt < 50) {
            // Quit event processed before Join event, delay processing
            processing.submitNonCritical(() -> onLeaveGameServer(leave, attempt + 1));
            return;
        }

        endSession(leave).ifPresent(this::storeFinishedSession);
        storeBanStatus(leave);
        updateExport(leave);
        cleanFromCache(leave);
    }

    public void onLeaveProxyServer(PlayerLeave leave) {
        endSession(leave);
        updateExport(leave);
        cleanFromCache(leave);
    }

    private Optional<FinishedSession> endSession(PlayerLeave leave) {
        return sessionCache.endSession(leave.getPlayerUUID(), leave.getTime());
    }

    private void storeFinishedSession(FinishedSession finishedSession) {
        dbSystem.getDatabase().executeTransaction(new StoreSessionTransaction(finishedSession));
    }

    private void storeBanStatus(PlayerLeave leave) {
        processing.submitCritical(() -> leave.getPlayer().isBanned()
                .map(banStatus -> new BanStatusTransaction(leave.getPlayerUUID(), leave.getServerUUID(), banStatus))
                .ifPresent(dbSystem.getDatabase()::executeTransaction));
    }

    private void updatePlayerDataExtensionValues(PlayerLeave leave) {
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(
                leave.getPlayerUUID(), leave.getPlayerName(), CallEvents.PLAYER_LEAVE)
        );
    }

    private void updateExport(PlayerLeave leave) {
        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(leave.getPlayerUUID(), leave.getPlayerName()));
        }
    }

    private void cleanFromCache(PlayerLeave leave) {
        UUID playerUUID = leave.getPlayerUUID();
        nicknameCache.removeDisplayName(playerUUID);
        joinAddressCache.remove(playerUUID);
        playerGatheringTasks.unregister(playerUUID);
    }
}
