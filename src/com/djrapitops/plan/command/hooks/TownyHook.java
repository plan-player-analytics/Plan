package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import com.palmergames.bukkit.towny.Towny;
import static com.palmergames.bukkit.towny.TownyFormatter.getFormattedName;
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
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player);
            if (resident != null) {
                data.put("TOW-ONLINE", new DataPoint("" + BukkitTools.isOnline(player), DataType.BOOLEAN));
                data.put("TOW-REGISTERED", new DataPoint(registeredFormat.format(resident.getRegistered()), DataType.DEPRECATED));
                data.put("TOW-LAST LOGIN", new DataPoint(lastOnlineFormat.format(resident.getLastOnline()), DataType.DEPRECATED));
                data.put("TOW-OWNER OF", new DataPoint(resident.getTownBlocks().size() + " plots", DataType.STRING));
                try {
                    if (resident.hasTown()) {
                        data.put("TOW-TOWN", new DataPoint(getFormattedName(resident.getTown()), DataType.STRING));
                    }
                    if (resident.hasNation()) {
                        if (!resident.getNationRanks().isEmpty()) {
                            data.put("TOW-NATION", new DataPoint(resident.getTown().getNation().getName(), DataType.STRING));
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
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        data.putAll(getData(player));
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player);

            data.put("TOW-PLOT PERMS", new DataPoint(resident.getPermissions().getColourString(), DataType.STRING));
            data.put("TOW-PLOT OPTIONS", new DataPoint("PVP: " + ((resident.getPermissions().pvp) ? "ON" : "OFF") + "  Explosions: " + ((resident.getPermissions().explosion) ? "ON" : "OFF") + "  Firespread: " + ((resident.getPermissions().fire) ? "ON" : "OFF") + "  Mob Spawns: " + ((resident.getPermissions().mobs) ? "ON" : "OFF"), DataType.STRING));
            List<Resident> friends = resident.getFriends();
            data.put("TOW-FRIENDS", new DataPoint(getFormattedResidents("Friends", friends).toString(), DataType.STRING));
        } catch (TownyException e) {
            plugin.logToFile("TOWNYHOOK-All\n" + e + "\nError resident: " + player);

        }
        return data;
    }

}
