package main.java.com.djrapitops.plan.data.additional;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.comparators.TownComparator;
import static org.bukkit.Bukkit.getOfflinePlayer;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class TownyHook extends Hook {

    private final Plan plugin;
    private Towny towny;

    /**
     * Hooks to Factions plugin
     *
     * @param plugin
     */
    public TownyHook(Plan plugin) throws NoClassDefFoundError {
        super("com.palmergames.bukkit.towny.Towny");
        this.plugin = plugin;
        this.towny = getPlugin(Towny.class);
    }
    
    public TownyHook() {
        super();
        plugin = null;        
    }

    /**
     * @return List of Faction names sorted by power
     */
    public List<String> getTopTowns() {        
        List<Town> topTowns = TownyUniverse.getDataSource().getTowns();
        Collections.sort(topTowns, new TownComparator());
        List<String> townNames = topTowns.stream()
                .map(town -> town.getName())
                .collect(Collectors.toList());
        return townNames;
    }

    /**
     * Grab basic info about Town. isEnabled() should be called before this
     * method.
     *
     * @param townName Name of the town.
     * @return HashMap containing boolean, number & string: RESIDENTS int, MAYOR string, LAND int
     */
    public HashMap<String, Serializable> getTownInfo(String townName) {
        HashMap<String, Serializable> info = new HashMap<>();
        try {
            Town town = TownyUniverse.getDataSource().getTown(townName);
            info.put("RESIDENTS", town.getNumResidents());
            info.put("MAYOR", town.getMayor().getName());
            info.put("LAND", town.getPurchasedBlocks());
        } catch (Exception ex) {
        }
        return info;
    }
    
    /**
     * Grab basic info about Player. isEnabled() should be called before this
     * method.
     *
     * @param uuid UUID of player
     * @return HashMap containing boolean, number & string: TOWN string, Friends string
     */
    public HashMap<String, Serializable> getPlayerInfo(UUID uuid) {
        HashMap<String, Serializable> info = new HashMap<>();
        String name = getOfflinePlayer(uuid).getName();
        try {
            Resident res = TownyUniverse.getDataSource().getResident(name);
            if (res.hasTown()) {
                info.put("TOWN", res.getTown().getName());
            } else {
                info.put("TOWN", "Not in town");
            }
            info.put("FRIENDS", res.getFriends().toString());
        } catch (Exception ex) {
        }
        return info;
    }
}
