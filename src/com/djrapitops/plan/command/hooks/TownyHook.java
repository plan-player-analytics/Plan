package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.palmergames.bukkit.towny.Towny;
import static com.palmergames.bukkit.towny.TownyFormatter.getFormattedName;
import static com.palmergames.bukkit.towny.TownyFormatter.getFormattedResidents;
import static com.palmergames.bukkit.towny.TownyFormatter.lastOnlineFormat;
import static com.palmergames.bukkit.towny.TownyFormatter.registeredFormat;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.util.BukkitTools;
import java.util.HashMap;
import java.util.List;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static com.palmergames.bukkit.towny.TownyFormatter.getFormattedResidents;

public class TownyHook implements Hook {

    private Towny towny;
    private final Plan plugin;

    public TownyHook(Plan p) throws Exception {
        this.towny = getPlugin(Towny.class);
        this.plugin = p;
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player);
            if (resident != null) {
                data.put("TOW-ONLINE", "" + BukkitTools.isOnline(player));
                data.put("TOW-REGISTERED", registeredFormat.format(resident.getRegistered()));
                data.put("TOW-LAST LOGIN", lastOnlineFormat.format(resident.getLastOnline()));
                data.put("TOW-OWNER OF", resident.getTownBlocks().size() + " plots");
                try {
                    if (resident.hasTown()) {
                        data.put("TOW-TOWN", getFormattedName(resident.getTown()));
                    }
                    if (resident.hasNation()) {
                        if (!resident.getNationRanks().isEmpty()) {
                            data.put("TOW-NATION", resident.getTown().getNation().getName());
                        }
                    }
                } catch (TownyException e) {
                    plugin.logToFile("TOWNYHOOK\n" + e + "\n" + e.getMessage());

                }

            }
        } catch (TownyException e) {
            plugin.logToFile("TOWNYHOOK\n" + e + "\nError resident: " + player);

        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.putAll(getData(player));
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player);

            data.put("TOW-PLOT PERMS", resident.getPermissions().getColourString());
            data.put("TOW-PLOT OPTIONS", "PVP: " + ((resident.getPermissions().pvp) ? "ON" : "OFF") + "  Explosions: " + ((resident.getPermissions().explosion) ? "ON" : "OFF") + "  Firespread: " + ((resident.getPermissions().fire) ? "ON" : "OFF") + "  Mob Spawns: " + ((resident.getPermissions().mobs) ? "ON" : "OFF"));
            List<Resident> friends = resident.getFriends();
            data.put("TOW-FRIENDS", getFormattedResidents("Friends", friends).toString());
        } catch (TownyException e) {
            plugin.logToFile("TOWNYHOOK-All\n" + e + "\nError resident: " + player);

        }
        return data;
    }

}
