package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.GeoInfoMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.GenericLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This command is used to cache UserInfo to InspectCache and display the link.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class QInspectCommand extends CommandNode {

    private final Locale locale;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QInspectCommand(PlanPlugin plugin) {
        super("qinspect", Permissions.QUICK_INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        setArguments("<player>");

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.QINSPECT));
        setInDepthHelp(locale.getArray(DeepHelpLang.QINSPECT));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
            return;
        }

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, ISender sender) {
        Processing.submitNonCritical(() -> {
            try {
                UUID uuid = UUIDUtility.getUUIDOf(playerName);
                if (uuid == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                PlayerContainer container = Database.getActive().fetch().getPlayerContainer(uuid);
                if (!container.getValue(PlayerKeys.REGISTERED).isPresent()) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                sendMessages(sender, container);
            } catch (DBOpException e) {
                sender.sendMessage("§eDatabase exception occurred: " + e.getMessage());
                Log.toLog(this.getClass(), e);
            }
        });
    }

    private void sendMessages(ISender sender, PlayerContainer player) {
        long now = System.currentTimeMillis();

        Formatter<DateHolder> timestamp = Formatters.year();
        Formatter<Long> length = Formatters.timeAmount();

        String playerName = player.getValue(PlayerKeys.NAME).orElse(locale.getString(GenericLang.UNKNOWN));

        ActivityIndex activityIndex = player.getActivityIndex(now);
        Long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
        Long lastSeen = player.getValue(PlayerKeys.LAST_SEEN).orElse(0L);
        List<GeoInfo> geoInfo = player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>());
        Optional<GeoInfo> mostRecentGeoInfo = new GeoInfoMutator(geoInfo).mostRecent();
        String geolocation = mostRecentGeoInfo.isPresent() ? mostRecentGeoInfo.get().getGeolocation() : "-";
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);

        String[] messages = new String[]{
                locale.getString(CommandLang.HEADER_INSPECT, playerName),
                locale.getString(CommandLang.QINSPECT_ACTIVITY_INDEX, activityIndex.getFormattedValue(), activityIndex.getGroup()),
                locale.getString(CommandLang.QINSPECT_REGISTERED, timestamp.apply(() -> registered)),
                locale.getString(CommandLang.QINSPECT_LAST_SEEN, timestamp.apply(() -> lastSeen)),
                locale.getString(CommandLang.QINSPECT_GEOLOCATION, geolocation),
                locale.getString(CommandLang.QINSPECT_PLAYTIME, length.apply(sessionsMutator.toPlaytime())),
                locale.getString(CommandLang.QINSPECT_LONGEST_SESSION, length.apply(sessionsMutator.toLongestSessionLength())),
                locale.getString(CommandLang.QINSPECT_TIMES_KICKED, player.getValue(PlayerKeys.KICK_COUNT).orElse(0)),
                "",
                locale.getString(CommandLang.QINSPECT_PLAYER_KILLS, sessionsMutator.toPlayerKillCount()),
                locale.getString(CommandLang.QINSPECT_MOB_KILLS, sessionsMutator.toMobKillCount()),
                locale.getString(CommandLang.QINSPECT_DEATHS, sessionsMutator.toDeathCount()),
                ">"
        };
        sender.sendMessage(messages);
    }
}