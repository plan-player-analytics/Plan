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
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

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

    private final PlanPlugin plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QInspectCommand(PlanPlugin plugin) {
        super("qinspect", Permissions.QUICK_INSPECT.getPermission(), CommandType.PLAYER_OR_ARGS);
        setArguments("<player>");
        setShortHelp(Locale.get(Msg.CMD_USG_QINSPECT).toString());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_QINSPECT).toArray());
        this.plugin = plugin;

    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);

        runInspectTask(playerName, sender);
    }

    private void runInspectTask(String playerName, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("InspectTask") {
            @Override
            public void run() {
                try {
                    UUID uuid = UUIDUtility.getUUIDOf(playerName);
                    if (uuid == null) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_VALID).toString());
                        return;
                    }

                    PlayerContainer container = Database.getActive().fetch().getPlayerContainer(uuid);
                    if (!container.getValue(PlayerKeys.REGISTERED).isPresent()) {
                        sender.sendMessage(Locale.get(Msg.CMD_FAIL_USERNAME_NOT_KNOWN).toString());
                        return;
                    }

                    sendMessages(sender, container);
                } catch (DBOpException e) {
                    if (e.isFatal()) {
                        sender.sendMessage("§cFatal database exception occurred: " + e.getMessage());
                    } else {
                        sender.sendMessage("§eNon-Fatal database exception occurred: " + e.getMessage());
                    }
                    Log.toLog(this.getClass(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void sendMessages(ISender sender, PlayerContainer player) {
        long now = System.currentTimeMillis();

        ColorScheme colorScheme = plugin.getColorScheme();

        String colM = colorScheme.getMainColor();
        String colS = colorScheme.getSecondaryColor();
        String colT = colorScheme.getTertiaryColor();
        Formatter<DateHolder> timestamp = Formatters.year();
        Formatter<Long> length = Formatters.timeAmount();

        sender.sendMessage(Locale.get(Msg.CMD_HEADER_INSPECT).toString() + ": " + colT + player.getValue(PlayerKeys.NAME).orElse("Unknown"));

        ActivityIndex activityIndex = player.getActivityIndex(now);
        Long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
        Long lastSeen = player.getValue(PlayerKeys.LAST_SEEN).orElse(0L);
        List<GeoInfo> geoInfo = player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>());
        Optional<GeoInfo> mostRecentGeoInfo = new GeoInfoMutator(geoInfo).mostRecent();
        String loginLocation = mostRecentGeoInfo.isPresent() ? mostRecentGeoInfo.get().getGeolocation() : "-";
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);

        sender.sendMessage(colM + "  Activity Index: " + colS + activityIndex.getFormattedValue() + " | " + activityIndex.getGroup());
        sender.sendMessage(colM + "  Registered: " + colS + timestamp.apply(() -> registered));
        sender.sendMessage(colM + "  Last Seen: " + colS + timestamp.apply(() -> lastSeen));
        sender.sendMessage(colM + "  Logged in from: " + colS + loginLocation);
        sender.sendMessage(colM + "  Playtime: " + colS + length.apply(sessionsMutator.toPlaytime()));
        sender.sendMessage(colM + "  Longest Session: " + colS + length.apply(sessionsMutator.toLongestSessionLength()));
        sender.sendMessage(colM + "  Times Kicked: " + colS + player.getValue(PlayerKeys.KICK_COUNT).orElse(0));
        sender.sendMessage("");
        sender.sendMessage(colM + "  Player Kills : " + colS + sessionsMutator.toPlayerKillCount());
        sender.sendMessage(colM + "  Mob Kills : " + colS + sessionsMutator.toMobKillCount());
        sender.sendMessage(colM + "  Deaths : " + colS + sessionsMutator.toDeathCount());

        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }
}