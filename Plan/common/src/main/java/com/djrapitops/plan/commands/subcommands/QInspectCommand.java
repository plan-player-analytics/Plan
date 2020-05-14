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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.domain.mutators.GeoInfoMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This command is used to cache UserInfo to InspectCache and display the link.
 *
 * @author Rsl1122
 */
@Singleton
public class QInspectCommand extends CommandNode {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final PlanConfig config;
    private final Processing processing;
    private final Formatters formatters;
    private final UUIDUtility uuidUtility;
    private final ErrorLogger errorLogger;

    @Inject
    public QInspectCommand(
            PlanConfig config,
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            UUIDUtility uuidUtility,
            Formatters formatters,
            ErrorLogger errorLogger
    ) {
        super("qinspect", Permissions.QUICK_INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        this.config = config;
        this.processing = processing;
        this.formatters = formatters;
        setArguments("<player>");

        this.locale = locale;
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.QINSPECT));
        setInDepthHelp(locale.getArray(DeepHelpLang.QINSPECT));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
            return;
        }

        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, Sender sender) {
        processing.submitNonCritical(() -> {
            try {
                UUID uuid = uuidUtility.getUUIDOf(playerName);
                if (uuid == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                PlayerContainer container = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid));
                if (!container.getValue(PlayerKeys.REGISTERED).isPresent()) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                sendMessages(sender, container);
            } catch (DBOpException e) {
                sender.sendMessage("§eDatabase exception occurred: " + e.getMessage());
                errorLogger.log(L.WARN, this.getClass(), e);
            }
        });
    }

    private void sendMessages(Sender sender, PlayerContainer player) {
        long now = System.currentTimeMillis();

        Formatter<DateHolder> timestamp = formatters.year();
        Formatter<Long> length = formatters.timeAmount();

        String playerName = player.getValue(PlayerKeys.NAME).orElse(locale.getString(GenericLang.UNKNOWN));

        ActivityIndex activityIndex = player.getActivityIndex(now, config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD));
        Long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
        Long lastSeen = player.getValue(PlayerKeys.LAST_SEEN).orElse(0L);
        List<GeoInfo> geoInfo = player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>());
        Optional<GeoInfo> mostRecentGeoInfo = new GeoInfoMutator(geoInfo).mostRecent();
        String geolocation = mostRecentGeoInfo.isPresent() ? mostRecentGeoInfo.get().getGeolocation() : "-";
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);

        String[] messages = new String[]{
                locale.getString(CommandLang.HEADER_INSPECT, playerName),
                locale.getString(CommandLang.QINSPECT_ACTIVITY_INDEX, activityIndex.getFormattedValue(formatters.decimals()), activityIndex.getGroup()),
                locale.getString(CommandLang.QINSPECT_REGISTERED, timestamp.apply(() -> registered)),
                locale.getString(CommandLang.QINSPECT_LAST_SEEN, timestamp.apply(() -> lastSeen)),
                locale.getString(CommandLang.QINSPECT_GEOLOCATION, geolocation),
                locale.getString(CommandLang.QINSPECT_TIMES_KICKED, player.getValue(PlayerKeys.KICK_COUNT).orElse(0)),
                "",
                locale.getString(CommandLang.QINSPECT_PLAYTIME, length.apply(sessionsMutator.toPlaytime())),
                locale.getString(CommandLang.QINSPECT_ACTIVE_PLAYTIME, length.apply(sessionsMutator.toActivePlaytime())),
                locale.getString(CommandLang.QINSPECT_AFK_PLAYTIME, length.apply(sessionsMutator.toAfkTime())),
                locale.getString(CommandLang.QINSPECT_LONGEST_SESSION, length.apply(sessionsMutator.toLongestSessionLength())),
                "",
                locale.getString(CommandLang.QINSPECT_PLAYER_KILLS, sessionsMutator.toPlayerKillCount()),
                locale.getString(CommandLang.QINSPECT_MOB_KILLS, sessionsMutator.toMobKillCount()),
                locale.getString(CommandLang.QINSPECT_DEATHS, sessionsMutator.toDeathCount()),
                ">"
        };
        sender.sendMessage(messages);
    }
}