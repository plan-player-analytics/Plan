package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import java.util.HashMap;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import com.earth2me.essentials.craftbukkit.BanLookup;
import com.gmail.nossr50.util.uuid.UUIDFetcher;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import org.bukkit.BanList;
import org.bukkit.Location;

public class EssentialsHook implements Hook {

    private IEssentials ess;
    private final Plan plugin;

    public EssentialsHook(Plan p) throws Exception {
        this.ess = getPlugin(Essentials.class);
        this.plugin = p;
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        User user = this.ess.getOfflineUser(player);
        if (user != null) {
            if (this.ess.getServer().getBanList(BanList.Type.IP).isBanned(player)
                    || BanLookup.isBanned(this.ess, player)) {
                data.put("ESS-BANNED", "" + true);
                data.put("ESS-BAN REASON", "" + BanLookup.getBanEntry(this.ess, player).getReason());
            }
            if (user.isJailed()) {
                data.put("ESS-JAILED", "" + true);
                data.put("ESS-JAIL TIME", "" + user.getJailTimeout());
            }
            if (user.isMuted()) {
                data.put("ESS-MUTED", "" + true);
                data.put("ESS-MUTE TIME", "" + user.getMuteTimeout());
            }
            try {
                if (user.isReachable()) {
                    Location loc = user.getLocation();
                    data.put("ESS-LOCATION WORLD", loc.getWorld().getName());
                    data.put("ESS-LOCATION", " X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
                } else {
                    Location loc = user.getLogoutLocation();
                    data.put("ESS-LOCATION WORLD", loc.getWorld().getName());
                    data.put("ESS-LOCATION", "X:" + loc.getBlockX() + " Y:" + loc.getBlockY() + " Z:" + loc.getBlockZ());
                }
            } catch (Exception e) {
                this.plugin.logToFile("ESSENTIALSHOOK\n" + e + "\n" + e.getMessage());

            }
            data.put("ESS-NICKNAME", "" + user.getDisplayName());
            if (user.isReachable()) {
                data.put("ESS-ONLINE SINCE", "" + user.getLastOnlineActivity());
            } else {
                data.put("ESS-OFFLINE SINCE", "" + user.getLastOnlineActivity());
            }
        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.putAll(getData(player));
        User user = this.ess.getOfflineUser(player);
        if (user != null) {
            data.put("ESS-UUID", "" + user.getBase().getUniqueId().toString());
            data.put("ESS-HEALTH", "" + user.getBase().getHealth());
            data.put("ESS-HUNGER", "" + user.getBase().getFoodLevel());
            data.put("ESS-XP LEVEL", "" + user.getBase().getLevel());
            data.put("ESS-OPPED", "" + user.getBase().isOp());
//            data.put("ESS-GOD MODE", "" + user.isGodModeEnabled());
            data.put("ESS-FLYING", "" + user.getBase().isFlying());
        }
        return data;
    }

}
