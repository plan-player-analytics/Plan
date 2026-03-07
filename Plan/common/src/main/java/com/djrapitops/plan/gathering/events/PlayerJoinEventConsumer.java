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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.gathering.JoinAddressValidator;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.gathering.timed.PlayerExtensionDataUpdateTask;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class PlayerJoinEventConsumer {

    private final Processing processing;
    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final JoinAddressValidator joinAddressValidator;
    private final GeolocationCache geolocationCache;
    private final SessionCache sessionCache;
    private final NicknameCache nicknameCache;

    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final PlayerExtensionDataUpdateTask.Factory playerExtensionUpdateTaskFactory;

    @Inject
    public PlayerJoinEventConsumer(
            Processing processing,
            PlanConfig config,
            DBSystem dbSystem, JoinAddressValidator joinAddressValidator,
            GeolocationCache geolocationCache,
            SessionCache sessionCache,
            NicknameCache nicknameCache,
            ExtensionSvc extensionService,
            Exporter exporter,
            PlayerExtensionDataUpdateTask.Factory playerExtensionUpdateTaskFactory
    ) {
        this.processing = processing;
        this.config = config;
        this.dbSystem = dbSystem;
        this.joinAddressValidator = joinAddressValidator;
        this.geolocationCache = geolocationCache;
        this.sessionCache = sessionCache;
        this.nicknameCache = nicknameCache;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.playerExtensionUpdateTaskFactory = playerExtensionUpdateTaskFactory;
    }

    private static long getRegisterDate(PlayerJoin join) {
        long registerDate = join.getPlayer().getRegisterDate().orElseGet(join::getTime);
        // Correct incorrect register dates https://github.com/plan-player-analytics/Plan/issues/2934
        if (registerDate < System.currentTimeMillis() / 1000) {
            registerDate = registerDate * 1000;
        }
        return registerDate;
    }

    public void onJoinGameServer(PlayerJoin join) {
        Optional<FinishedSession> interruptedSession = cacheActiveSession(join);
        processing.submitCritical(() -> {
            storeWorldInformation(join);
            storeGamePlayer(join)
                    .thenRunAsync(() -> {
                        storeJoinAddress(join);
                        interruptedSession.ifPresent(this::storeInterruptedSession);
                        storeGeolocation(join);
                        storeOperatorStatus(join);
                        storeNickname(join);
                        updatePlayerDataExtensionValues(join);
                        updateExport(join);
                        registerExtensionUpdateTask(join);
                    }, processing.getCriticalExecutor());
        });
    }

    private void registerExtensionUpdateTask(PlayerJoin join) {
        playerExtensionUpdateTaskFactory.register(Parameters.player(join.getServer().getUuid(), join.getPlayerUUID(), join.getPlayer().getName()));
    }

    public void onJoinProxyServer(PlayerJoin join) {
        cacheActiveSession(join);
        processing.submitCritical(() -> storeProxyPlayer(join)
                .thenRunAsync(() -> {
                    storeGeolocation(join);
                    updatePlayerDataExtensionValues(join);
                    updateExport(join);
                }, processing.getCriticalExecutor())
        );
    }

    private void storeJoinAddress(PlayerJoin join) {
        join.getPlayer().getJoinAddress()
                .map(joinAddressValidator::sanitize)
                .filter(joinAddressValidator::isValid)
                .map(StoreJoinAddressTransaction::new)
                .ifPresent(dbSystem.getDatabase()::executeTransaction);
    }

    private void storeGeolocation(PlayerJoin join) {
        if (config.isTrue(DataGatheringSettings.GEOLOCATIONS) && geolocationCache.canGeolocate()) {
            join.getPlayer().getIPAddress()
                    .map(ip -> new StoreGeoInfoTransaction(join.getPlayerUUID(), ip, join.getTime(), geolocationCache::getCountry))
                    .ifPresent(dbSystem.getDatabase()::executeTransaction);
        }
    }

    private void storeWorldInformation(PlayerJoin join) {
        ServerUUID serverUUID = join.getServerUUID();
        join.getPlayer().getCurrentWorld()
                .map(world -> new StoreWorldNameTransaction(serverUUID, world))
                .ifPresent(dbSystem.getDatabase()::executeTransaction);
    }

    private CompletableFuture<?> storeGamePlayer(PlayerJoin join) {
        long registerDate = getRegisterDate(join);
        String joinAddress = join.getPlayer().getJoinAddress()
                .map(joinAddressValidator::sanitize)
                .filter(joinAddressValidator::isValid)
                .orElse(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Transaction transaction = new StoreServerPlayerTransaction(
                join.getPlayerUUID(), registerDate, join.getPlayer().getName(), join.getServerUUID(), joinAddress
        );
        return dbSystem.getDatabase().executeTransaction(transaction);
    }

    private CompletableFuture<?> storeProxyPlayer(PlayerJoin join) {
        Transaction transaction = new PlayerRegisterTransaction(
                join.getPlayerUUID(), join::getTime, join.getPlayer().getName()
        );
        return dbSystem.getDatabase().executeTransaction(transaction);
    }

    private void storeOperatorStatus(PlayerJoin join) {
        join.getPlayer().isOperator()
                .map(opStatus -> new OperatorStatusTransaction(join.getPlayerUUID(), join.getServerUUID(), opStatus))
                .ifPresent(dbSystem.getDatabase()::executeTransaction);
    }

    Optional<FinishedSession> cacheActiveSession(PlayerJoin join) {
        ActiveSession session = mapToActiveSession(join);
        return sessionCache.cacheSession(join.getPlayerUUID(), session);
    }

    private void storeInterruptedSession(FinishedSession finishedSession) {
        dbSystem.getDatabase().executeTransaction(new StoreSessionTransaction(finishedSession));
    }

    private ActiveSession mapToActiveSession(PlayerJoin join) {
        String joinAddress = join.getPlayer().getJoinAddress()
                .map(joinAddressValidator::sanitize)
                .filter(joinAddressValidator::isValid)
                .orElse(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        ActiveSession session = new ActiveSession(join.getPlayerUUID(), join.getServerUUID(), join.getTime(),
                join.getPlayer().getCurrentWorld().orElse(null),
                join.getPlayer().getCurrentGameMode().orElse(null));
        session.getExtraData().put(PlayerName.class, new PlayerName(join.getPlayer().getName()));
        session.getExtraData().put(ServerName.class, new ServerName(join.getServer().isProxy() ? join.getServer().getName() : "Proxy Server"));
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        return session;
    }

    private void storeNickname(PlayerJoin join) {
        join.getPlayer().getDisplayName()
                .map(displayName -> new Nickname(displayName, join.getTime(), join.getServerUUID()))
                .map(nickname -> new StoreNicknameTransaction(
                        join.getPlayerUUID(), nickname,
                        (uuid, name) -> nicknameCache.getDisplayName(join.getPlayerUUID())
                                .map(name::equals)
                                .orElse(false)))
                .ifPresent(dbSystem.getDatabase()::executeTransaction);
    }

    private void updatePlayerDataExtensionValues(PlayerJoin join) {
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(
                join.getPlayerUUID(), join.getPlayerName(), CallEvents.PLAYER_JOIN)
        );
    }

    void updateExport(PlayerJoin join) {
        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(join.getPlayerUUID(), join.getPlayerName()));
        }
    }

}
