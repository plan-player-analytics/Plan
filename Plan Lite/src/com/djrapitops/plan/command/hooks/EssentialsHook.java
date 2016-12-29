package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import java.util.HashMap;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import com.earth2me.essentials.craftbukkit.BanLookup;
import java.util.Optional;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import org.bukkit.BanList;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public class EssentialsHook implements Hook {

    private IEssentials ess;
    private final Plan plugin;

    public EssentialsHook(Plan p) throws Exception {
        this.ess = getPlugin(Essentials.class);
        this.plugin = p;
    }

    // Gets data with Essentials own User methods
    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            User user = this.ess.getOfflineUser(player);
            OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
            if (p.hasPlayedBefore()) {
                if (Optional.of(user).isPresent()) {
                    if (this.ess.getServer().getBanList(BanList.Type.IP).isBanned(player)
                            || BanLookup.isBanned(this.ess, player)) {
                        data.put("ESS-BANNED", new DataPoint("" + true, DataType.BOOLEAN));
                        data.put("ESS-BAN REASON", new DataPoint("" + BanLookup.getBanEntry(this.ess, player).getReason(), DataType.STRING));
                    }
                    if (user.isJailed()) {
                        data.put("ESS-JAILED", new DataPoint("" + true, DataType.BOOLEAN));
                        data.put("ESS-JAIL TIME", new DataPoint("" + user.getJailTimeout(), DataType.TIME_TIMESTAMP));
                    }
                    if (user.isMuted()) {
                        data.put("ESS-MUTED", new DataPoint("" + true, DataType.BOOLEAN));
                        data.put("ESS-MUTE TIME", new DataPoint("" + user.getMuteTimeout(), DataType.TIME_TIMESTAMP));
                    }
                    try {
                        if (user.isReachable()) {
                            Location loc = user.getLocation();
                            data.put("ESS-LOCATION WORLD", new DataPoint(loc.getWorld().getName(), DataType.STRING));
                            data.put("ESS-LOCATION", new DataPoint(" X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ(), DataType.LOCATION));
                        } else {
                            Location loc = user.getLogoutLocation();
                            data.put("ESS-LOCATION WORLD", new DataPoint(loc.getWorld().getName(), DataType.STRING));
                            data.put("ESS-LOCATION", new DataPoint("X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ(), DataType.LOCATION));
                        }
                    } catch (Exception e) {
                        this.plugin.logToFile("ESSENTIALSHOOK\n" + e + "\n" + e.getMessage());

                    }
                    data.put("ESS-NICKNAME", new DataPoint("" + user.getDisplayName(), DataType.STRING));
                    if (user.isReachable()) {
                        data.put("ESS-ONLINE SINCE", new DataPoint("" + user.getLastLogin(), DataType.TIME_TIMESTAMP));
                    } else {
                        data.put("ESS-OFFLINE SINCE", new DataPoint("" + user.getLastLogout(), DataType.TIME_TIMESTAMP));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        data.putAll(getData(player));
        try {
            OfflinePlayer p = getOfflinePlayer(UUIDFetcher.getUUIDOf(player));
            if (p.hasPlayedBefore()) {
                User user = this.ess.getOfflineUser(player);
                if (Optional.of(user).isPresent()) {
                    data.put("ESS-UUID", new DataPoint("" + user.getBase().getUniqueId().toString(), DataType.OTHER));
                    data.put("ESS-HEALTH", new DataPoint("" + user.getBase().getHealth(), DataType.AMOUNT));
                    data.put("ESS-HUNGER", new DataPoint("" + user.getBase().getFoodLevel(), DataType.AMOUNT));
                    data.put("ESS-XP LEVEL", new DataPoint("" + user.getBase().getLevel(), DataType.AMOUNT));
                    data.put("ESS-OPPED", new DataPoint("" + user.getBase().isOp(), DataType.BOOLEAN));
                    data.put("ESS-FLYING", new DataPoint("" + user.getBase().isFlying(), DataType.BOOLEAN));
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

}
