package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.UUIDFetcher;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.ServerData;

import java.util.Date;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class InspectCommand extends SubCommand {

    private Plan plugin;
    private InspectCacheHandler inspectCache;

    public InspectCommand(Plan plugin) {
        super("inspect", "plan.inspect", "Inspect data /plan <player> [-a, -r].", CommandType.CONSOLE_WITH_ARGUMENTS);

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerDisplayname(args, sender);

        UUID uuid;
        try {
            uuid = UUIDFetcher.getUUIDOf(playerName);
            if (uuid == null) {
                throw new Exception("Username doesn't exist.");
            }
        } catch (Exception e) {
            sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
            return true;
        }
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (!p.hasPlayedBefore()) {
            sender.sendMessage(Phrase.USERNAME_NOT_SEEN.toString());
            return true;
        }
        if (!plugin.getDB().wasSeenBefore(uuid)) {
            sender.sendMessage(Phrase.USERNAME_NOT_KNOWN.toString());
            return true;
        }

        Date refreshDate = new Date();
        inspectCache.cache(uuid);
        UserData data = inspectCache.getFromCache(uuid);

        ChatColor operatorColor = Phrase.COLOR_MAIN.color();
        ChatColor textColor = Phrase.COLOR_SEC.color();

        List<String> msgs = new ArrayList<>();
        msgs.add("Logintimes " + data.getLoginTimes());
        msgs.add("BedLocation " + data.getBedLocation().getBlockX());
        msgs.add("GeoLoc " + data.getDemData().getGeoLocation());
        msgs.add("GMTimes values " + data.getGmTimes().values().toString());
        msgs.add("Ips " + data.getIps().toString());
        msgs.add("Last gamemode " + data.getLastGamemode());
        msgs.add("Last gm swap time " + data.getLastGmSwapTime());
        msgs.add("Last Played " + data.getLastPlayed());
        msgs.add("Location " + data.getLocation().getBlockX());
        msgs.add("Locations "+data.getLocations().size());
        msgs.add("Nicknames " + data.getNicknames().toString());
        msgs.add("Registered " + data.getRegistered());
        msgs.add("TimesKicked " + data.getTimesKicked());
        msgs.add("Uuid " + data.getUuid());
        msgs.add("PlayTime " + data.getPlayTime());
        msgs.add("Banned "+ data.isBanned());
        msgs.add("Opped" + data.isOp());
        msgs.add(operatorColor + "SERVER");
        ServerData sdata = plugin.getHandler().getServerData();
        msgs.add("Commands " + sdata.getCommandUsage().keySet().toString());
        msgs.add("New Players " + sdata.getNewPlayers());
        msgs.add("Online Players " + sdata.getPlayersOnline());
        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Inspect results: " + playerName + " - took " + FormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + textColor + "] --");

        for (String message : msgs) {
            sender.sendMessage(textColor + message);
        }

        sender.sendMessage(textColor + "-- o --");
        return true;
    }
}
